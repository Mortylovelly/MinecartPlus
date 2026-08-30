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
         * НЕЛЬЗЯ использовать обычный rotationYaw GeckoLib
         * вслепую для нашей лодки.
         *
         * Берём именно интерполированный yaw самой BoatEntity:
         *
         * previousYaw -> currentYaw
         *
         * Это устраняет визуальные рывки между client/server
         * и делает модель следовать реальному вращению сущности.
         */
        float previousYaw = entity.prevYaw;
        float currentYaw = entity.getYaw();

        float interpolatedYaw = MathHelper.lerpAngleDegrees(
                partialTick,
                previousYaw,
                currentYaw
        );

        /*
         * GeckoLib обычно делает:
         *
         * 180 - yaw
         *
         * Для нашей геометрии нужен эквивалент:
         *
         * -yaw
         *
         * Поэтому используем интерполированный yaw.
         */
        matrices.multiply(
                RotationAxis.POSITIVE_Y.rotationDegrees(
                        -interpolatedYaw
                )
        );
    }
}
