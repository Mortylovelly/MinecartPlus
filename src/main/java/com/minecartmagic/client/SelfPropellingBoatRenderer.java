package com.minecartmagic.client;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
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
     * Оставляем ту же рабочую схему, которая сейчас
     * правильно ориентирует саму модель.
     *
     * Никакого дополнительного поворота в render() больше
     * не существует.
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
     * ПОДГОТОВКА РЕНДЕРА
     * =====================================================
     *
     * GeckoLib уже применил здесь:
     *
     * - позицию сущности
     * - масштаб
     * - applyRotations()
     *
     * Поэтому водяную маску рисуем здесь же.
     *
     * Благодаря этому маска получает ТОЧНО ТУ ЖЕ
     * ориентацию, что и сама модель лодки.
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

        /*
         * Маска нужна только для основного рендера.
         *
         * Во время внутренних re-render GeckoLib
         * повторять её не нужно.
         */
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
     * VANILLA WATER MASK
     * =====================================================
     *
     * Это НЕ деревянный куб и НЕ видимая текстура.
     *
     * RenderLayer.getWaterMask() используется Minecraft
     * именно как depth-mask для лодки.
     *
     * Его задача:
     *
     * вода мира -> за маской
     * содержимое лодки -> перед маской
     *
     * Поэтому вода внутри лодки перестает просвечивать.
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
         * Плоскость расположена внутри корпуса.
         *
         * Она НЕ находится под лодкой.
         *
         * Размер соответствует внутренней части
         * текущей модели.
         */
        float minX = -8.0F;
        float maxX = 8.0F;

        float minZ = -9.0F;
        float maxZ = 9.0F;

        /*
         * Верхняя поверхность внутреннего пола.
         *
         * Небольшой сдвиг вверх нужен, чтобы поверхность
         * гарантированно оказалась перед водой.
         */
        float y = 3.02F;

        /*
         * Верхняя сторона.
         *
         * Никакой обычной текстуры здесь нет.
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
