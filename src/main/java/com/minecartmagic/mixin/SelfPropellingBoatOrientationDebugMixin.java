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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoatEntity.class)
public abstract class SelfPropellingBoatOrientationDebugMixin {

    private static final Logger LOGGER =
            LoggerFactory.getLogger("MinecartMagic");

    private static final long DEBUG_INTERVAL = 5L;

    private long minecartmagic$lastLoggedTick =
            Long.MIN_VALUE;

    @Shadow
    public float prevYaw;

    @Inject(
            method = "getVisualRotationYInDegrees",
            at = @At("RETURN")
    )
    private void minecartmagic$debugVisualRotation(
            CallbackInfoReturnable<Float> cir
    ) {
        BoatEntity boat =
                (BoatEntity) (Object) this;

        if (!(boat instanceof SelfPropellingBoatEntity selfPropellingBoat)) {
            return;
        }

        if (!boat.getWorld().isClient()) {
            return;
        }

        long currentTick =
                boat.getWorld().getTime();

        if (currentTick - minecartmagic$lastLoggedTick < DEBUG_INTERVAL) {
            return;
        }

        minecartmagic$lastLoggedTick =
                currentTick;

        float entityYaw =
                boat.getYaw();

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

        Vec3d rotationVector =
                boat.getRotationVector(
                        boat.getPitch(),
                        visualYaw
                );

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

        boolean moving =
                horizontalSpeed > 0.00001D;

        String passengerInfo;

        if (boat.getPassengerList().isEmpty()) {
            passengerInfo = "NONE";
        } else {
            passengerInfo =
                    boat.getPassengerList()
                            .stream()
                            .map(
                                    passenger ->
                                            passenger.getType()
                                                    .toString()
                            )
                            .reduce(
                                    (a, b) -> a + "," + b
                            )
                            .orElse("UNKNOWN");
        }

        String rendererInfo =
                getRendererInfo(
                        selfPropellingBoat
                );

        LOGGER.info(
                "[SELF_BOAT_DEBUG] " +
                        "id={} tick={} " +
                        "variant={} fuel={} " +
                        "engineTailwind={} " +
                        "entityYaw={} " +
                        "prevYaw={} " +
                        "visualYaw={} " +
                        "lerpTargetYaw={} " +
                        "pitch={} " +
                        "rotationVector=({},{},{}) " +
                        "velocity=({},{},{}) " +
                        "horizontalSpeed={} " +
                        "velocityYaw={} " +
                        "movementDirection={} " +
                        "moving={} " +
                        "touchingWater={} " +
                        "centerWater={} " +
                        "belowWater={} " +
                        "centerPos={} " +
                        "passengers={} " +
                        "renderer={}",
                selfPropellingBoat.getId(),
                currentTick,

                selfPropellingBoat.getVariant(),
                selfPropellingBoat.hasFuel(),
                selfPropellingBoat.getEngineTailwindLevel(),

                format(entityYaw),
                format(prevYaw),
                format(visualYaw),
                format(lerpTargetYaw),

                format(boat.getPitch()),

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

                boat.isTouchingWater(),
                centerWater,
                belowWater,

                centerPos,
                passengerInfo,

                rendererInfo
        );
    }

    private static String getRendererInfo(
            SelfPropellingBoatEntity boat
    ) {
        try {
            MinecraftClient client =
                    MinecraftClient.getInstance();

            if (client.world == null) {
                return "CLIENT_WORLD_NULL";
            }

            return client.getEntityRenderDispatcher()
                    .getRenderer(boat)
                    .getClass()
                    .getName();

        } catch (Exception exception) {
            return "ERROR:" +
                    exception.getClass().getSimpleName();
        }
    }

    private static String format(
            double value
    ) {
        return String.format(
                "%.5f",
                value
        );
    }
}
