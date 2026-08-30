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
         * Берём настоящий интерполированный yaw BoatEntity,
         * как у ванильной лодки.
         */
        float interpolatedYaw =
                MathHelper.lerpAngleDegrees(
                        partialTick,
                        entity.prevYaw,
                        entity.getYaw()
                );

        /*
         * GeckoLib-модель имеет направление, противоположное
         * направлению BoatEntity.
         *
         * Дополнительные 180° переворачивают ТОЛЬКО визуальную
         * модель. Физика, yaw сущности и пассажир не изменяются.
         *
         * Итог:
         *   - лодка физически плывёт правильно;
         *   - модель смотрит в направлении движения;
         *   - пассажир не начинает вращаться;
         *   - никаких дополнительных render() и вторичных
         *     поворотов нет.
         */
        matrices.multiply(
                RotationAxis.POSITIVE_Y.rotationDegrees(
                        180.0F - interpolatedYaw
                )
        );
    }
}
