package com.minecartmagic.client;

import com.minecartmagic.entity.AdvancedMinecartEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
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

        Vec3d previousVelocity = entity.getVelocity();
        Vec3d velocity = entity.getVelocity();

        double horizontalSpeed = Math.sqrt(
                velocity.x * velocity.x + velocity.z * velocity.z
        );

        float pitch = 0.0F;

        if (horizontalSpeed > 1.0E-4D) {
            pitch = (float) (-Math.toDegrees(Math.atan2(
                    velocity.y,
                    horizontalSpeed
            )));
        }

        // The supplied model is authored 90 degrees off the vanilla
        // minecart forward axis. Keep that correction, then add the
        // pitch produced by the rail slope so the model follows hills.
        matrices.multiply(
                RotationAxis.POSITIVE_Y.rotationDegrees(
                        270.0F - yaw
                )
        );

        matrices.multiply(
                RotationAxis.POSITIVE_X.rotationDegrees(
                        pitch
                )
        );
    }
}
