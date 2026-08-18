package com.minecartmagic.mixin;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatEntity.class)
public abstract class SelfPropellingBoatVelocityMixin {

    @Shadow
    private boolean pressingLeft;

    @Shadow
    private boolean pressingRight;

    @Inject(
            method = "updateVelocity",
            at = @At("HEAD"),
            cancellable = true
    )
    private void minecartmagic$handleSelfPropelledVelocity(
            CallbackInfo ci
    ) {
        BoatEntity boat =
                (BoatEntity) (Object) this;

        if (!(boat instanceof SelfPropellingBoatEntity selfPropellingBoat)) {
            return;
        }

        /*
         * Только лодка с топливом переходит
         * в самоходный режим.
         *
         * Без топлива vanilla updateVelocity()
         * продолжает работать полностью нормально.
         */
        if (!selfPropellingBoat.hasFuel()) {
            return;
        }

        selfPropellingBoat.applySelfPropulsion(
                pressingLeft,
                pressingRight
        );

        /*
         * Не даём ванильной BoatEntity повторно
         * обработать W/S/A/D и перезаписать скорость.
         */
        ci.cancel();
    }
}
