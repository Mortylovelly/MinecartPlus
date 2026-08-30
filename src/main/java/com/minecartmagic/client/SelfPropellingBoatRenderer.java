package com.minecartmagic.client;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
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

    /*
     * =====================================================
     * ОРИЕНТАЦИЯ ЛОДКИ
     * =====================================================
     *
     * Это уже рабочая ориентация нашей модели.
     *
     * В render() НИКАКОЙ дополнительный поворот
     * больше не выполняется.
     */
    @Override
    protected void applyRotations(
            SelfPropellingBoatEntity entity,
            MatrixStack matrices,
            float ageInTicks,
            float rotationYaw,
            float partialTick,
            float nativeScale
    ) {
        float interpolatedYaw =
                MathHelper.lerpAngleDegrees(
                        partialTick,
                        entity.prevYaw,
                        entity.getYaw()
                );

        matrices.multiply(
                RotationAxis.POSITIVE_Y.rotationDegrees(
                        180.0F - interpolatedYaw
                )
        );
    }

    /*
     * =====================================================
     * WATER MASK
     * =====================================================
     *
     * Маска вызывается после того, как GeckoLib уже
     * подготовил PoseStack для нашей сущности.
     *
     * Поэтому она получает ТО ЖЕ положение и поворот,
     * что и модель.
     */
    @Override
    public void preRender(
            MatrixStack matrices,
            SelfPropellingBoatEntity entity,
            BakedGeoModel model,
            @Nullable VertexConsumerProvider vertexConsumers,
            @Nullable VertexConsumer buffer,
            boolean isReRender,
            float partialTick,
            int packedLight,
            int packedOverlay,
            int colour
    ) {
        super.preRender(
                matrices,
                entity,
                model,
                vertexConsumers,
                buffer,
                isReRender,
                partialTick,
                packedLight,
                packedOverlay,
                colour
        );

        if (isReRender || vertexConsumers == null) {
            return;
        }

        renderWaterMask(
                matrices,
                vertexConsumers
        );
    }

    /*
     * =====================================================
     * ВОДЯНАЯ МАСКА
     * =====================================================
     *
     * ВАЖНО:
     *
     * Координаты geo-модели заданы в ПИКСЕЛЯХ.
     *
     * Поэтому:
     *
     * 26 px = 26 / 16 блока
     * 18 px = 18 / 16 блока
     * 3 px  =  3 / 16 блока
     *
     * Раньше здесь ошибочно использовались 26 и 18
     * непосредственно как мировые координаты.
     *
     * Из-за этого появлялся огромный квадрат над лодкой.
     */
    private void renderWaterMask(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers
    ) {
        VertexConsumer consumer =
                vertexConsumers.getBuffer(
                        RenderLayer.getWaterMask()
                );

        Matrix4f matrix =
                matrices.peek().getPositionMatrix();

        /*
         * Соответствует внутренней области
         * bottom_no_water из geo-модели.
         */
        float minX =
                -13.0F / 16.0F;

        float maxX =
                13.0F / 16.0F;

        float minZ =
                -9.0F / 16.0F;

        float maxZ =
                9.0F / 16.0F;

        /*
         * Поверхность находится всего на 3 px
         * выше локального нуля модели.
         *
         * Это координата МОДЕЛИ, а не мира.
         */
        float y =
                3.0F / 16.0F;

        /*
         * Рисуем ровно одну плоскость.
         *
         * Она не имеет деревянной текстуры.
         *
         * RenderLayer.getWaterMask() используется
         * только для маскирования воды.
         */
        consumer.vertex(
                matrix,
                minX,
                y,
                minZ
        );

        consumer.vertex(
                matrix,
                minX,
                y,
                maxZ
        );

        consumer.vertex(
                matrix,
                maxX,
                y,
                maxZ
        );

        consumer.vertex(
                matrix,
                maxX,
                y,
                minZ
        );
    }
}
