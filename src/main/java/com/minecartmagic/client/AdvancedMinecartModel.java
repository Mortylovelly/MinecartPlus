package com.minecartmagic.client;

import com.minecartmagic.MinecartMagicMod;
import com.minecartmagic.entity.AdvancedMinecartEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class AdvancedMinecartModel extends GeoModel<AdvancedMinecartEntity> {

    private static final Identifier MODEL =
            MinecartMagicMod.id(
                    "geo/entity/advanced_minecart.geo.json"
            );

    private static final Identifier TEXTURE_OFF =
            MinecartMagicMod.id(
                    "textures/entity/advanced_minecart.png"
            );

    private static final Identifier TEXTURE_ON =
            MinecartMagicMod.id(
                    "textures/entity/advanced_minecart_on.png"
            );

    private static final Identifier ANIMATIONS =
            MinecartMagicMod.id(
                    "animations/entity/advanced_minecart.animation.json"
            );

    @Override
    public Identifier getModelResource(
            AdvancedMinecartEntity animatable
    ) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(
            AdvancedMinecartEntity animatable
    ) {
        return animatable.getVelocity().horizontalLengthSquared() > 0.0001D
                ? TEXTURE_ON
                : TEXTURE_OFF;
    }

    @Override
    public Identifier getAnimationResource(
            AdvancedMinecartEntity animatable
    ) {
        return ANIMATIONS;
    }
}
