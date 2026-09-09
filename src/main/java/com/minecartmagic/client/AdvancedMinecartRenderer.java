package com.minecartmagic.client;

import com.minecartmagic.entity.AdvancedMinecartEntity;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.RailShape;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AdvancedMinecartRenderer
        extends GeoEntityRenderer<AdvancedMinecartEntity> {

    public AdvancedMinecartRenderer(
            EntityRendererFactory.Context context
    ) {
        super(context, new AdvancedMinecartModel());
        shadowRadius = 0.6F;
    }

    @Override
    protected void applyRotations(
            AdvancedMinecartEntity entity,
            MatrixStack matrices,
            float ageInTicks,
            float rotationYaw,
            float partialTick,
            float nativeScale
    ) {
        float yaw = getRailAlignedYaw(entity, partialTick);
        float pitch = getRailPitch(entity);

        matrices.multiply(
                RotationAxis.POSITIVE_Y.rotationDegrees(yaw)
        );

        matrices.multiply(
                RotationAxis.POSITIVE_X.rotationDegrees(pitch)
        );
    }

    private static float getRailAlignedYaw(
            AdvancedMinecartEntity entity,
            float partialTick
    ) {
        Vec3d velocity = entity.getVelocity();
        double horizontalSpeedSquared =
                velocity.x * velocity.x + velocity.z * velocity.z;

        if (horizontalSpeedSquared > 1.0E-8D) {
            /*
             * The previous renderer applied an additional -90 degree
             * correction here. That was the reason the already-correct
             * rail movement was rendered with the model lying sideways.
             * The travel direction itself is the required model yaw.
             */
            return (float) Math.toDegrees(
                    Math.atan2(-velocity.x, velocity.z)
            );
        }

        BlockState state = entity.getWorld().getBlockState(
                entity.getBlockPos()
        );

        if (state.getBlock() instanceof AbstractRailBlock railBlock) {
            RailShape shape = state.get(
                    railBlock.getShapeProperty()
            );

            return switch (shape) {
                case EAST_WEST,
                        ASCENDING_EAST,
                        ASCENDING_WEST -> 90.0F;

                case NORTH_SOUTH,
                        ASCENDING_NORTH,
                        ASCENDING_SOUTH -> 0.0F;

                case SOUTH_EAST -> -45.0F;
                case SOUTH_WEST -> 45.0F;
                case NORTH_EAST -> -135.0F;
                case NORTH_WEST -> 135.0F;
            };
        }

        return MathHelper.lerpAngleDegrees(
                partialTick,
                entity.prevYaw,
                entity.getYaw()
        );
    }

    private static float getRailPitch(
            AdvancedMinecartEntity entity
    ) {
        BlockState state = entity.getWorld().getBlockState(
                entity.getBlockPos()
        );

        if (!(state.getBlock() instanceof AbstractRailBlock railBlock)) {
            return 0.0F;
        }

        RailShape shape = state.get(
                railBlock.getShapeProperty()
        );

        Vec3d velocity = entity.getVelocity();
        boolean uphill;

        switch (shape) {
            case ASCENDING_EAST -> uphill = velocity.x >= 0.0D;
            case ASCENDING_WEST -> uphill = velocity.x <= 0.0D;
            case ASCENDING_NORTH -> uphill = velocity.z <= 0.0D;
            case ASCENDING_SOUTH -> uphill = velocity.z >= 0.0D;
            default -> {
                return 0.0F;
            }
        }

        return uphill ? -45.0F : 45.0F;
    }
}
