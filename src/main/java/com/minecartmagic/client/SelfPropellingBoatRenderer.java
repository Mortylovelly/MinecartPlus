package com.minecartmagic.client;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderType;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
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
     * Это текущая рабочая ориентация модели.
     *
     * Не добавляем никаких дополнительных поворотов
     * в render() или в маску.
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
     * РЕНДЕР КОСТЕЙ
     * =====================================================
     *
     * Используем существующий bone:
     *
     * bottom_no_water
     *
     * Он больше НЕ рисуется обычной текстурой.
     *
     * Вместо этого его геометрия отправляется в
     * RenderLayer.getWaterMask().
     *
     * Поэтому маска получает:
     *
     * - тот же pivot
     * - тот же rotation
     * - те же cubes
     * - ту же ориентацию
     *
     * что и реальная модель лодки.
     */
    @Override
    public void renderRecursively(
            MatrixStack matrices,
            SelfPropellingBoatEntity entity,
            GeoBone bone,
            RenderType renderType,
            VertexConsumerProvider vertexConsumers,
            VertexConsumer buffer,
            boolean isReRender,
            float partialTick,
            int packedLight,
            int packedOverlay,
            int colour
    ) {
        /*
         * =================================================
         * BOTTOM_NO_WATER
         * =================================================
         */
        if ("bottom_no_water".equals(
                bone.getName()
        )) {

            /*
             * При re-render GeckoLib не создаём
             * дополнительную water mask.
             */
            if (!isReRender) {

                matrices.push();

                /*
                 * Полностью повторяем стандартные
                 * преобразования GeckoLib для bone.
                 */
                software.bernie.geckolib.util.RenderUtil.translateMatrixToBone(
                        matrices,
                        bone
                );

                software.bernie.geckolib.util.RenderUtil.translateToPivotPoint(
                        matrices,
                        bone
                );

                software.bernie.geckolib.util.RenderUtil.rotateMatrixAroundBone(
                        matrices,
                        bone
                );

                software.bernie.geckolib.util.RenderUtil.scaleMatrixForBone(
                        matrices,
                        bone
                );

                software.bernie.geckolib.util.RenderUtil.translateAwayFromPivotPoint(
                        matrices,
                        bone
                );

                /*
                 * Получаем именно ванильный water-mask layer.
                 *
                 * Он не рисует текстуру дерева.
                 */
                VertexConsumer waterMaskBuffer =
                        vertexConsumers.getBuffer(
                                RenderLayer.getWaterMask()
                        );

                /*
                 * Рисуем существующий bottom_no_water
                 * как depth-mask.
                 */
                renderCubesOfBone(
                        matrices,
                        bone,
                        waterMaskBuffer,
                        packedLight,
                        packedOverlay,
                        colour
                );

                /*
                 * bottom_no_water не должен иметь обычного
                 * текстурного рендера.
                 */
                matrices.popPose();
            }

            /*
             * Очень важно:
             *
             * Не вызываем super.renderRecursively()
             * для этого bone.
             *
             * Поэтому деревянной плиты от него больше
             * не появляется.
             */
            return;
        }

        /*
         * Все остальные bone рендерим совершенно
         * обычным GeckoLib способом.
         */
        super.renderRecursively(
                matrices,
                entity,
                bone,
                renderType,
                vertexConsumers,
                buffer,
                isReRender,
                partialTick,
                packedLight,
                packedOverlay,
                colour
        );
    }

    /*
     * =====================================================
     * PRE-RENDER
     * =====================================================
     *
     * Никакой самодельной плоскости здесь больше нет.
     *
     * Вся маска берётся непосредственно из
     * bottom_no_water.
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
    }
}
