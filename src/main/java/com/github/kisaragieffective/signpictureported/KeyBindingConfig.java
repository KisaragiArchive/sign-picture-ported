package com.github.kisaragieffective.signpictureported;

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
        SignPicturePorted.LOGGER.info("Key config...");
        KeyBinding openGUI = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + SignPicturePorted.MOD_ID + ".gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_DECIMAL,
                "category." + SignPicturePorted.MOD_ID + ".gui"
        ));

        ClientTickCallback.EVENT.register(client -> {
            while (openGUI.wasPressed()) {
                client.player.sendMessage(new LiteralText("Key 1 was pressed!"), false);
            }
        });
        SignPicturePorted.LOGGER.info("successful");
    }
}
