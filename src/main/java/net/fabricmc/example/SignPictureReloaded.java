package net.fabricmc.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.impl.client.rendering.BuiltinItemRendererRegistryImpl;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SignPictureReloaded implements ModInitializer, ClientModInitializer {
    public static final String MOD_ID = "modid";
    public static final Logger LOGGER = LogManager.getLogger();
    @Override
    public void onInitialize() {
        LogManager.getLogger().info("ExampleMod Loaded");
    }

    public void overrideDefaultSignItemRenderers() {
        BuiltinItemRendererRegistry birr = BuiltinItemRendererRegistry.INSTANCE;
        try {
            Field defaultRendererField = BuiltinItemRendererRegistryImpl.class.getDeclaredField("RENDERERS");
            @SuppressWarnings("unchecked")
            Map<Item, BuiltinItemRendererRegistry.DynamicItemRenderer> defaultRenderers = (Map<Item, BuiltinItemRendererRegistry.DynamicItemRenderer>)
                    defaultRendererField.get(/* IT'S STATIC */ null);
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
            signItems.forEach(sign -> {
                BuiltinItemRendererRegistry.DynamicItemRenderer defaultRenderer = defaultRenderers.get(sign);
                defaultRenderers.remove(sign);
                birr.register(sign, (itemStack, mode, matrixStack, vertexConsumerProvider, i, i1) -> {
                    CompoundTag tag = itemStack.getTag();
                    if (tag == null || !tag.contains("line1") || itemStack.isInFrame()) {
                        // default to built-in renderer
                        defaultRenderer.render(itemStack, ModelTransformation.Mode.GUI, matrixStack, vertexConsumerProvider, i, i1);
                    }
                    // DO NOTHING, SO WE CAN SEE NOTHING.
                });

            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onInitializeClient() {
        overrideDefaultSignItemRenderers();
    }
}
