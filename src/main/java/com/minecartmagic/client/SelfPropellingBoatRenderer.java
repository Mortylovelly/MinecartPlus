package com.minecartmagic.client;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
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
    protected float calculateYRot(
            SelfPropellingBoatEntity animatable,
            float yHeadRot,
            float partialTick
    ) {
        /*
         * Для нашей лодки берём настоящий интерполированный
         * yaw самой Entity.
         *
         * Это не меняет физику или управление.
         * Меняется только визуальный поворот модели.
         */
        return animatable.getYaw(
                partialTick
        );
    }
}
