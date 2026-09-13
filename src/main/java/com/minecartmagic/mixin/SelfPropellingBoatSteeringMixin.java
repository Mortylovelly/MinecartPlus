package com.minecartmagic.mixin;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelfPropellingBoatEntity.class)
public abstract class SelfPropellingBoatSteeringMixin {

    private static final float STEERING_SPEED = 2.5F;

    @Inject(method = "applySelfPropulsion", at = @At("HEAD"))
    private void minecartmagic$applyEngineSteering(
            boolean pressingLeft,
            boolean pressingRight,
            boolean clientSide,
            CallbackInfo ci
    ) {
        SelfPropellingBoatEntity boat =
                (SelfPropellingBoatEntity) (Object) this;

        if (!boat.hasFuel() || !boat.isTouchingWater()) {
            return;
        }

        if (pressingLeft == pressingRight) {
            return;
        }

        float yaw = boat.getYaw();

        if (pressingLeft) {
            yaw -= STEERING_SPEED;
        } else {
            yaw += STEERING_SPEED;
        }

        boat.setYaw(yaw);
        boat.setBodyYaw(yaw);
        boat.setHeadYaw(yaw);
        boat.prevYaw = yaw;
    }
}
