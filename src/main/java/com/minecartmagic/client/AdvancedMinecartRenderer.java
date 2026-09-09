package com.minecartmagic.client;

import com.minecartmagic.entity.AdvancedMinecartEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.util.math.MatrixStack;
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

        matrices.multiply(
                RotationAxis.POSITIVE_Y.rotationDegrees(
                        180.0F - yaw
                )
        );
    }
}
