package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatEntity.class)
public abstract class BoatTailwindSpeedMixin {

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
         * Скорость лодки рассчитываем на сервере.
         */
        if (boat.getEntityWorld().isClient()) {
            return;
        }

        int level =
                ModEnchantments.getTailwindLevel(boat);

        if (level <= 0) {
            return;
        }

        /*
         * Попутный ветер:
         *
         * I   = 0.60 блока/тик
         * II  = 0.80 блока/тик
         * III = 1.00 блока/тик
         */
        double maxSpeed = switch (level) {
            case 1 -> 0.60D;
            case 2 -> 0.80D;
            default -> 1.00D;
        };

        /*
         * Чем выше уровень, тем быстрее лодка набирает
         * максимальную скорость.
         */
        double acceleration = switch (level) {
            case 1 -> 0.035D;
            case 2 -> 0.050D;
            default -> 0.065D;
        };

        if (!boat.isTouchingWater()) {
            return;
        }

        Vec3d velocity =
                boat.getVelocity();

        double horizontalSpeed =
                Math.sqrt(
                        velocity.x * velocity.x
                                + velocity.z * velocity.z
                );

        /*
         * Если лодка практически стоит, ничего не
         * создаём искусственно. Ваниль сама начнёт движение
         * после гребли игрока.
         */
        if (horizontalSpeed <= 0.0001D) {
            return;
        }

        /*
         * Уже достигли максимальной скорости.
         */
        if (horizontalSpeed >= maxSpeed) {
            return;
        }

        /*
         * Плавно увеличиваем текущую скорость.
         */
        double newSpeed =
                Math.min(
                        horizontalSpeed + acceleration,
                        maxSpeed
                );

        double scale =
                newSpeed / horizontalSpeed;

        boat.setVelocity(
                velocity.x * scale,
                velocity.y,
                velocity.z * scale
        );
    }
}
