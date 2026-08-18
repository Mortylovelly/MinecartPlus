package com.minecartmagic.client;

import com.minecartmagic.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class MinecartMagicClient
        implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        EntityRendererRegistry.register(
                ModEntities.SELF_PROPELLING_BOAT,
                SelfPropellingBoatRenderer::new
        );
    }
}
