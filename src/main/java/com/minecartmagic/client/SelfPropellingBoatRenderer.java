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
     * BOTTOM_NO_WATER -> VANILLA WATER MASK
     * =====================================================
     *
     * Используем уже существующий bone:
     *
     * bottom_no_water
     *
     * Он не рендерится обычной текстурой.
     * Вместо этого его геометрия отправляется в
     * специальный water-mask layer Minecraft.
     */
    @Override
    public void renderRecursively(
            MatrixStack matrices,
            SelfPropellingBoatEntity entity,
            GeoBone bone,
            RenderLayer renderLayer,
            VertexConsumerProvider vertexConsumers,
            VertexConsumer buffer,
            boolean isReRender,
            float partialTick,
            int packedLight,
            int packedOverlay,
            int colour
    ) {
        /*
         * Именно эту кость используем как маску воды.
         */
        if ("bottom_no_water".equals(
                bone.getName()
        )) {

            /*
             * Во время re-render GeckoLib
             * дополнительную маску не создаём.
             */
            if (!isReRender) {

                matrices.push();

                /*
                 * Полностью повторяем стандартные
                 * преобразования конкретного GeoBone.
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
                 * Получаем настоящий water-mask layer.
                 */
                VertexConsumer waterMaskBuffer =
                        vertexConsumers.getBuffer(
                                RenderLayer.getWaterMask()
                        );

                /*
                 * Отрисовываем РЕАЛЬНУЮ геометрию
                 * bottom_no_water как water mask.
                 */
                renderCubesOfBone(
                        matrices,
                        bone,
                        waterMaskBuffer,
                        packedLight,
                        packedOverlay,
                        colour
                );

                matrices.pop();
            }

            /*
             * Не отправляем этот bone в обычный
             * GeckoLib-рендер.
             *
             * Поэтому деревянной пластины больше нет.
             */
            return;
        }

        /*
         * Все остальные кости работают как раньше.
         */
        super.renderRecursively(
                matrices,
                entity,
                bone,
                renderLayer,
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
     * Здесь больше ничего не создаём.
     * Никаких дополнительных квадратов.
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
