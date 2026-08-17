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
         * II  = 0.82 блока/тик
         * III = 1.05 блока/тик
         *
         * Это заметно быстрее обычной лодки,
         * но слабее наших максимальных значений Тяги.
         */
        double maxSpeed = switch (level) {
            case 1 -> 0.60D;
            case 2 -> 0.82D;
            default -> 1.05D;
        };

        /*
         * Эффект работает именно на воде.
         * На льду и других поверхностях ванильная
         * физика лодки остаётся без изменений.
         */
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

        if (horizontalSpeed <= maxSpeed) {
            return;
        }

        double scale =
                maxSpeed / horizontalSpeed;

        boat.setVelocity(
                velocity.x * scale,
                velocity.y,
                velocity.z * scale
        );
    }
}
