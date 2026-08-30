package com.minecartmagic.client;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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

    /*
     * GeckoLib по умолчанию делает:
     *
     * 180° - rotationYaw
     *
     * Наша модель уже построена носом вдоль +Z,
     * поэтому стандартный поворот оказывается перевёрнутым
     * относительно направления BoatEntity.
     *
     * Добавляем ещё 180°, чтобы получить:
     *
     * (180° - rotationYaw) + 180°
     * = -rotationYaw
     *
     * Благодаря этому модель смотрит туда же,
     * куда направлена сама лодка.
     */
    @Override
    protected void applyRotations(
            SelfPropellingBoatEntity animatable,
            PoseStack poseStack,
            float ageInTicks,
            float rotationYaw,
            float partialTick
    ) {
        super.applyRotations(
                animatable,
                poseStack,
                ageInTicks,
                rotationYaw,
                partialTick
        );

        poseStack.mulPose(
                Axis.YP.rotationDegrees(
                        180.0F
                )
        );
    }
}
