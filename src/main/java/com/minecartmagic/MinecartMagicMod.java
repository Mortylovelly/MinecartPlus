package com.minecartmagic;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinecartMagicMod implements ModInitializer {
    public static final String MOD_ID = "minecartmagic";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Minecart Magic loaded!");
    }
}
