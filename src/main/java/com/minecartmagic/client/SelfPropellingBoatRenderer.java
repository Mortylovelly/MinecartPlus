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

    @Override
    protected void applyRotations(
            SelfPropellingBoatEntity entity,
            MatrixStack matrices,
            float ageInTicks,
            float rotationYaw,
            float partialTick,
            float nativeScale
    ) {
        /*
         * Интерполированный yaw самой BoatEntity.
         *
         * Никакого super.applyRotations() здесь нет,
         * чтобы GeckoLib не добавлял свой второй 180°-поворот.
         */
        float interpolatedYaw =
                MathHelper.lerpAngleDegrees(
                        partialTick,
                        entity.prevYaw,
                        entity.getYaw()
                );

        /*
         * Геометрия нашей модели развернута относительно
         * стандартной системы GeckoLib.
         *
         * Поэтому визуальную модель разворачиваем на 180°.
         */
        matrices.multiply(
                RotationAxis.POSITIVE_Y.rotationDegrees(
                        180.0F - interpolatedYaw
                )
        );
    }

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
         * =========================================================
         * WATER MASK
         * =========================================================
         *
         * Ванильная лодка использует специальный
         * RenderLayer.getWaterMask().
         *
         * Этот слой не рисует видимую текстуру.
         * Он только записывает глубину, благодаря чему
         * вода мира перестает отображаться внутри лодки.
         *
         * Это принципиально отличается от деревянного куба:
         * визуально здесь ничего не появляется.
         */
        matrices.push();

        float interpolatedYaw =
                MathHelper.lerpAngleDegrees(
                        partialPart(entity, partialTick),
                        entity.prevYaw,
                        entity.getYaw()
                );

        matrices.multiply(
                RotationAxis.POSITIVE_Y.rotationDegrees(
                        180.0F - interpolatedYaw
                )
        );

        renderWaterMask(
                matrices,
                vertexConsumers
        );

        matrices.pop();

        /*
         * После маски рендерим обычную GeckoLib-модель.
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

    private float partialPart(
            SelfPropellingBoatEntity entity,
            float partialTick
    ) {
        return partialTick;
    }

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
         * Внутренняя поверхность лодки.
         *
         * Координаты специально находятся ВНУТРИ корпуса,
         * а не под лодкой.
         *
         * Никакой текстуры здесь нет:
         * RenderLayer.getWaterMask() используется только
         * как глубинная маска.
         */
        float minX = -8.0F;
        float maxX = 8.0F;

        float minZ = -9.0F;
        float maxZ = 9.0F;

        float y = 3.01F;

        consumer.vertex(matrix, minX, y, minZ);
        consumer.vertex(matrix, minX, y, maxZ);
        consumer.vertex(matrix, maxX, y, maxZ);
        consumer.vertex(matrix, maxX, y, minZ);
    }
}
