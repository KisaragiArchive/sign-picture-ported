package com.github.kisaragieffective.signpictureported.mixin;

import com.github.kisaragieffective.signpictureported.OutsideCache;
import net.minecraft.client.world.ClientChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientChunkManager.class)
public class ChunkInvalidateMixin {
    @Inject(method = "unload", at = @At("HEAD"))
    public void onChunkUnloaded(int chunkX, int chunkZ, CallbackInfo ci) {
        final int lowerX = chunkX * 16;
        final int upperX = lowerX + 15;
        final int lowerZ = chunkZ * 16;
        final int upperZ = lowerZ + 15;
        OutsideCache.locations()
                .stream()
                .filter(pos -> lowerX <= pos.getX()
                        && pos.getX() <= upperX
                        && lowerZ <= pos.getZ()
                        && pos.getZ() <= upperZ)
                .forEach(OutsideCache::drop);
    }
}
