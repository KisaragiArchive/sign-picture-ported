package com.github.kisaragieffective.signpictureported;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SignPicturePorted implements ModInitializer, ClientModInitializer, DedicatedServerModInitializer {
    public static final String MOD_ID = "signpictureported";
    public static final Logger LOGGER = LogManager.getLogger();
    @Override
    public void onInitialize() {
        LOGGER.info("SignPictureReloaded was loaded");
        LOGGER.info("version: " + FabricLoader.getInstance().getModContainer(MOD_ID).map(x -> x.getMetadata().getVersion().getFriendlyString()).orElse("<unknown>"));
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Installation detected on client side");
    }

    @Override
    public void onInitializeServer() {
        LOGGER.warn("This mod doesn't anything for server side. You may remove this mod from server's folder.");
    }
}
