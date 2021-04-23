package com.github.kisaragieffective.signpictureported.mixin;

import com.github.kisaragieffective.signpictureported.InternalSpecialUtility;
import com.github.kisaragieffective.signpictureported.ParseResult;
import com.github.kisaragieffective.signpictureported.SignPicturePorted;
import com.github.kisaragieffective.signpictureported.TextureFlipper;
import com.mojang.blaze3d.systems.RenderSystem;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@Mixin(SignBlockEntityRenderer.class)
public class SignBlockEntityRenderMixin {
    // TODO out-memory cache
    private final Map<URL, SoftReference<NativeImageBackedTexture>> correspond = new HashMap<>();
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private final Set<String> invalidURL = new HashSet<>();
    private final Set<String> badURLs = new HashSet<>();
    private Map<URL, SoftReference<NativeImageBackedTexture>> flippedCache = new HashMap<>();

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

    private static void selectTexture(NativeImageBackedTexture nibt) {
        if (nibt == null) return;
        Identifier id = newIdentifierOrCached(nibt);
        Objects.requireNonNull(CLIENT.getTextureManager().getTexture(id)).bindTexture();
    }

    /**
     * SignBlockEntityに応じたMatrixStackの調整を入れる
     * @param matrices
     * @param signBlockEntity
     */
    private static void fixMatrices(MatrixStack matrices, SignBlockEntity signBlockEntity) {
        final boolean isStand = signBlockEntity.getCachedState().getBlock() instanceof SignBlock;
        final boolean isOnWall = signBlockEntity.getCachedState().getBlock() instanceof WallSignBlock;
        if (isStand) {
            matrices.translate(0.5, 0.5, 0.5);
            // 左右反転するなら逆手に取れば良いのでは？
            matrices.scale(1F, -1F, 1F);
            // 角度補正
            float rotateAngle = getSignRotation(signBlockEntity);

            matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(rotateAngle));
            matrices.translate(-0.5, -0.5, 0);
        } else if (isOnWall) {
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
    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    private void injectRender(SignBlockEntity signBlockEntity, float f,
                              MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider,
                              int i, int j, CallbackInfo defModelRender) {
        //matrices.push();
        List<String> x = InternalSpecialUtility.getPlaintextLines(signBlockEntity);
        String specs = String.join("", x);
        // BlockPos pos = signBlockEntity.getPos();
        // SignPicturePorted.LOGGER.info("{ pos: " + pos + ", specs: " + specs + " }");
        boolean isURLSpec = specs.startsWith("#$");
        boolean isTextureSpec = specs.startsWith("!");
        if (!isURLSpec && !isTextureSpec) {
            return;
        }

        // SignPicturePorted.LOGGER.info("matched");

        if (isURLSpec) {
            // always start with "#$"
            int confBegin = specs.indexOf("{");
            // SignPicturePorted.LOGGER.debug("conf start:" + confBegin);
            String urlFlag = confBegin >= 0 ? specs.substring(2, confBegin) : specs.substring(2);
            if (invalidURL.contains(urlFlag)) {
                return;
            }

            // String conf = confBegin >= 0 ? specs.substring(confBegin) : "";
            // SignPicturePorted.LOGGER.info("fragment:" + urlFlag);

            URL parsedURL;
            try {
                parsedURL = new URL(urlFlag.startsWith("http://") || urlFlag.startsWith("https://") ? urlFlag : "https://" + urlFlag);
            } catch (MalformedURLException | IllegalArgumentException e) {
                // Adding IllegalArgumentException for sun.net.spi.DefaultProxySelector.select
                invalidURL.add(urlFlag);
                return;
            }

            NativeImageBackedTexture nibt = registerFrom(parsedURL);
            if (nibt == null) return;
            selectTexture(nibt);
            NativeImage img = nibt.getImage();
            Objects.requireNonNull(img);
            // NOTE テクスチャ向いてるほうがZ-
            BlockState bs = signBlockEntity.getCachedState();
            final boolean isStand = bs.getBlock() instanceof SignBlock;
            final boolean isOnWall = bs.getBlock() instanceof WallSignBlock;
            if (!isStand && !isOnWall) {
                SignPicturePorted.LOGGER.warn("Unknown sign-like block");
                return;
            }

            matrices.push();
            // color(1, 0, 1);
            //
            fixMatrices(matrices, signBlockEntity);

            // TODO make it change-able
            final ParseResult pr = ParseResult.DEFAULT;
            float rotateX = (float) pr.rotateX;
            float rotateY = (float) pr.rotateY;
            float rotateZ = (float) pr.rotateZ;
            matrices.multiply(new Quaternion(rotateX, rotateY, rotateZ, true));
            double offsetUp = pr.offsetUp;
            double offsetRight = pr.offsetRight;
            double offsetDepth = pr.offsetDepth;
            // against Z-fighting
            matrices.translate(offsetRight, offsetUp, offsetDepth + 0.001);
            float scaleX = (float) pr.scaleX;
            float scaleY = (float) pr.scaleY;
            matrices.scale(scaleX, scaleY, 1.0F);
            drawImage(matrices);

            // 左右反転したテクスチャを裏側に表示させる
            selectTexture(new NativeImageBackedTexture(TextureFlipper.flipHorizontal(img)));
            matrices.translate(scaleX, 0.0, 0.0);
            matrices.multiply(new Quaternion(0.0F, 180.0F, 0.0F, true));
            drawImage(matrices);
            matrices.pop();
            // 元々のモデルを黙らせる
            defModelRender.cancel();
        } else {
            // TODO: texture
            String spec = specs.substring(1);
        }
    }

    private void drawImage(MatrixStack matrices) {
        // depth test resolves z-issue
        RenderSystem.enableDepthTest();
        DrawableHelper.drawTexture(matrices, 0, 0, 0, 0, 0, 1, 1, 1, 1);
        RenderSystem.disableDepthTest();
    }

    private static final Map<NativeImageBackedTexture, Identifier> identifierMap = new HashMap<>();
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
        correspond.values()
                .stream()
                .map(SoftReference::get)
                .filter(Objects::nonNull)
                .forEach(NativeImageBackedTexture::close);
        correspond.clear();
        flippedCache.values()
                .stream()
                .map(SoftReference::get)
                .filter(Objects::nonNull)
                .forEach(NativeImageBackedTexture::close);
        flippedCache.clear();
        identifierMap.keySet().forEach(x -> x.close());
        identifierMap.clear();
        invalidURL.clear();
    }

    private NativeImageBackedTexture registerFrom(URL url) {
        if (correspond.containsKey(url) && correspond.get(url).get() != null) {
            return correspond.get(url).get();
        }

        // If we don't do that, the game will be frozen
        if (badURLs.contains(url.toString())) {
            return null;
        }
        /*
        System.out.println(flippedCache.entrySet().parallelStream()
                .map(x -> x.getKey() + ": " + x.getValue())
                .collect(Collectors.joining(", ", "[", "]")));
        */
        NativeImageBackedTexture returning;
        {
            InputStream httpBuffer;
            try {
                HttpURLConnection huc1;
                if (url.getHost() == null || url.getProtocol() == null) return null;
                huc1 = cast(url.openConnection());
                huc1.setInstanceFollowRedirects(true);
                huc1.connect();
                final int code = huc1.getResponseCode();
                if (code == 200) {
                    // OK: Do nothing
                } else if (code == 204) {
                    throw new IOException("Nothing can't be rendered (HTTP 204)");
                } else {
                    throw new IOException("URL connection failed: {code: " + code + "}");
                }

                /*
                Check response MIME type, these types will be accepted:
                    * image/png
                    * image/jpg
                    * image/xml+svg
                 */
                String mimeType = huc1.getContentType();

                if (!mimeType.startsWith("image/")) {
                    throw new IOException("Content-Type " + mimeType + " is not image");
                }
                httpBuffer = huc1.getInputStream();
            } catch (IllegalArgumentException | IOException e) {
                SignPicturePorted.LOGGER.error("Exception caught", e);
                badURLs.add(url.toString());
                return null;
            }

            NativeImage mayImage;
            InputStream bufferedHttpIS = new BufferedInputStream(httpBuffer);
            try {
                mayImage = NativeImage.read(bufferedHttpIS);
            } catch (IOException | UnsupportedOperationException e) {
                badURLs.add(url.toString());
                SignPicturePorted.LOGGER.error("During get NativeImage", e);
                return null;
            }

            returning = new NativeImageBackedTexture(mayImage);
            correspond.put(url, new SoftReference<>(returning));
        }

        return returning;
    }

    private <E> E cast(Object x) { return (E) x; }
}
