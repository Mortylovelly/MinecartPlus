package com.minecartmagic.client;

import net.minecraft.client.render.entity.BoatEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class SelfPropellingBoatRenderer
        extends BoatEntityRenderer {

    public SelfPropellingBoatRenderer(
            EntityRendererFactory.Context context
    ) {
        super(
                context,
                false
        );
    }
}
