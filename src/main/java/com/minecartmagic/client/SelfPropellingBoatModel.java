package com.minecartmagic.client;

import com.minecartmagic.MinecartMagicMod;
import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    private static final Identifier BAMBOO_ANIMATIONS =
            MinecartMagicMod.id(
                    "animations/entity/self_propeled_bamboo_raft.animation.json"
            );

    private static final Map<Integer, Long> LAST_DEBUG_LOG_TICK =
            new HashMap<>();

    private static final Map<Integer, String> LAST_MODEL_LOG =
            new HashMap<>();

    private static final Map<Integer, String> LAST_TEXTURE_LOG =
            new HashMap<>();

    private static final Map<Integer, String> LAST_ANIMATION_LOG =
            new HashMap<>();

    private static final long DEBUG_INTERVAL =
            10L;

    @Override
    public Identifier getModelResource(
            SelfPropellingBoatEntity animatable
    ) {
        Identifier model =
                animatable.getVariant()
                        == BoatEntity.Type.BAMBOO
                        ? BAMBOO_MODEL
                        : WOOD_MODEL;

        logModel(
                animatable,
                model
        );

        return model;
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

        Identifier texture =
                MinecartMagicMod.id(
                        "textures/entity/"
                                + name
                                + suffix
                                + ".png"
                );

        logTexture(
                animatable,
                texture
        );

        return texture;
    }

    @Override
    public Identifier getAnimationResource(
            SelfPropellingBoatEntity animatable
    ) {
        Identifier animation =
                animatable.getVariant()
                        == BoatEntity.Type.BAMBOO
                        ? BAMBOO_ANIMATIONS
                        : WOOD_ANIMATIONS;

        logAnimation(
                animatable,
                animation
        );

        return animation;
    }

    /*
     * =====================================================
     * VANILLA PADDLE ANIMATION
     * =====================================================
     *
     * Здесь НЕ задаём абсолютное положение костей.
     *
     * GeckoLib уже загрузил исходные rotations
     * непосредственно из geo.json.
     *
     * Мы только добавляем к ним относительное
     * движение ванильного весла.
     */
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
                        .getBone(
                                "paddle_left"
                        );

        GeoBone rightPaddle =
                getAnimationProcessor()
                        .getBone(
                                "paddle_right"
                        );

        if (leftPaddle == null
                || rightPaddle == null) {
            return;
        }

        float tickDelta =
                animationState.getPartialTick();

        animateVanillaPaddle(
                animatable,
                0,
                leftPaddle,
                tickDelta
        );

        animateVanillaPaddle(
                animatable,
                1,
                rightPaddle,
                tickDelta
        );
    }

    private static void animateVanillaPaddle(
            SelfPropellingBoatEntity boat,
            int side,
            GeoBone paddle,
            float tickDelta
    ) {
        /*
         * Если ванильное весло сейчас не гребёт,
         * вообще ничего не меняем.
         *
         * Благодаря этому GeckoLib оставляет
         * ровно ту исходную позу, которую задал
         * Blockbench в geo.json.
         */
        if (!boat.isPaddleMoving(side)) {
            return;
        }

        /*
         * Настоящая фаза ванильного BoatEntity.
         */
        float paddlePhase =
                boat.interpolatePaddlePhase(
                        side,
                        tickDelta
                );

        /*
         * Это диапазоны, используемые vanilla
         * BoatModel.animatePaddle().
         *
         * X:
         * -60° .. -15°
         *
         * Y:
         * -45° .. +45°
         */
        float vanillaX =
                MathHelper.clampedLerp(
                        -1.0471976F,
                        -0.2617994F,
                        (
                                MathHelper.sin(
                                        -paddlePhase
                                ) + 1.0F
                        ) / 2.0F
                );

        float vanillaY =
                MathHelper.clampedLerp(
                        -0.7853982F,
                        0.7853982F,
                        (
                                MathHelper.sin(
                                        -paddlePhase + 1.0F
                                ) + 1.0F
                        ) / 2.0F
                );

        /*
         * Состояние покоя ванильного весла:
         *
         * X = -45°
         * Y = 0°
         */
        float deltaX =
                vanillaX + 0.7853982F;

        float deltaY =
                vanillaY;

        /*
         * ВАЖНО:
         *
         * Не задаём setRotX(vanillaX).
         * Не задаём setRotY(vanillaY).
         *
         * Добавляем только delta к тому,
         * что уже находится на GeoBone.
         *
         * Поэтому pivot и положение весла,
         * созданные в Blockbench, не меняются.
         */
        paddle.setRotX(
                paddle.getRotX()
                        + deltaX
        );

        paddle.setRotY(
                paddle.getRotY()
                        + deltaY
        );
    }

    private static void logModel(
            SelfPropellingBoatEntity boat,
            Identifier model
    ) {
        if (!boat.getWorld().isClient()) {
            return;
        }

        long tick =
                boat.getWorld().getTime();

        int id =
                boat.getId();

        Long lastTick =
                LAST_DEBUG_LOG_TICK.get(id);

        if (lastTick != null
                && tick - lastTick < DEBUG_INTERVAL) {
            return;
        }

        LAST_DEBUG_LOG_TICK.put(
                id,
                tick
        );

        boolean exists =
                resourceExists(model);

        String state =
                model + "|exists=" + exists;

        if (!state.equals(
                LAST_MODEL_LOG.get(id)
        )) {

            LAST_MODEL_LOG.put(
                    id,
                    state
            );

            MinecartMagicMod.LOGGER.info(
                    "[SELF_BOAT_MODEL_DEBUG] " +
                            "id={} variant={} model={} exists={}",
                    id,
                    boat.getVariant(),
                    model,
                    exists
            );
        }
    }

    private static void logTexture(
            SelfPropellingBoatEntity boat,
            Identifier texture
    ) {
        if (!boat.getWorld().isClient()) {
            return;
        }

        int id =
                boat.getId();

        boolean exists =
                resourceExists(texture);

        String state =
                texture + "|exists=" + exists;

        if (!state.equals(
                LAST_TEXTURE_LOG.get(id)
        )) {

            LAST_TEXTURE_LOG.put(
                    id,
                    state
            );

            MinecartMagicMod.LOGGER.info(
                    "[SELF_BOAT_TEXTURE_DEBUG] " +
                            "id={} variant={} fuel={} texture={} exists={}",
                    id,
                    boat.getVariant(),
                    boat.hasFuel(),
                    texture,
                    exists
            );
        }
    }

    private static void logAnimation(
            SelfPropellingBoatEntity boat,
            Identifier animation
    ) {
        if (!boat.getWorld().isClient()) {
            return;
        }

        int id =
                boat.getId();

        boolean exists =
                resourceExists(animation);

        String state =
                animation + "|exists=" + exists;

        if (!state.equals(
                LAST_ANIMATION_LOG.get(id)
        )) {

            LAST_ANIMATION_LOG.put(
                    id,
                    state
            );

            MinecartMagicMod.LOGGER.info(
                    "[SELF_BOAT_ANIMATION_DEBUG] " +
                            "id={} variant={} animation={} exists={} " +
                            "controllers=NONE_REGISTERED",
                    id,
                    boat.getVariant(),
                    animation,
                    exists
            );
        }
    }

    private static boolean resourceExists(
            Identifier identifier
    ) {
        try {
            ResourceManager resourceManager =
                    MinecraftClient
                            .getInstance()
                            .getResourceManager();

            Optional<?> resource =
                    resourceManager.getResource(
                            identifier
                    );

            return resource.isPresent();

        } catch (Exception exception) {
            return false;
        }
    }
}
