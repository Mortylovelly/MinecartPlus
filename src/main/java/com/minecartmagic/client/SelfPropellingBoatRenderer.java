package com.minecartmagic.client;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
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
         * Не используем стандартный applyRotations GeckoLib,
         * потому что он добавляет 180 - yaw.
         *
         * Наша .geo-модель уже ориентирована так,
         * чтобы ей соответствовал обычный yaw лодки.
         *
         * Берём интерполированный yaw самой BoatEntity,
         * чтобы на клиенте модель плавно следовала за сущностью.
         */
        float interpolatedYaw =
                MathHelper.lerpAngleDegrees(
                        partialTick,
                        entity.prevYaw,
                        entity.getYaw()
                );

        matrices.multiply(
                RotationAxis.POSITIVE_Y.rotationDegrees(
                        -interpolatedYaw
                )
        );
    }
}
