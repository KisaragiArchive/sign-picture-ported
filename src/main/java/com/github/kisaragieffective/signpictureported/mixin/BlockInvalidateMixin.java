package com.github.kisaragieffective.signpictureported.mixin;

import com.github.kisaragieffective.signpictureported.NativeImageFactory;
import com.github.kisaragieffective.signpictureported.OutsideCache;
import com.github.kisaragieffective.signpictureported.ImageWrapper;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Mixin(World.class)
public class BlockInvalidateMixin {
    @Inject(method = "markDirty", at = @At("HEAD"))
    public void onBlockInvalidate(BlockPos pos, CallbackInfo ci) {
        final ImageWrapper r2 = OutsideCache.cache.get(pos);
        if (r2 == null) return;
        final NativeImageBackedTexture nibt = r2.nibt.get();
        final Set<? extends @NotNull NativeImageBackedTexture> excludedSet = new HashSet<>(Arrays.asList(
                NativeImageFactory.errorImage.getValue(),
                NativeImageFactory.loadingImage.getValue()
        ));
        if (nibt != null && excludedSet.stream().noneMatch(x -> x.equals(nibt))) {
            OutsideCache.drop(pos);
        }
    }
}
