package com.github.kisaragieffective.signpictureported.mixin;

import com.github.kisaragieffective.signpictureported.SignPicturePorted;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.impl.client.rendering.BuiltinItemRendererRegistryImpl;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(BuiltinItemRendererRegistryImpl.class)
public abstract class BIRRMixin {
    // TODO: Override looks in inventory, arms, and on players' head (which interpreted as helmet)
    @Shadow @Final
    @Mutable
    private static Map<Item, BuiltinItemRendererRegistry.DynamicItemRenderer> RENDERERS;

    @Shadow public abstract void register(ItemConvertible item, BuiltinItemRendererRegistry.DynamicItemRenderer renderer);

    @Inject(at = @At("TAIL"), method = "<init>")
    private void initializerTail(CallbackInfo ci) {
        // todo not working.
        SignPicturePorted.LOGGER.info("Start overriding vanilla sign renderers...");
        Set<Item> signItems = Stream.of(
                Blocks.ACACIA_SIGN,
                Blocks.BIRCH_SIGN,
                Blocks.CRIMSON_SIGN,
                Blocks.DARK_OAK_SIGN,
                Blocks.SPRUCE_SIGN,
                Blocks.JUNGLE_SIGN,
                Blocks.OAK_SIGN,
                Blocks.WARPED_SIGN
        )
                .map(Block::asItem)
                .collect(Collectors.toSet());
        Map<Item, BuiltinItemRendererRegistry.DynamicItemRenderer> defaultRenderers = RENDERERS;
        // override
        if (defaultRenderers == null) {
            defaultRenderers = new HashMap<>();
        }
        RENDERERS = defaultRenderers;

        final Map<Item, BuiltinItemRendererRegistry.DynamicItemRenderer> defRenderer = defaultRenderers;
        signItems.forEach(sign -> {
            @Nullable
            BuiltinItemRendererRegistry.DynamicItemRenderer defaultRenderer = defRenderer.get(sign);
            BuiltinItemRendererRegistry.DynamicItemRenderer customRenderer = getRenderer(defaultRenderer);
            SignPicturePorted.LOGGER.info("Overriding renderer: for:" + sign + ",render class:" + defaultRenderer);
            // defRenderer.remove(sign);
            this.register(
                    Objects.requireNonNull(sign, "sign"),
                    Objects.requireNonNull(customRenderer, "customRender")
            );
        });
        RENDERERS = defRenderer;
        SignPicturePorted.LOGGER.info("Done");
    }

    @NotNull
    @Contract("_->new")
    private BuiltinItemRendererRegistry.DynamicItemRenderer getRenderer(BuiltinItemRendererRegistry.DynamicItemRenderer defaultRenderer) {
        SignPicturePorted.LOGGER.info("called getRenderer in BIRRMixin");
        return (itemStack, mode, ms, vcp, i, i1) -> {
            SignPicturePorted.LOGGER.warn("Hi from BuiltinItemRendererRegistry.DynamicItemRenderer");
            CompoundTag tag = itemStack.getTag();
            if ((tag == null || !tag.contains("line1") || itemStack.isInFrame()) && defaultRenderer != null) {
                // default to built-in renderer
                defaultRenderer.render(itemStack, ModelTransformation.Mode.GUI, ms, vcp, i, i1);
            }
            SignPicturePorted.LOGGER.info("Hey! Handle This case!\ntag:" + tag + "\nitemstack:" + itemStack + "\nmode:" + mode);
            // DO NOTHING, SO WE CAN SEE NOTHING?
        };
    }
}
