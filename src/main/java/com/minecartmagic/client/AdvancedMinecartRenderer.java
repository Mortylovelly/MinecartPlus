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
        float yaw = MathHelper.wrapDegrees(
                entity.getPlacementYaw() + 90.0F
        );

        matrices.multiply(
                RotationAxis.POSITIVE_Y.rotationDegrees(yaw)
        );

        matrices.multiply(
                RotationAxis.POSITIVE_X.rotationDegrees(
                        getRailPitch(entity)
                )
        );
    }

    private static float getRailPitch(AdvancedMinecartEntity entity) {
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
