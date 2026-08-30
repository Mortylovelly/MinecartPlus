package com.minecartmagic.client;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public final class SelfPropellingBoatDebug {

    private static final Logger LOGGER =
            LoggerFactory.getLogger("MinecartMagic");

    private static final int SEARCH_RADIUS = 64;

    private static final long LOG_INTERVAL = 10L;

    private static long lastLogTick = Long.MIN_VALUE;

    private SelfPropellingBoatDebug() {
    }

    public static void init() {

        ClientTickEvents.END_CLIENT_TICK.register(
                SelfPropellingBoatDebug::onClientTick
        );

        LOGGER.info(
                "[SELF_BOAT_DEBUG] Diagnostic system initialized."
        );
    }

    private static void onClientTick(
            MinecraftClient client
    ) {

        ClientWorld world =
                client.world;

        if (world == null) {
            return;
        }

        if (client.player == null) {
            return;
        }

        long tick =
                world.getTime();

        if (tick - lastLogTick < LOG_INTERVAL) {
            return;
        }

        lastLogTick =
                tick;

        /*
         * Берём лодки вокруг игрока.
         *
         * Этого достаточно для диагностики:
         * пользователь просто садится в самоходную лодку
         * или находится рядом с ней.
         */
        Box searchBox =
                client.player
                        .getBoundingBox()
                        .expand(
                                SEARCH_RADIUS
                        );

        var boats =
                world.getEntitiesByClass(
                        SelfPropellingBoatEntity.class,
                        searchBox,
                        boat -> true
                );

        for (SelfPropellingBoatEntity boat : boats) {
            logBoat(
                    boat,
                    client
            );
        }
    }

    private static void logBoat(
            SelfPropellingBoatEntity boat,
            MinecraftClient client
    ) {

        /*
         * =====================================================
         * ENTITY ROTATION
         * =====================================================
         */

        float yaw =
                boat.getYaw();

        float pitch =
                boat.getPitch();

        float lerpYaw =
                boat.getLerpTargetYaw();

        float lerpPitch =
                boat.getLerpTargetPitch();

        Vec3d rotationVector =
                boat.getRotationVector(
                        pitch,
                        yaw
                );

        /*
         * =====================================================
         * VELOCITY
         * =====================================================
         */

        Vec3d velocity =
                boat.getVelocity();

        double horizontalSpeed =
                Math.sqrt(
                        velocity.x * velocity.x
                                + velocity.z * velocity.z
                );

        double velocityYaw =
                horizontalSpeed > 0.00001D
                        ? Math.toDegrees(
                                Math.atan2(
                                        -velocity.x,
                                        velocity.z
                                )
                        )
                        : Double.NaN;

        Direction movementDirection =
                boat.getMovementDirection();

        /*
         * =====================================================
         * EXPECTED FORWARD DIRECTION
         * =====================================================
         *
         * Это ровно та формула, которую сейчас
         * использует двигатель.
         */

        double radians =
                Math.toRadians(
                        yaw
                );

        Vec3d engineForward =
                new Vec3d(
                        -Math.sin(radians),
                        0.0D,
                        Math.cos(radians)
                );

        double engineForwardYaw =
                Math.toDegrees(
                        Math.atan2(
                                -engineForward.x,
                                engineForward.z
                        )
                );

        /*
         * =====================================================
         * WATER
         * =====================================================
         */

        BlockPos centerPos =
                boat.getBlockPos();

        BlockPos belowPos =
                centerPos.down();

        FluidState centerFluid =
                boat.getWorld().getFluidState(
                        centerPos
                );

        FluidState belowFluid =
                boat.getWorld().getFluidState(
                        belowPos
                );

        boolean centerWater =
                centerFluid.isIn(
                        FluidTags.WATER
                );

        boolean belowWater =
                belowFluid.isIn(
                        FluidTags.WATER
                );

        boolean touchingWater =
                boat.isTouchingWater();

        /*
         * =====================================================
         * PASSENGERS
         * =====================================================
         */

        int passengers =
                boat.getPassengerList()
                        .size();

        boolean playerInside =
                boat.getPassengerList()
                        .contains(
                                client.player
                        );

        /*
         * =====================================================
         * ENTITY DATA
         * =====================================================
         */

        String variant =
                boat.getVariant().name();

        boolean hasFuel =
                boat.hasFuel();

        int engineTailwind =
                boat.getEngineTailwindLevel();

        double maxSpeed =
                boat.getMaximumSpeed();

        /*
         * =====================================================
         * POSITION
         * =====================================================
         */

        double x =
                boat.getX();

        double y =
                boat.getY();

        double z =
                boat.getZ();

        /*
         * =====================================================
         * BOUNDING BOX
         * =====================================================
         */

        var box =
                boat.getBoundingBox();

        double width =
                box.getLengthX();

        double height =
                box.getLengthY();

        double depth =
                box.getLengthZ();

        /*
         * =====================================================
         * RESOURCE IDS
         * =====================================================
         */

        boolean bamboo =
                boat.getVariant()
                        == net.minecraft.entity.vehicle.BoatEntity.Type.BAMBOO;

        Identifier modelId =
                bamboo
                        ? Identifier.of(
                                "minecartmagic",
                                "geo/entity/self_propeled_bamboo_raft.geo.json"
                        )
                        : Identifier.of(
                                "minecartmagic",
                                "geo/entity/self_propeled_boat.geo.json"
                        );

        Identifier animationId =
                bamboo
                        ? Identifier.of(
                                "minecartmagic",
                                "animations/entity/self_propeled_bamboo_raft.animation.json"
                        )
                        : Identifier.of(
                                "minecartmagic",
                                "animations/entity/self_propeled_boat.animation.json"
                        );

        /*
         * Текстура.
         */
        String textureName =
                switch (boat.getVariant()) {

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

        if (hasFuel) {
            textureName += "_on";
        }

        Identifier textureId =
                Identifier.of(
                        "minecartmagic",
                        "textures/entity/"
                                + textureName
                                + ".png"
                );

        /*
         * =====================================================
         * RESOURCE EXISTENCE
         * =====================================================
         */

        boolean modelExists =
                resourceExists(
                        client,
                        modelId
                );

        boolean animationExists =
                resourceExists(
                        client,
                        animationId
                );

        boolean textureExists =
                resourceExists(
                        client,
                        textureId
                );

        /*
         * =====================================================
         * RENDERER
         * =====================================================
         */

        String rendererName;

        try {

            rendererName =
                    client.getEntityRenderDispatcher()
                            .getRenderer(
                                    boat
                            )
                            .getClass()
                            .getName();

        } catch (Exception exception) {

            rendererName =
                    "ERROR:"
                            + exception
                            .getClass()
                            .getSimpleName();
        }

        /*
         * =====================================================
         * FULL LOG
         * =====================================================
         */

        LOGGER.info(
                "[SELF_BOAT_DEBUG] "
                        + "id={} "
                        + "tick={} "
                        + "variant={} "
                        + "fuel={} "
                        + "engineTailwind={} "
                        + "maxSpeed={} "
                        + "yaw={} "
                        + "lerpYaw={} "
                        + "pitch={} "
                        + "lerpPitch={} "
                        + "rotationVector=({},{},{}) "
                        + "velocity=({},{},{}) "
                        + "horizontalSpeed={} "
                        + "velocityYaw={} "
                        + "engineForwardYaw={} "
                        + "movementDirection={} "
                        + "touchingWater={} "
                        + "centerWater={} "
                        + "belowWater={} "
                        + "position=({},{},{}) "
                        + "bbox=({},{},{}) "
                        + "passengers={} "
                        + "playerInside={} "
                        + "modelExists={} "
                        + "textureExists={} "
                        + "animationExists={} "
                        + "model={} "
                        + "texture={} "
                        + "animation={} "
                        + "renderer={}",
                boat.getId(),
                boat.getWorld().getTime(),

                variant,
                hasFuel,
                engineTailwind,

                format(maxSpeed),

                format(yaw),
                format(lerpYaw),

                format(pitch),
                format(lerpPitch),

                format(rotationVector.x),
                format(rotationVector.y),
                format(rotationVector.z),

                format(velocity.x),
                format(velocity.y),
                format(velocity.z),

                format(horizontalSpeed),

                Double.isNaN(velocityYaw)
                        ? "NaN"
                        : format(velocityYaw),

                format(engineForwardYaw),

                movementDirection,

                touchingWater,
                centerWater,
                belowWater,

                format(x),
                format(y),
                format(z),

                format(width),
                format(height),
                format(depth),

                passengers,
                playerInside,

                modelExists,
                textureExists,
                animationExists,

                modelId,
                textureId,
                animationId,

                rendererName
        );
    }

    private static boolean resourceExists(
            MinecraftClient client,
            Identifier identifier
    ) {

        try {

            return client.getResourceManager()
                    .getResource(
                            identifier
                    )
                    .isPresent();

        } catch (Exception exception) {

            return false;
        }
    }

    private static String format(
            double value
    ) {

        return String.format(
                Locale.ROOT,
                "%.5f",
                value
        );
    }
}
