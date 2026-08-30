package com.minecartmagic.client;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SelfPropellingBoatRenderer
        extends GeoEntityRenderer<SelfPropellingBoatEntity> {

    public SelfPropellingBoatRenderer(
            EntityRendererFactory.Context context
    ) {
        super(
                context,
                new SelfPropellingBoatModel()
        );

        shadowRadius = 0.8F;
    }

    @Override
    protected void applyRotations(
            SelfPropellingBoatEntity entity,
            MatrixStack matrices,
            float ageInTicks,
            float rotationYaw,
            float partialTick,
            float nativeScale
    ) {
        /*
         * GeoEntityRenderer по умолчанию использует:
         *
         * 180° - rotationYaw
         *
         * Но наша .geo-модель построена носом в +Z,
         * тогда как стандартная геометрия Boat ориентирована
         * относительно противоположной локальной оси.
         *
         * Для нашей модели правильное соответствие:
         *
         * -rotationYaw
         *
         * Поэтому yaw самой BoatEntity используется напрямую,
         * без дополнительного искусственного разворота.
         */
        matrices.multiply(
                RotationAxis.POSITIVE_Y.rotationDegrees(
                        -rotationYaw
                )
        );
    }
}
