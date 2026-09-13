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
     * GeckoLib 4.9.x в оригинале позволяет переопределить
     * applyRotations(). В GeckoLib 4.8.4 этого override с
     * нашей сигнатурой нет, поэтому сохраняем ТО ЖЕ самое
     * преобразование через гарантированно вызываемый
     * preRender().
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
                red,
                green,
                blue,
                alpha
        );
    }

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
                red,
                green,
                blue,
                alpha
        );
    }
}
