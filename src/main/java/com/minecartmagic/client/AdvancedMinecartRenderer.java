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

public class AdvancedMinecartRenderer extends GeoEntityRenderer<AdvancedMinecartEntity> {

    public AdvancedMinecartRenderer(EntityRendererFactory.Context context) {
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

        // The model's long axis is aligned with the rail line. Using the
        // horizontal travel vector (rather than the minecart body's yaw)
        // prevents the model from snapping sideways when vanilla minecart
        // rotation changes during movement and turns.
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
            float travelYaw = (float) Math.toDegrees(
                    Math.atan2(-velocity.x, velocity.z)
            );

            // Current model orientation is one quarter-turn behind the
            // vanilla entity yaw convention. Subtracting 90° converts the
            // travel direction into the model's rail-axis orientation.
            return travelYaw - 90.0F;
        }

        BlockState state = entity.getWorld().getBlockState(entity.getBlockPos());
        if (state.getBlock() instanceof AbstractRailBlock railBlock) {
            RailShape shape = state.get(railBlock.getShapeProperty());
            return switch (shape) {
                case EAST_WEST,
                        ASCENDING_EAST,
                        ASCENDING_WEST -> 0.0F;
                case NORTH_SOUTH,
                        ASCENDING_NORTH,
                        ASCENDING_SOUTH -> -90.0F;
                case SOUTH_EAST -> -135.0F;
                case SOUTH_WEST -> -45.0F;
                case NORTH_EAST -> 135.0F;
                case NORTH_WEST -> 45.0F;
            };
        }

        return MathHelper.lerpAngleDegrees(
                partialTick,
                entity.prevYaw,
                entity.getYaw()
        );
    }

    private static float getRailPitch(AdvancedMinecartEntity entity) {
        BlockState state = entity.getWorld().getBlockState(entity.getBlockPos());

        if (!(state.getBlock() instanceof AbstractRailBlock railBlock)) {
            return 0.0F;
        }

        RailShape shape = state.get(railBlock.getShapeProperty());
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
