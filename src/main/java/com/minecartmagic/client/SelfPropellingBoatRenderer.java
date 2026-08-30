package com.minecartmagic.client;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SelfPropellingBoatRenderer
        extends GeoEntityRenderer<SelfPropellingBoatEntity> {

    private long lastDebugTick = Long.MIN_VALUE;

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
            SelfPropellingBoatEntity boat,
            float entityYaw,
            float tickDelta,
            Object matrices,
            Object vertexConsumers,
            int light
    ) {
        debugBoatRotation(
                boat,
                entityYaw,
                tickDelta
        );

        /*
         * НИЧЕГО не меняем в рендере.
         * Весь существующий GeckoLib renderer
         * продолжает работать как раньше.
         */
        super.render(
                boat,
                entityYaw,
                tickDelta,
                matrices,
                vertexConsumers,
                light
        );
    }

    private void debugBoatRotation(
            SelfPropellingBoatEntity boat,
            float rendererYaw,
            float tickDelta
    ) {
        /*
         * ОСТАВЛЕНО НАМЕРЕННО ПРОСТО.
         *
         * Здесь нам нужно увидеть:
         *
         * 1. yaw самой entity
         * 2. yaw, который пришёл в renderer
         * 3. prevYaw
         * 4. скорость
         * 5. направление движения
         * 6. вариант лодки
         * 7. наличие топлива
         */

        long tick =
                boat.getWorld().getTime();

        if (tick - lastDebugTick < 10) {
            return;
        }

        lastDebugTick =
                tick;

        var velocity =
                boat.getVelocity();

        double horizontalSpeed =
                Math.sqrt(
                        velocity.x * velocity.x
                                + velocity.z * velocity.z
                );

        double velocityYaw =
                horizontalSpeed > 0.00001D
                        ? Math.toDegrees(
                                Math.atan2(
                                        -velocity.x,
                                        velocity.z
                                )
                        )
                        : Double.NaN;

        SelfPropellingBoatClientLogger.log(
                boat,
                rendererYaw,
                tickDelta,
                velocity,
                horizontalSpeed,
                velocityYaw
        );
    }
}
