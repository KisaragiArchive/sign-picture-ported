package com.github.kisaragieffective.signpictureported.mixin;

import com.github.kisaragieffective.signpictureported.internal.BuiltinPreciseCacheRepositories;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientChunkManager.class)
public class ChunkInvalidateMixin {
    @Shadow @Final
    ClientWorld world;

    @Inject(method = "unload", at = @At("HEAD"))
    public void onChunkUnloaded(int chunkX, int chunkZ, CallbackInfo ci) {
        BuiltinPreciseCacheRepositories.NORMAL.releaseByChunk(world.getDimension(), chunkX, chunkZ);
    }
}
