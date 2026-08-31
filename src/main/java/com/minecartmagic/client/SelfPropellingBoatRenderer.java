package com.minecartmagic.client;

import com.minecartmagic.ModEnchantments;
import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
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
         * =================================================
         * WATER MASK
         * =================================================
         */
        if ("bottom_no_water".equals(
                bone.getName()
        )) {

            if (!isReRender) {

                matrices.push();

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
                        colour
                );

                matrices.pop();
            }

            return;
        }

        /*
         * =================================================
         * VANILLA ENCHANTMENT GLINT
         * =================================================
         *
         * Если у лодки есть Tailwind:
         *
         * обычный buffer заменяется на настоящий
         * vanilla enchanted-item glint consumer.
         *
         * При этом сам GeckoLib render остаётся тем же.
         */
        VertexConsumer actualBuffer =
                buffer;

        int tailwindLevel =
                ModEnchantments.getTailwindLevel(
                        entity
                );

        if (tailwindLevel > 0) {

            actualBuffer =
                    ItemRenderer.getItemGlintConsumer(
                            vertexConsumers,
                            renderLayer,
                            false,
                            true
                    );
        }

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
                colour
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
