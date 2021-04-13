package com.github.kisaragieffective.signpictureported.mixin;

import com.github.kisaragieffective.signpictureported.*;
import com.github.kisaragieffective.signpictureported.api.DisplayConfigurationParseResult;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Block;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Quaternion;
import net.minecraft.world.LightType;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.github.kisaragieffective.signpictureported.InternalSpecialUtility.dummy;

@Mixin(SignBlockEntityRenderer.class)
public class SignBlockEntityRenderMixin {
    private final Map<URL, NativeImageBackedTexture> correspond = new HashMap<>();
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private final Set<String> invalidURL = new HashSet<>();
    private final Set<String> badURLs = new HashSet<>();
    private final Map<URL, NativeImageBackedTexture> flippedCache = new HashMap<>();
    private static final float EPS_F = 0.0001F;
    private static final double EPS_D = 0.0000001;
    @Contract("_ -> fail")
    private static void notifyUnknownBlock(BlockPos pos) {
        SignPicturePorted.LOGGER.warn("Unknown sign-like block: " + pos);
        throw new IllegalStateException("Unsupported block");
    }

    private static float getSignRotation(SignBlockEntity sbe) {
        BlockState blockState = sbe.getCachedState();
        float rotateAngle;
        final Block b = blockState.getBlock();
        if (b instanceof SignBlock) {
            // stand
            rotateAngle = -((float)(blockState.get(SignBlock.ROTATION) * 360) / 16.0F);
            return rotateAngle;
        } else if (b instanceof WallSignBlock) {
            // on wall
            rotateAngle = blockState.get(WallSignBlock.FACING).asRotation();
            return rotateAngle;
        } else {
            notifyUnknownBlock(sbe.getPos());
            return dummy();
        }
    }

    private static void selectTexture(NativeImageBackedTexture nibt) {
        if (nibt == null) return;
        Identifier id = newIdentifierOrCached(nibt);
        Objects.requireNonNull(
                CLIENT.getTextureManager().getTexture(id), "TextureManager.getTexture"
        ).bindTexture();
    }

    private boolean isTarget(SignBlockEntity sbe) {
        final Block b = sbe.getCachedState().getBlock();
        return b instanceof SignBlock || b instanceof WallSignBlock;
    }
    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    private void injectRender(SignBlockEntity signBlockEntity, float f,
                              MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider,
                              int i, int j, CallbackInfo defModelRender) {
        final List<String> x = InternalSpecialUtility.getPlaintextLines(signBlockEntity);
        final String specs = String.join("", x);
        final boolean isURLSpec = specs.startsWith("#$");
        final boolean isTextureSpec = specs.startsWith("!");
        if (!isURLSpec && !isTextureSpec) {
            return;
        }

        if (!isTarget(signBlockEntity)) {
            final BlockPos pos = signBlockEntity.getPos();
            notifyUnknownBlock(pos);
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

            final String url = urlFlag.startsWith("http://") || urlFlag.startsWith("https://")
                    ? urlFlag
                    : "https://" + urlFlag;
            SignPicturePorted.LOGGER.info(url);
            final Optional<URL> urlOpt = parseURL(url);
            // empty implies parsing was failed
            if (urlOpt.isEmpty()) return;
            final URL url2 = urlOpt.get();

            NativeImageBackedTexture nibt = null;
            try {
                nibt = CompletableFuture.supplyAsync(() -> url2)
                        .handle((r, l) -> registerFrom(r))
                        .get();
            } catch (InterruptedException | ExecutionException | NoSuchElementException e) {
                SignPicturePorted.LOGGER.catching(e);
            }

            if (nibt == null) return;
            selectTexture(nibt);
            // NOTE テクスチャ向いてるほうがZ-

            int lightLevel = signBlockEntity.getWorld().getLightLevel(LightType.BLOCK, signBlockEntity.getPos());
            SignPicturePorted.LOGGER.info("light: " + lightLevel + ", pos: " + signBlockEntity.getPos());
            matrices.push();
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

            // TODO make configurable
            final DisplayConfigurationParseResult pr = DisplayConfigurationParseResult.DEFAULT;
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
            // TODO set brightness
            drawImage(matrices);

            // 左右反転
            final NativeImageBackedTexture flipped = flippedCache.get(url2);
            selectTexture(flipped);
            matrices.multiply(new Quaternion(0.0F, 180.0F, 0.0F, true));
            // 左上が中心点なのでもとに戻す
            // scaleYは影響しない？
            matrices.translate(-scaleX, 0.0, 0.0);
            // TODO set brightness
            drawImage(matrices);

            matrices.pop();
            // 元々のモデルを黙らせる
            defModelRender.cancel();
        } else {
            // TODO: path points client texture
            String spec = specs.substring(1);
        }
    }

    private void drawImage(MatrixStack matrices) {
        // depth test resolves z-issue; avoiding invalid depth
        RenderSystem.enableDepthTest();
        DrawableHelper.drawTexture(matrices, 0, 0, 0, 0, 0, 1, 1, 1, 1);
        RenderSystem.disableDepthTest();
    }

    private static Optional<URL> parseURL(String fragment) {
        try {
            return Optional.of(new URL(fragment));
        } catch (MalformedURLException e) {
            return Optional.empty();
        }
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
            final Optional<NativeImage> opt = getContent(url);
            if (opt.isEmpty()) return null;
            returning = new NativeImageBackedTexture(opt.get());
            correspond.put(url, returning);
            flippedCache.putIfAbsent(url, flippedCache.computeIfAbsent(url, _ignored -> new NativeImageBackedTexture(
                    // idempotent
                    InternalSpecialUtility.mirrorHorizontally(
                            Objects.requireNonNull(returning.getImage(), "Corresponding image was disposed!")
                    )
            )));
        }

        return returning;
    }

    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.ALWAYS)
            // .authenticator(Authenticator.getDefault())
            .build();

    private static <A, R, X extends Throwable> Optional<R> completeSuccessfulOrNone(A a, MayThrowFunction<A, R, X> arx) {
        return completeSuccessfulOrNone(a, arx, Tag.of());
    }

    private static <A, R, X extends Throwable> Optional<R> completeSuccessfulOrNone(A a, MayThrowFunction<A, R, X> arx, Tag<X> tx) {
        try {
            return Optional.of(arx.apply(a));
        } catch (Throwable t) {
            if (tx.isAcceptable(t)) {
                return Optional.empty();
            } else {
                throw new RuntimeException(t);
            }
        }
    }

    private Optional<NativeImage> getContent(URL url) {
        final Optional<URI> xxx = completeSuccessfulOrNone(url, URL::toURI);
        if (xxx.isEmpty()) return Optional.empty();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(xxx.get())
                .GET()
                .build();

        return client.sendAsync(req, HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(HttpResponse::body)
                .thenApply(x -> completeSuccessfulOrNone(x, NativeImage::read))
                .join();
    }
}
