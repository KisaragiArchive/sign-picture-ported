package net.fabricmc.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;

public class KeyBindingConfig implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SignPictureReloaded.LOGGER.info("Key config...");
        KeyBinding openGUI = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + SignPictureReloaded.MOD_ID + ".gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_DECIMAL,
                "category." + SignPictureReloaded.MOD_ID + ".gui"
        ));

        ClientTickCallback.EVENT.register(client -> {
            while (openGUI.wasPressed()) {
                client.player.sendMessage(new LiteralText("Key 1 was pressed!"), false);
            }
        });
        SignPictureReloaded.LOGGER.info("successful");
    }
}
