package com.minecartmagic.client;

import com.minecartmagic.ModEntities;
import com.minecartmagic.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class MinecartMagicClient
        implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        /*
         * Самоходная лодка.
         */
        EntityRendererRegistry.register(
                ModEntities.SELF_PROPELLING_BOAT,
                SelfPropellingBoatRenderer::new
        );

        /*
         * Fuel GUI.
         */
        HandledScreens.register(
                ModScreenHandlers.SELF_PROPELLING_BOAT,
                SelfPropellingBoatScreen::new
        );
    }
}
