package com.minecartmagic.client;

import com.minecartmagic.entity.AdvancedMinecartEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AdvancedMinecartRenderer
        extends GeoEntityRenderer<AdvancedMinecartEntity> {

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

        // The supplied model is authored 90 degrees off the vanilla
        // minecart forward axis, so compensate here instead of touching
        // the model geometry.
        matrices.multiply(
                RotationAxis.POSITIVE_Y.rotationDegrees(
                        270.0F - yaw
                )
        );
    }
}
