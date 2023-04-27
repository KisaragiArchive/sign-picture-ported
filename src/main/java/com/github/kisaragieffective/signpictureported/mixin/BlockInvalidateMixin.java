package com.github.kisaragieffective.signpictureported.mixin;

import com.github.kisaragieffective.signpictureported.internal.BuiltinPreciseCacheRepositories;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class BlockInvalidateMixin {
    @Shadow public abstract DimensionType getDimension();

    @Inject(method = "markDirty", at = @At("HEAD"))
    public void onBlockInvalidate(BlockPos pos, CallbackInfo ci) {
        final DimensionType dim = getDimension();

        BuiltinPreciseCacheRepositories.NORMAL.releaseByBlockPos(dim, pos);
    }
}
