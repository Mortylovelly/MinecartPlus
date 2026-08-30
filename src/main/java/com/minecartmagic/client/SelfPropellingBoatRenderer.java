package com.minecartmagic.client;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
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
     * ОРИЕНТАЦИЯ МОДЕЛИ
     * =====================================================
     *
     * Это наша рабочая ориентация.
     *
     * GeckoLib должен получить только один поворот:
     *
     * 180 - yaw
     *
     * Дополнительного render-поворота больше нет.
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
     * ВАЖНО:
     *
     * Здесь больше НЕ используется квадрат размером
     * 16 x 18 мировых блоков.
     *
     * Размер задаётся в координатах модели.
     *
     * 1 пиксель модели = 1/16 блока.
     */
    @Override
    public void render(
            SelfPropellingBoatEntity entity,
            float entityYaw,
            float partialTick,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int packedLight
    ) {
        /*
         * Сначала обычная GeckoLib-модель.
         *
         * Это сохраняет всю уже рабочую ориентацию,
         * текстуру, пассажира и анимационную систему.
         */
        super.render(
                entity,
                entityYaw,
                partialTick,
                matrices,
                vertexConsumers,
                packedLight
        );
    }

    /*
     * =====================================================
     * WATER MASK
     * =====================================================
     *
     * Рисуем маску в системе координат модели.
     *
     * Размеры соответствуют внутренней части корпуса:
     *
     * X: -13 .. +13
     * Z: -9  .. +9
     *
     * После масштабирования GeckoLib это:
     *
     * 26 / 16 = 1.625 блока
     * 18 / 16 = 1.125 блока
     *
     * Никаких 16-18 блоков здесь больше нет.
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

        float minX = -13.0F / 16.0F;
        float maxX = 13.0F / 16.0F;

        float minZ = -9.0F / 16.0F;
        float maxZ = 9.0F / 16.0F;

        /*
         * Внутри лодки.
         *
         * Чуть выше основания,
         * чтобы вода не находилась перед маской.
         */
        float y = 3.02F / 16.0F;

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
