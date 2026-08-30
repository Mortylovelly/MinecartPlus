package com.minecartmagic.client;

import com.minecartmagic.ModEntities;
import com.minecartmagic.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class MinecartMagicClient
        implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        EntityRendererRegistry.register(
                ModEntities.SELF_PROPELLING_BOAT,
                SelfPropellingBoatRenderer::new
        );

        HandledScreens.register(
                ModScreenHandlers.SELF_PROPELLING_BOAT,
                SelfPropellingBoatScreen::new
        );
    }
}
