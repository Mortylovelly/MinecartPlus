package com.minecartmagic;

import com.minecartmagic.network.ModNetworking;
import com.minecartmagic.screen.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinecartMagicMod implements ModInitializer {

    public static final String MOD_ID = "minecartmagic";

    public static final Logger LOGGER =
            LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {

        MinecartMagicAttachments.init();

        ModEntities.init();

        ModItems.init();

        ModEnchantments.init();

        /*
         * Не удалять.
         */
        MinecartPlacementHandler.init();

        ModScreenHandlers.init();

        ModNetworking.init();

        LOGGER.info(
                "Minecart Magic loaded!"
        );
    }

    public static Identifier id(
            String path
    ) {
        return Identifier.of(
                MOD_ID,
                path
        );
    }
}
