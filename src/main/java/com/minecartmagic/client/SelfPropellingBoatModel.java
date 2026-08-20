package com.minecartmagic.client;

import com.minecartmagic.MinecartMagicMod;
import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class SelfPropellingBoatModel
        extends GeoModel<SelfPropellingBoatEntity> {

    private static final Identifier WOOD_MODEL =
            MinecartMagicMod.id(
                    "geo/entity/self_propeled_boat.geo.json"
            );

    private static final Identifier BAMBOO_MODEL =
            MinecartMagicMod.id(
                    "geo/entity/self_propeled_bamboo_raft.geo.json"
            );

    @Override
    public Identifier getModelResource(
            SelfPropellingBoatEntity animatable
    ) {
        return animatable.getVariant()
                == BoatEntity.Type.BAMBOO
                ? BAMBOO_MODEL
                : WOOD_MODEL;
    }

    @Override
    public Identifier getTextureResource(
            SelfPropellingBoatEntity animatable
    ) {
        BoatEntity.Type type =
                animatable.getVariant();

        String suffix =
                animatable.hasFuel()
                        ? "_on"
                        : "";

        String name =
                switch (type) {
                    case SPRUCE ->
                            "self_propeled_spruce_boat";

                    case BIRCH ->
                            "self_propeled_birch_boat";

                    case JUNGLE ->
                            "self_propeled_jungle_boat";

                    case ACACIA ->
                            "self_propeled_acacia_boat";

                    case DARK_OAK ->
                            "self_propeled_dark_oak_boat";

                    case MANGROVE ->
                            "self_propeled_mangrove_boat";

                    case CHERRY ->
                            "self_propeled_cherry_boat";

                    case BAMBOO ->
                            "self_propeled_bamboo_raft";

                    case OAK ->
                            "self_propeled_oak_boat";
                };

        return MinecartMagicMod.id(
                "textures/entity/"
                        + name
                        + suffix
                        + ".png"
        );
    }

    @Override
    public Identifier getAnimationResource(
            SelfPropellingBoatEntity animatable
    ) {
        /*
         * Анимаций пока нет.
         * GeckoLib требует этот метод,
         * поэтому позже сюда подключим настоящие
         * animation.json из Blockbench.
         *
         * Пока используется ресурс-заглушка.
         */
        return MinecartMagicMod.id(
                "animations/entity/self_propeled_boat.animation.json"
        );
    }
}
