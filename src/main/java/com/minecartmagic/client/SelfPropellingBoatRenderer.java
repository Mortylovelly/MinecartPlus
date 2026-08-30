package com.minecartmagic.client;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.client.render.VertexConsumerProvider;
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
    public void render(
            SelfPropellingBoatEntity entity,
            float entityYaw,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light
    ) {
        /*
         * GeckoLib уже выполняет свой стандартный поворот:
         *
         * 180° - entityYaw
         *
         * Наша .geo-модель ориентирована относительно другой
         * локальной оси, поэтому дополнительно разворачиваем
         * модель на 180°.
         *
         * Итоговая ориентация:
         *
         * (180° - yaw) + 180°
         * = -yaw
         *
         * То есть модель теперь ориентируется так же,
         * как сама BoatEntity.
         *
         * Никакую физику лодки это не меняет.
         */
        matrices.push();

        matrices.multiply(
                RotationAxis.POSITIVE_Y.rotationDegrees(
                        180.0F
                )
        );

        super.render(
                entity,
                entityYaw,
                tickDelta,
                matrices,
                vertexConsumers,
                light
        );

        matrices.pop();
    }
}
