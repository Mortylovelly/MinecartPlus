package com.minecartmagic.client;

import com.minecartmagic.MinecartMagicMod;
import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
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

    private static final Identifier WOOD_ANIMATIONS =
            MinecartMagicMod.id(
                    "animations/entity/self_propeled_boat.animation.json"
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
                            "self_propelled_bamboo_raft";

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
         * Вёсла анимируются напрямую через
         * setCustomAnimations(), поэтому отдельный
         * animation.json для бамбука здесь не нужен.
         */
        return WOOD_ANIMATIONS;
    }

    @Override
    public void setCustomAnimations(
            SelfPropellingBoatEntity animatable,
            long instanceId,
            AnimationState<SelfPropellingBoatEntity> animationState
    ) {
        super.setCustomAnimations(
                animatable,
                instanceId,
                animationState
        );

        GeoBone leftPaddle =
                getAnimationProcessor()
                        .getBone("paddle_left");

        GeoBone rightPaddle =
                getAnimationProcessor()
                        .getBone("paddle_right");

        if (leftPaddle == null
                || rightPaddle == null) {
            return;
        }

        float tickDelta =
                animationState.getPartialTick();

        float leftPhase =
                animatable.interpolatePaddlePhase(
                        0,
                        tickDelta
                );

        float rightPhase =
                animatable.interpolatePaddlePhase(
                        1,
                        tickDelta
                );

        float leftSin =
                MathHelper.sin(-leftPhase);

        float rightSin =
                MathHelper.sin(-rightPhase);

        float leftT =
                (leftSin + 1.0F) * 0.5F;

        float rightT =
                (rightSin + 1.0F) * 0.5F;

        /*
         * Направление pitch инвертировано относительно
         * предыдущей версии, чтобы движение весла
         * происходило в правильную сторону.
         */
        float leftPitch =
                MathHelper.lerp(
                        leftT,
                        1.0471976F,
                        0.2617994F
                );

        float rightPitch =
                MathHelper.lerp(
                        rightT,
                        1.0471976F,
                        0.2617994F
                );

        /*
         * Левая и правая стороны вращаются зеркально.
         */
        float leftYaw =
                MathHelper.lerp(
                        leftT,
                        -0.7853982F,
                        0.7853982F
                );

        float rightYaw =
                MathHelper.lerp(
                        rightT,
                        0.7853982F,
                        -0.7853982F
                );

        leftPaddle.setRotX(leftPitch);
        leftPaddle.setRotY(leftYaw);

        rightPaddle.setRotX(rightPitch);
        rightPaddle.setRotY(rightYaw);
    }
}
