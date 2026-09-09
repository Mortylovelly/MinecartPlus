package com.minecartmagic.client;

import com.minecartmagic.entity.AdvancedMinecartEntity;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.RailShape;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
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
        float yaw = MathHelper.wrapDegrees(
                entity.getPlacementYaw() + 90.0F
        );
        float pitch = getRailPitch(entity);

        /*
         * The 90 degree offset converts the minecart/entity yaw convention
         * into the local forward axis of the GeckoLib model.
         *
         * We intentionally do NOT derive yaw from velocity. Vanilla minecart
         * physics may reverse the entity's movement yaw while following a
         * rail, but the model must keep the direction in which the cart was
         * placed instead of snapping 180 degrees.
         */
        matrices.multiply(
                RotationAxis.POSITIVE_Y.rotationDegrees(yaw)
        );

        matrices.multiply(
                RotationAxis.POSITIVE_X.rotationDegrees(pitch)
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

        double velocityX = entity.getVelocity().x;
        double velocityZ = entity.getVelocity().z;
        boolean uphill;

        switch (shape) {
            case ASCENDING_EAST -> uphill = velocityX >= 0.0D;
            case ASCENDING_WEST -> uphill = velocityX <= 0.0D;
            case ASCENDING_NORTH -> uphill = velocityZ <= 0.0D;
            case ASCENDING_SOUTH -> uphill = velocityZ >= 0.0D;
            default -> {
                return 0.0F;
            }
        }

        return uphill ? -45.0F : 45.0F;
    }
}
