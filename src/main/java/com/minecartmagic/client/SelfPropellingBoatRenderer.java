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

    /**
     * GeckoLib уже сам поворачивает модель по yaw:
     *
     * 180 - rotationYaw
     *
     * Наша геометрия лодки ориентирована относительно
     * противоположной локальной продольной оси.
     *
     * Поэтому меняем только локальную ориентацию модели
     * здесь, не трогая yaw самой Entity.
     *
     * В отличие от предыдущего варианта, мы НЕ вращаем
     * MatrixStack до super.render().
     *
     * Это происходит внутри реального GeckoLib
     * applyRotations(), который мы сейчас переопределяем.
     */
    @Override
    protected void applyRotations(
            SelfPropellingBoatEntity animatable,
            MatrixStack matrices,
            float ageInTicks,
            float rotationYaw,
            float partialTick,
            float nativeScale
    ) {
        /*
         * Точно такая же базовая ориентация,
         * которую делает GeckoLib.
         */
        matrices.multiply(
                RotationAxis.POSITIVE_Y.rotationDegrees(
                        180.0F - rotationYaw
                )
        );

        /*
         * Важный момент:
         *
         * .geo-модель имеет противоположное направление
         * продольной оси относительно стандартной
         * GeckoLib entity model.
         *
         * Поэтому исправляем ТОЛЬКО локальную ориентацию
         * самой геометрии.
         */
        matrices.multiply(
                RotationAxis.POSITIVE_Y.rotationDegrees(
                        180.0F
                )
        );
    }
}
