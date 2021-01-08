package net.fabricmc.example.mixin;

import net.fabricmc.example.InternalSpecialUtility;
import net.minecraft.block.BlockState;
import net.minecraft.block.SignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.render.Tessellator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin {
    @Shadow public abstract BlockEntityType<?> getType();

    @Shadow protected BlockPos pos;

    @Shadow @Nullable protected World world;

    @Inject(at = @At("HEAD"), method = "markDirty")
    private void a(CallbackInfo ci) {
        if (this.getType() != BlockEntityType.SIGN) {
            return;
        }

        SignBlockEntity xxx = (SignBlockEntity) this.world.getBlockEntity(this.pos);
        if (xxx == null) return;
        BlockState blockState = xxx.getCachedState();
        float rotateAngle;
        if (blockState.getBlock() instanceof SignBlock) {
            // stand
            rotateAngle = -((float)(blockState.get(SignBlock.ROTATION) * 360) / 16.0F);
        } else {
            // on wall
            rotateAngle = blockState.get(WallSignBlock.FACING).asRotation();
        }
        println("Sign " + this.pos + " angle: " + rotateAngle);

        final String url = String.join("", InternalSpecialUtility.getPlaintextLines(xxx));
        PlayerEntity editor = xxx.getEditor();
        String message;
        try {
            URL xxxzzz = new URL(url);
            if (xxxzzz.getProtocol() == null || xxxzzz.getHost() == null) {
                throw new MalformedURLException("" + url + " is not valid");
            }
            message = "Sign(" + this.pos + ") text is valid url!";
        } catch (MalformedURLException e) {
            message = "Sign(" + this.pos + ") text is malformed url";
        }

        if (editor != null) {
            editor.sendMessage(new LiteralText(message), false);
        } else {
            System.out.println("WARN: SignEditor(" + xxx.getPos() + ") was null");
        }
    }


    private void println(Object x) {
        System.out.println(x);
    }

    private void println(String x) {
        System.out.println(x);
    }
}
