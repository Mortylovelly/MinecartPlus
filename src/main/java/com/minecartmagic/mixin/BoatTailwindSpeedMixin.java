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

        int level =
                ModEnchantments.getTailwindLevel(boat);

        if (level <= 0) {
            return;
        }

        if (!boat.isTouchingWater()) {
            return;
        }

        double maxSpeed = switch (level) {
            case 1 -> 0.60D;
            case 2 -> 0.80D;
            default -> 1.00D;
        };

        double acceleration = switch (level) {
            case 1 -> 0.020D;
            case 2 -> 0.032D;
            default -> 0.045D;
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

        if (horizontalSpeed >= maxSpeed) {
            return;
        }

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
