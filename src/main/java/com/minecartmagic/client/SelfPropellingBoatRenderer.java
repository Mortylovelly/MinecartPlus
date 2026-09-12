package com.minecartmagic.client;

import com.minecartmagic.ModEnchantments;
import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
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
            float red,
            float green,
            float blue,
            float alpha
    ) {
        /*
         * =================================================
         * WATER MASK
         * =================================================
         *
         * Этот bone НЕ должен получать обычную текстуру
         * и НЕ должен получать glint.
         */
        if ("bottom_no_water".equals(
                bone.getName()
        )) {

            if (!isReRender) {

                matrices.push();

                software.bernie.geckolib.util.RenderUtils.translateMatrixToBone(
                        matrices,
                        bone
                );

                software.bernie.geckolib.util.RenderUtils.translateToPivotPoint(
                        matrices,
                        bone
                );

                software.bernie.geckolib.util.RenderUtils.rotateMatrixAroundBone(
                        matrices,
                        bone
                );

                software.bernie.geckolib.util.RenderUtils.scaleMatrixForBone(
                        matrices,
                        bone
                );

                software.bernie.geckolib.util.RenderUtils.translateAwayFromPivotPoint(
                        matrices,
                        bone
                );

                VertexConsumer waterMaskBuffer =
                        vertexConsumers.getBuffer(
                                RenderLayer.getWaterMask()
                        );

                renderCubesOfBone(
                        matrices,
                        bone,
                        waterMaskBuffer,
                        packedLight,
                        packedOverlay,
                        1.0F,
                        1.0F,
                        1.0F,
                        1.0F
                );

                matrices.pop();
            }

            return;
        }

        /*
         * =================================================
         * TAILWIND GLINT
         * =================================================
         *
         * Уровень берём из уже существующей системы
         * самоходной лодки.
         *
         * getEngineTailwindLevel() содержит уровень,
         * перенесённый на entity.
         *
         * Дополнительно проверяем attachment, чтобы
         * enchanted-состояние не потерялось, если
         * entity ещё не успела скопировать его
         * в engine level.
         */
        int engineTailwindLevel =
                entity.getEngineTailwindLevel();

        int attachmentTailwindLevel =
                ModEnchantments.getTailwindLevel(
                        entity
                );

        boolean enchanted =
                engineTailwindLevel > 0
                        || attachmentTailwindLevel > 0;

        VertexConsumer actualBuffer =
                buffer;

        /*
         * Используем настоящий vanilla item glint.
         *
         * Никакой собственной текстуры,
         * никакого второго model.render().
         */
        if (enchanted
                && vertexConsumers != null) {

            actualBuffer =
                    ItemRenderer.getItemGlintConsumer(
                            vertexConsumers,
                            renderLayer,
                            false,
                            true
                    );
        }

        /*
         * Все обычные bones рендерим через GeckoLib,
         * только с заменённым buffer для glint.
         */
        super.renderRecursively(
                matrices,
                entity,
                bone,
                renderLayer,
                vertexConsumers,
                actualBuffer,
                isReRender,
                partialTick,
                packedLight,
                packedOverlay,
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );
    }

    /*
     * =====================================================
     * PRE-RENDER
     * =====================================================
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
            float red,
            float green,
            float blue,
            float alpha
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
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );
    }
}
