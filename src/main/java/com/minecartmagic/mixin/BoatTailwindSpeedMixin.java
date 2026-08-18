package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import com.minecartmagic.entity.SelfPropellingBoatEntity;
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
         * Самоходная лодка использует собственную
         * систему двигателя.
         *
         * Иначе здесь Tailwind мог бы второй раз
         * вмешиваться в скорость и реагировать на W.
         */
        if (boat instanceof SelfPropellingBoatEntity) {
            return;
        }

        /*
         * Старое поведение обычных лодок НЕ меняем.
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

        double maxSpeed = switch (level) {
            case 1 -> 0.55D;
            case 2 -> 0.65D;
            default -> 0.75D;
        };

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
