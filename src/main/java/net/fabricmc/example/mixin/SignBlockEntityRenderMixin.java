package net.fabricmc.example.mixin;

import net.fabricmc.example.InternalSpecialUtility;
import net.fabricmc.example.SignPictureReloaded;
import net.minecraft.block.BlockState;
import net.minecraft.block.SignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Quaternion;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Mixin(SignBlockEntityRenderer.class)
public class SignBlockEntityRenderMixin {
    private final Map<URL, NativeImageBackedTexture> correspond = new HashMap<>();
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private final Set<String> invalidURL = new HashSet<>();
    private final Set<String> notFound = new HashSet<>();
    private Map<URL, NativeImageBackedTexture> flippedCache = new HashMap<>();

    private static float getSignRotation(SignBlockEntity sbe) {
        BlockState blockState = sbe.getCachedState();
        float rotateAngle;
        if (blockState.getBlock() instanceof SignBlock) {
            // stand
            rotateAngle = -((float)(blockState.get(SignBlock.ROTATION) * 360) / 16.0F);
        } else {
            // on wall
            rotateAngle = blockState.get(WallSignBlock.FACING).asRotation();
        }
        return rotateAngle;
    }

    private static void color(float r, float g, float b) {
        GL11.glColor3f(r, g, b);
    }

    private static void selectTexture(Identifier id) {
        Objects.requireNonNull(CLIENT.getTextureManager().getTexture(id)).bindTexture();
    }

    private static void selectTexture(NativeImageBackedTexture nibt) {
        if (nibt == null) return;
        Identifier ident = newIdentifierOrCached(nibt);
        selectTexture(ident);
    }
    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    private void injectRender(SignBlockEntity signBlockEntity, float f,
                              MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider,
                              int i, int j, CallbackInfo defModelRender) {
        //matrices.push();
        List<String> x = InternalSpecialUtility.getPlaintextLines(signBlockEntity);
        String specs = String.join("", x);
        boolean isURLSpec = specs.startsWith("#$");
        boolean isTextureSpec = specs.startsWith("!");
        if (!isURLSpec && !isTextureSpec) {
            return;
        }

        if (isURLSpec) {
            // always start with "#$"
            int confBegin = specs.indexOf("{");
            SignPictureReloaded.LOGGER.info("conf start:" + confBegin);
            String spec = confBegin >= 0 ? specs.substring(2, confBegin) : specs.substring(2);
            SignPictureReloaded.LOGGER.info("spec:" + spec);
            if (invalidURL.contains(spec)) {
                return;
            }

            URL parsedURL;
            try {
                parsedURL = new URL("https://" + spec);
            } catch (MalformedURLException | IllegalArgumentException e) {
                // Adding IllegalArgumentException for sun.net.spi.DefaultProxySelector.select
                invalidURL.add(spec);
                return;
            }

            NativeImageBackedTexture nibt = registerFrom(parsedURL);
            if (nibt == null) return;
            Identifier ident = newIdentifierOrCached(nibt);
            selectTexture(ident);
            NativeImage img = nibt.getImage();
            Objects.requireNonNull(img);
            // NOTE テクスチャ向いてるほうがZ-
            BlockState bs = signBlockEntity.getCachedState();
            final boolean isStand = bs.getBlock() instanceof SignBlock;
            final boolean isOnWall = bs.getBlock() instanceof WallSignBlock;
            if (!isStand && !isOnWall) {
                SignPictureReloaded.LOGGER.warn("Unknown sign-like block");
                return;
            }

            matrices.push();
            // color(1, 0, 1);
            //
            {
                if (isStand) {
                    matrices.translate(0.5, 0.5, 0.5);
                    // 左右反転するなら逆手に取れば良いのでは？
                    matrices.scale(1F, -1F, 1F);
                    // 角度補正
                    float rotateAngle = getSignRotation(signBlockEntity);

                    matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(rotateAngle));
                    matrices.translate(-0.5, -0.5, 0);
                } else {
                    matrices.scale(1F, -1F, 1F);
                    // 下にずれる問題の修正
                    matrices.translate(0.0, -1.0, 0.0);
                    float rotateAngle = getSignRotation(signBlockEntity);
                    final float zvz = 0.0001f;
                    // todo: 力技
                    if (Math.abs(rotateAngle - 0f) < zvz) {
                        // nothing to do
                    } else if (Math.abs(rotateAngle - 90.0f) < zvz) {
                        matrices.translate(1, 0, 0);
                    } else if (Math.abs(rotateAngle - 180.0f) < zvz) {
                        matrices.translate(1, 0, 1);
                    } else if (Math.abs(rotateAngle - 270.0f) < zvz) {
                        matrices.translate(0, 0, 1);
                    }

                    matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(360 - rotateAngle));
                }
            }

            // TODO make it change-able
            float rotateX = 0.0F;
            float rotateY = 0.0F;
            float rotateZ = 0.0F;
            matrices.multiply(new Quaternion(rotateX, rotateY, rotateZ, true));
            double offsetX = 0.0;
            double offsetY = 0.0;
            double offsetZ = 0.0;
            matrices.translate(offsetX, offsetY, offsetZ);
            float scaleX = 1.0F;
            float scaleY = 1.0F;
            float scaleZ = 1.0F;
            matrices.scale(scaleX, scaleY, scaleZ);
            drawImage(matrices);
            // 左右反転 - 多分不可能

            matrices.pop();
            // TODO: 左右反転したテクスチャを裏側に表示させる
            /*
            color(0, 1, 1);
            selectTexture(flippedCache.get(parsedURL));
            // matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180f));
            matrices.translate(0, 0, 1);
            drawImage(matrices);
            */

            // 元々のモデルを黙らせる
            defModelRender.cancel();
            // GL11.glEnable(GL11.GL_BLEND);

            //matrices.pop();
        } else {
            // is texture
            String spec = specs.substring(1);
        }
    }

    private void drawImage(MatrixStack matrices) {
        DrawableHelper.drawTexture(matrices, 0, 0, 0, 0, 0, 1, 1, 1, 1);
    }

    private static Map<NativeImageBackedTexture, Identifier> identifierMap = new HashMap<>();
    private static Identifier newIdentifierOrCached(NativeImageBackedTexture nibt) {
        if (identifierMap.containsKey(nibt)) {
            return identifierMap.get(nibt);
        }

        Identifier newIdentifier = CLIENT.getTextureManager().registerDynamicTexture("sgpc_reloaded", nibt);
        identifierMap.put(nibt, newIdentifier);
        return newIdentifier;
    }

    private <E> E TODO() { throw new RuntimeException(); }

    private void invalidate() {
        correspond.values().forEach(x -> x.close());
        correspond.clear();
        flippedCache.values().forEach(x -> x.close());
        flippedCache.clear();
        identifierMap.keySet().forEach(x -> x.close());
        identifierMap.clear();
        invalidURL.clear();
    }

    private NativeImageBackedTexture registerFrom(URL url) {
        if (correspond.containsKey(url)) {
            return correspond.get(url);
        }

        // If we don't do that, the game will be frozen
        if (notFound.contains(url.toString())) {
            return null;
        }
        System.out.println(flippedCache.entrySet().parallelStream()
                .map(x -> x.getKey() + ": " + x.getValue())
                .collect(Collectors.joining(", ", "[", "]")));
        NativeImageBackedTexture returning;
        {
            InputStream httpBuffer;
            try {
                HttpURLConnection huc1;
                if (url.getHost() == null || url.getProtocol() == null) return null;
                huc1 = cast(url.openConnection());
                huc1.connect();
                if (huc1.getResponseCode() != 200) {
                    notFound.add(url.toString());
                    throw new IOException("Respond code is Not ok");
                }
                // MIME type
                String mimeType = huc1.getContentType();
                if (!mimeType.startsWith("image/")) {
                    throw new IOException("Content-Type " + mimeType + " is not image");
                }
                httpBuffer = huc1.getInputStream();
            } catch (IllegalArgumentException | IOException e) {
                LogManager.getLogger().catching(e);
                return null;
            }

            NativeImage mayImage;
            InputStream bufferedHttpIS = new BufferedInputStream(httpBuffer);
            try {
                mayImage = NativeImage.read(bufferedHttpIS);
            } catch (IOException e) {
                System.out.println("Error during getting NativeImage" + ": " + e.getClass().getSimpleName() + ": " + e.getMessage());
                return null;
            }

            returning = new NativeImageBackedTexture(mayImage);
            correspond.put(url, returning);
            // TODO: Flipped Image Cache

        }

        return returning;
    }

    private <E> E cast(Object x) { return (E) x; }
}
