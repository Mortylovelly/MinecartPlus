package com.minecartmagic;

import net.minecraft.util.Identifier;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinecartMagicMod implements ModInitializer {

    public static final String MOD_ID = "minecartmagic";

    public static final Logger LOGGER =
            LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        MinecartMagicAttachments.init();
        ModEnchantments.init();

        // Возвращаем рабочую установку зачарованных вагонеток.
        MinecartPlacementHandler.init();

        LOGGER.info("Minecart Magic loaded!");
    }

    public static Identifier id(String path) {
        return Identifier.of(
                MOD_ID,
                path
        );
    }
}
