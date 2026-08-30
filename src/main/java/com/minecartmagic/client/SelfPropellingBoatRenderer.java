package com.minecartmagic.client;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SelfPropellingBoatRenderer
        extends GeoEntityRenderer<SelfPropellingBoatEntity> {

    private static final ModelPart WATER_MASK =
            createWaterMask();

    private static ModelPart createWaterMask() {

        ModelData modelData =
                new ModelData();

        modelData
                .getRoot()
                .addChild(
                        "water_patch",
                        ModelPartBuilder
                                .create()
                                .cuboid(
                                        -13.0F,
                                        3.0F,
                                        -9.0F,
                                        26.0F,
                                        1.0F,
                                        18.0F
                                ),
                        ModelTransform.NONE
                );

        return TexturedModelData
                .of(
                        modelData,
                        64,
                        64
                )
                .createModel()
                .getChild(
                        "water_patch"
                );
    }

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
    public void render(
            SelfPropellingBoatEntity entity,
            float entityYaw,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light
    ) {

        matrices.push();

        matrices.multiply(
                RotationAxis.POSITIVE_Y.rotationDegrees(
                        180.0F - entityYaw
                )
        );

        VertexConsumer waterMaskBuffer =
                vertexConsumers.getBuffer(
                        RenderLayer.getWaterMask()
                );

        WATER_MASK.render(
                matrices,
                waterMaskBuffer,
                light,
                0
        );

        matrices.pop();

        super.render(
                entity,
                entityYaw,
                tickDelta,
                matrices,
                vertexConsumers,
                light
        );
    }
}
