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
        float yaw = MathHelper.lerpAngleDegrees(
                partialTick,
                entity.prevYaw,
                entity.getYaw()
        );

        float pitch = getRailPitch(entity, yaw);

        // The GeckoLib model is authored with its long axis along Z,
        // so the entity yaw can be applied directly without the old 270° offset.
        matrices.multiply(
                RotationAxis.POSITIVE_Y.rotationDegrees(yaw)
        );

        matrices.multiply(
                RotationAxis.POSITIVE_X.rotationDegrees(pitch)
        );
    }

    private static float getRailPitch(
            AdvancedMinecartEntity entity,
            float yaw
    ) {
        BlockState state = entity.getWorld().getBlockState(entity.getBlockPos());

        if (!(state.getBlock() instanceof AbstractRailBlock railBlock)) {
            return 0.0F;
        }

        RailShape shape = state.get(railBlock.getShapeProperty());

        double uphillX;
        double uphillZ;

        switch (shape) {
            case ASCENDING_EAST -> {
                uphillX = 1.0D;
                uphillZ = 0.0D;
            }
            case ASCENDING_WEST -> {
                uphillX = -1.0D;
                uphillZ = 0.0D;
            }
            case ASCENDING_NORTH -> {
                uphillX = 0.0D;
                uphillZ = -1.0D;
            }
            case ASCENDING_SOUTH -> {
                uphillX = 0.0D;
                uphillZ = 1.0D;
            }
            default -> {
                return 0.0F;
            }
        }

        Vec3d forward = new Vec3d(
                -Math.sin(Math.toRadians(yaw)),
                0.0D,
                Math.cos(Math.toRadians(yaw))
        );

        double direction =
                forward.x * uphillX
                        + forward.z * uphillZ;

        // Positive X rotation tilts the front downward, so facing uphill
        // uses -45°, while facing downhill uses +45°.
        return direction >= 0.0D ? -45.0F : 45.0F;
    }
}
