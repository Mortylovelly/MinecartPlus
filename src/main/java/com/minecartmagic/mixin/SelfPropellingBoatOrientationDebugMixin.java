package com.minecartmagic.mixin;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mixin(BoatEntity.class)
public abstract class SelfPropellingBoatOrientationDebugMixin {

    private static final Logger LOGGER =
            LoggerFactory.getLogger("MinecartMagic");

    private long minecartmagic$lastLoggedTick = Long.MIN_VALUE;

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
         * Логируем только нашу самоходную лодку.
         */
        if (!(boat instanceof SelfPropellingBoatEntity selfPropellingBoat)) {
            return;
        }

        /*
         * Только клиент.
         *
         * Именно клиентский visual yaw интересует
         * GeckoLib при рендере.
         */
        if (!boat.getWorld().isClient()) {
            return;
        }

        long tick =
                boat.getWorld().getTime();

        /*
         * Не спамим консоль каждый вызов рендера.
         *
         * Один лог примерно раз в 10 тиков.
         */
        if (tick == minecartmagic$lastLoggedTick
                || tick - minecartmagic$lastLoggedTick < 10) {
            return;
        }

        minecartmagic$lastLoggedTick = tick;

        float entityYaw =
                boat.getYaw();

        float previousYaw =
                boat.prevYaw;

        float visualYaw =
                cir.getReturnValue();

        float lerpTargetYaw =
                boat.getLerpTargetYaw();

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

        LOGGER.info(
                "[SELF_BOAT_ORIENTATION] " +
                        "id={} tick={} " +
                        "entityYaw={} " +
                        "previousYaw={} " +
                        "visualYaw={} " +
                        "lerpTargetYaw={} " +
                        "velocity=({},{},{}) " +
                        "horizontalSpeed={} " +
                        "velocityYaw={} " +
                        "movementDirection={} " +
                        "variant={} " +
                        "fuel={}",
                selfPropellingBoat.getId(),
                tick,

                String.format(
                        "%.3f",
                        entityYaw
                ),

                String.format(
                        "%.3f",
                        previousYaw
                ),

                String.format(
                        "%.3f",
                        visualYaw
                ),

                String.format(
                        "%.3f",
                        lerpTargetYaw
                ),

                String.format(
                        "%.5f",
                        velocity.x
                ),

                String.format(
                        "%.5f",
                        velocity.y
                ),

                String.format(
                        "%.5f",
                        velocity.z
                ),

                String.format(
                        "%.5f",
                        horizontalSpeed
                ),

                Double.isNaN(velocityYaw)
                        ? "NaN"
                        : String.format(
                                "%.3f",
                                velocityYaw
                        ),

                movementDirection,

                selfPropellingBoat.getVariant(),

                selfPropellingBoat.hasFuel()
        );
    }
}
