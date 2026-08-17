package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatEntity.class)
public abstract class BoatTailwindSpeedMixin {

    @Shadow
    private boolean pressingForward;

    @Inject(
            method = "updateVelocity",
            at = @At("TAIL")
    )
    private void minecartmagic$applyTailwindSpeed(
            CallbackInfo ci
    ) {
        BoatEntity boat =
                (BoatEntity) (Object) this;

        /*
         * Важно:
         * если игрок не нажимает W,
         * мы вообще не вмешиваемся в физику.
         *
         * Поэтому после отпускания W лодка
         * нормально тормозит по ванильной физике.
         */
        if (!pressingForward) {
            return;
        }

        int level =
                ModEnchantments.getTailwindLevel(boat);

        if (level <= 0) {
            return;
        }

        if (!boat.isTouchingWater()) {
            return;
        }

        /*
         * Максимальная скорость Попутного ветра.
         *
         * I   = 0.55
         * II  = 0.65
         * III = 0.75
         *
         * Это выше обычной лодки,
         * но ниже максимальных значений Тяги.
         */
        double maxSpeed = switch (level) {
            case 1 -> 0.55D;
            case 2 -> 0.65D;
            default -> 0.75D;
        };

        /*
         * Насколько сильно Попутный ветер
         * усиливает результат ванильного движения.
         */
        double multiplier = switch (level) {
            case 1 -> 1.08D;
            case 2 -> 1.14D;
            default -> 1.20D;
        };

        Vec3d velocity =
                boat.getVelocity();

        double horizontalSpeed =
                Math.sqrt(
                        velocity.x * velocity.x
                                + velocity.z * velocity.z
                );

        if (horizontalSpeed <= 0.00001D) {
            return;
        }

        /*
         * Усиливаем уже рассчитанную ванилью
         * скорость, а не создаём движение из ничего.
         */
        double boostedSpeed =
                horizontalSpeed * multiplier;

        boostedSpeed =
                Math.min(
                        boostedSpeed,
                        maxSpeed
                );

        if (boostedSpeed <= horizontalSpeed) {
            return;
        }

        double scale =
                boostedSpeed / horizontalSpeed;

        boat.setVelocity(
                velocity.x * scale,
                velocity.y,
                velocity.z * scale
        );
    }
}
