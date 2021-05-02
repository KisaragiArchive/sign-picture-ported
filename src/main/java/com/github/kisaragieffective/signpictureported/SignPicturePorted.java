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
    public static final boolean DEBUG = false;

    static {
        if (DEBUG) {
            LOGGER.info("Hint: This build is DEBUG build.");
            LOGGER.debug("If you can see this message, it's ok.");
            LOGGER.info("Or else: logger can't log debug message.");
        }
    }

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
