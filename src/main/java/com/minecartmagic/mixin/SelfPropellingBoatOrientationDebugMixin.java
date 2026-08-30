package com.minecartmagic.mixin;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;

@Mixin(BoatEntity.class)
public abstract class SelfPropellingBoatOrientationDebugMixin {

    private static final Logger LOGGER =
            LoggerFactory.getLogger("MinecartMagic");

    private static final long DEBUG_INTERVAL =
            5L;

    private long minecartmagic$lastLoggedTick =
            Long.MIN_VALUE;

    @Inject(
            method = "getVisualRotationYInDegrees",
            at = @At("RETURN")
    )
    private void minecartmagic$debugVisualRotation(
            CallbackInfoReturnable<Float> cir
    ) {
        BoatEntity boat =
                (BoatEntity) (Object) this;

        /*
         * Только наша самоходная лодка.
         */
        if (!(boat instanceof SelfPropellingBoatEntity selfPropellingBoat)) {
            return;
        }

        /*
         * Нам нужна именно клиентская информация,
         * которую получает renderer.
         */
        if (!boat.getWorld().isClient()) {
            return;
        }

        long currentTick =
                boat.getWorld().getTime();

        /*
         * Не спамим лог каждый вызов рендера.
         */
        if (currentTick - minecartmagic$lastLoggedTick
                < DEBUG_INTERVAL) {
            return;
        }

        minecartmagic$lastLoggedTick =
                currentTick;

        /*
         * =====================================================
         * ROTATION
         * =====================================================
         */

        float entityYaw =
                boat.getYaw();

        float visualYaw =
                cir.getReturnValue();

        float lerpTargetYaw =
                boat.getLerpTargetYaw();

        float pitch =
                boat.getPitch();

        Vec3d rotationVector =
                boat.getRotationVector(
                        pitch,
                        visualYaw
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
         * MOVEMENT / PASSENGERS
         * =====================================================
         */

        boolean moving =
                horizontalSpeed > 0.00001D;

        String passengers;

        if (boat.getPassengerList().isEmpty()) {
            passengers = "NONE";
        } else {
            passengers =
                    boat.getPassengerList()
                            .stream()
                            .map(
                                    passenger ->
                                            passenger.getType()
                                                    .toString()
                            )
                            .reduce(
                                    (first, second) ->
                                            first + "," + second
                            )
                            .orElse(
                                    "UNKNOWN"
                            );
        }

        /*
         * =====================================================
         * RENDERER
         * =====================================================
         */

        String renderer;

        try {
            MinecraftClient client =
                    MinecraftClient.getInstance();

            if (client.world == null) {
                renderer =
                        "CLIENT_WORLD_NULL";
            } else {
                renderer =
                        client.getEntityRenderDispatcher()
                                .getRenderer(
                                        boat
                                )
                                .getClass()
                                .getName();
            }

        } catch (Exception exception) {
            renderer =
                    "ERROR:"
                            + exception
                            .getClass()
                            .getSimpleName();
        }

        /*
         * =====================================================
         * FULL DEBUG LOG
         * =====================================================
         */

        LOGGER.info(
                "[SELF_BOAT_DEBUG] "
                        + "id={} "
                        + "tick={} "
                        + "variant={} "
                        + "fuel={} "
                        + "engineTailwind={} "
                        + "entityYaw={} "
                        + "visualYaw={} "
                        + "lerpTargetYaw={} "
                        + "pitch={} "
                        + "rotationVector=({},{},{}) "
                        + "velocity=({},{},{}) "
                        + "horizontalSpeed={} "
                        + "velocityYaw={} "
                        + "movementDirection={} "
                        + "moving={} "
                        + "touchingWater={} "
                        + "centerWater={} "
                        + "belowWater={} "
                        + "pos=({},{},{}) "
                        + "centerBlock={} "
                        + "passengers={} "
                        + "renderer={}",
                selfPropellingBoat.getId(),
                currentTick,

                selfPropellingBoat.getVariant(),
                selfPropellingBoat.hasFuel(),
                selfPropellingBoat.getEngineTailwindLevel(),

                format(entityYaw),
                format(visualYaw),
                format(lerpTargetYaw),

                format(pitch),

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

                movementDirection,
                moving,

                touchingWater,
                centerWater,
                belowWater,

                format(boat.getX()),
                format(boat.getY()),
                format(boat.getZ()),

                centerPos,
                passengers,
                renderer
        );
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
