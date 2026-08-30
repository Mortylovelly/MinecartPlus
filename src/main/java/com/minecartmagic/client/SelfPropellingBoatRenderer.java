package com.minecartmagic.client;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
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
     * ВАЖНО:
     *
     * GeoEntityRenderer сам выполняет ванильную
     * трансформацию:
     *
     * 180 - entityYaw
     *
     * Поэтому здесь больше НЕ добавляем
     * дополнительный поворот 180 градусов.
     *
     * Благодаря этому визуальная модель использует
     * ту же систему ориентации, что и BoatEntity.
     */

    @Override
    public RenderLayer getRenderType(
            SelfPropellingBoatEntity entity,
            Identifier texture,
            VertexConsumerProvider provider,
            float partialTick
    ) {
        /*
         * Используем полностью непрозрачный entity-render.
         *
         * Это важно для модели лодки:
         * вода не должна визуально просвечивать
         * через непрозрачную геометрию.
         */
        return RenderLayer.getEntitySolid(texture);
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
