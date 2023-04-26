package com.github.kisaragieffective.signpictureported.mixin;

import com.github.kisaragieffective.signpictureported.ImageWrapper;
import com.github.kisaragieffective.signpictureported.StaticNativeImage;
import com.github.kisaragieffective.signpictureported.OutsideCache;
import com.github.kisaragieffective.signpictureported.SignPicturePorted;
import com.github.kisaragieffective.signpictureported.TextureFlipper;
import com.github.kisaragieffective.signpictureported.api.DisplayConfigurationParseResult;
import com.github.kisaragieffective.signpictureported.internal.ErrorOrValid;
import com.github.kisaragieffective.signpictureported.internal.InternalSpecialUtility;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.LightType;
import org.jetbrains.annotations.Contract;
import org.joml.AxisAngle4d;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static com.github.kisaragieffective.signpictureported.internal.InternalSpecialUtility.never;
import static com.github.kisaragieffective.signpictureported.internal.WrapExceptions.wrapExceptionToUnchecked;

@Mixin(SignBlockEntityRenderer.class)
public class SignBlockEntityRenderMixin {
    // TODO out-memory cache
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final float EPS_F = 0.0001F;
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
            return never();
        }
    }

    private static void selectTexture(BlockPos pos, NativeImageBackedTexture nibt) {
        if (nibt == null) return;
        Identifier id = OutsideCache.putNewIdentifierOrCached(pos, nibt);
        selectTexturePure(id);
    }

    private static void selectFlippedTexture(BlockPos pos, NativeImageBackedTexture nibt) {
        if (nibt == null) return;
        Identifier id = OutsideCache.putFlippedNewIdentifierOrCached(pos, nibt);
        selectTexturePure(id);
    }

    private static void selectTexturePure(Identifier id) {
        Objects.requireNonNull(
                CLIENT.getTextureManager().getTexture(id), "TextureManager.getTexture"
        ).bindTexture();
    }

    private boolean isTarget(SignBlockEntity sbe) {
        final Block b = sbe.getCachedState().getBlock();
        return b instanceof SignBlock || b instanceof WallSignBlock;
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

            matrices.multiply(new Quaternionf(new AxisAngle4d(rotateAngle, 0, 1, 0)));
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

            rotateByAxisY(matrices, 360 - rotateAngle);
        }
    }

    private static void rotateByAxisY(MatrixStack matrices, float rotateAngle) {
        // 1.19.3+: RotationAxis.POSITIVE_Y.rotationDegrees
        // 1.19.2-: Vec3f.POSITIVE_Y
        // more legacy version: Vector3f.POSITIVE_Y
        rotateAllAxisByDegrees(matrices, 0, rotateAngle, 0);
    }

    private static void rotateAllAxisByDegrees(MatrixStack matrices, float degX, float degY, float degZ) {
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(degX).rotateY(degY).rotateZ(degZ));
    }

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    private void injectRender(SignBlockEntity signBlockEntity, float f,
                              MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider,
                              int i, int j, CallbackInfo defModelRender) {
        final String specs = String.join("", InternalSpecialUtility.getPlaintextLines(signBlockEntity));
        final boolean isURLSpec = specs.startsWith("#$");
        final boolean isTextureSpec = specs.startsWith("!");
        final BlockPos pos = signBlockEntity.getPos();
        if (!isURLSpec && !isTextureSpec) {
            return;
        }

        if (!isTarget(signBlockEntity)) {
            notifyUnknownBlock(pos);
            return;
        }

        // SignPicturePorted.LOGGER.info("matched");

        if (isURLSpec) {
            // always start with "#$"
            int confBegin = specs.indexOf("{");
            // SignPicturePorted.LOGGER.debug("conf start:" + confBegin);
            String urlFlag = confBegin >= 0 ? specs.substring(2, confBegin) : specs.substring(2);
            if (OutsideCache.invalidURL.contains(urlFlag)) {
                return;
            }

            // String conf = confBegin >= 0 ? specs.substring(confBegin) : "";
            // SignPicturePorted.LOGGER.info("fragment:" + urlFlag);

            final String url = urlFlag.startsWith("http://") || urlFlag.startsWith("https://")
                    ? urlFlag
                    : "https://" + urlFlag;
            // SignPicturePorted.LOGGER.info(url);
            final Optional<URL> urlOpt = parseURL(url);
            // empty implies parsing was failed
            if (!urlOpt.isPresent()) return;
            final URL url2 = urlOpt.get();
            final NativeImageBackedTexture nibt;
            final NativeImage ni;
            {
                final Supplier<NativeImageBackedTexture> fetch = () -> fetchFrom(url2, OptionalLong.empty())
                        .value()
                        .map(NativeImageBackedTexture::new)
                        .orElseGet(StaticNativeImage.errorImage::getValue);
                if (!OutsideCache.sbp.contains(pos)) {
                    // unsafeRunAsyncAndForget
                    CompletableFuture.supplyAsync(fetch)
                            .thenAccept(x -> OutsideCache.put(pos, x, false));
                    OutsideCache.sbp.add(pos);
                }
                final Supplier<NativeImageBackedTexture> loadSupplier = StaticNativeImage.loadingImage::getValue;
                final ImageWrapper cacheEntry = OutsideCache.putOrCached(pos, loadSupplier);
                final Optional<NativeImageBackedTexture> cache = Optional.ofNullable(cacheEntry.nibt.get());
                nibt = cache.orElseGet(loadSupplier);
                ni = nibt.getImage();
                // SignPicturePorted.LOGGER.info("back(pos: " + pos + "): " + ni);
                Objects.requireNonNull(ni);
                selectTexture(pos, nibt);
            }
            // NOTE テクスチャ向いてるほうがZ-

            int lightLevel = signBlockEntity.getWorld().getLightLevel(LightType.BLOCK, signBlockEntity.getPos());
            // SignPicturePorted.LOGGER.info("light: " + lightLevel + ", pos: " + signBlockEntity.getPos());
            matrices.push();
            // color(1, 0, 1);
            //
            fixMatrices(matrices, signBlockEntity);

            // TODO make it configurable
            final DisplayConfigurationParseResult pr = DisplayConfigurationParseResult.DEFAULT;
            float rotateX = (float) pr.rotateX;
            float rotateY = (float) pr.rotateY;
            float rotateZ = (float) pr.rotateZ;
            matrices.multiply(new Quaternionf(new AxisAngle4f(0.0F, rotateX, rotateY, rotateZ)));
            double offsetUp = pr.offsetUp;
            double offsetRight = pr.offsetRight;
            double offsetDepth = pr.offsetDepth;
            // against Z-fighting
            matrices.translate(offsetRight, offsetUp, offsetDepth + EPS_F);
            float scaleX = (float) pr.scaleX;
            float scaleY = (float) pr.scaleY;
            matrices.scale(scaleX, scaleY, 1.0F);
            // TODO set brightness
            drawImage(matrices);

            // 左右反転したテクスチャを裏側に表示させる
            {
                Supplier<? extends NativeImageBackedTexture> ff =
                        () -> new NativeImageBackedTexture(TextureFlipper.flipHorizontal(ni));
                ImageWrapper cache2 = OutsideCache.putFlippedOrCached(pos, ff);
                final NativeImageBackedTexture nibt2;
                final Optional<NativeImageBackedTexture> cache12 = Optional.ofNullable(cache2.nibt.get());
                nibt2 = cache12.orElseGet(ff);
                selectFlippedTexture(pos, nibt2);
            }

            matrices.translate(scaleX, 0.0, 0.0);
            rotateByAxisY(matrices, 180);
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


    private static ErrorOrValid<? extends String, ? extends NativeImage> fetchFrom(URL url, OptionalLong ifModifiedSince) {
        SignPicturePorted.LOGGER.info("fetchFrom: " + url);
        try {
            //noinspection Convert2MethodRef
            return CompletableFuture.supplyAsync(wrapExceptionToUnchecked(() -> url.openConnection()))
                    .thenApplyAsync(x -> (HttpURLConnection) x)
                    .thenApplyAsync(x -> {
                        x.setInstanceFollowRedirects(true);
                        ifModifiedSince.ifPresent(x::setIfModifiedSince);
                        wrapExceptionToUnchecked(x::connect).run();
                        return x;
                    })
                    .thenApplyAsync(x -> wrapExceptionToUnchecked(x::getInputStream).get())
                    .thenApplyAsync(x -> wrapExceptionToUnchecked(() -> NativeImage.read(x)).get())
                    .<ErrorOrValid<? extends String, ? extends NativeImage>>handleAsync((res, ex) ->
                            res != null ? ErrorOrValid.valid(res) : ErrorOrValid.error(ex.toString()))
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            return ErrorOrValid.error(e.getMessage());
        }
    }
}
