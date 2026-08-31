package com.minecartmagic.client;

import com.minecartmagic.MinecartMagicMod;
import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.resource.ResourceManager;
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
                            "self_propelled_bamboo_raft";

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
                WOOD_ANIMATIONS;

        logAnimation(
                animatable,
                animation
        );

        return animation;
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
