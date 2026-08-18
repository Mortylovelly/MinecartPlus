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
    private void minecartmagic$selfPropellingVelocity(
            CallbackInfo ci
    ) {
        BoatEntity boat =
                (BoatEntity) (Object) this;

        if (!(boat
                instanceof SelfPropellingBoatEntity selfPropellingBoat)) {
            return;
        }

        /*
         * NO FUEL:
         *
         * Completely vanilla BoatEntity.
         *
         * W/A/S/D work normally.
         */
        if (!selfPropellingBoat.hasFuel()) {
            return;
        }

        /*
         * Engine mode:
         *
         * W = ignored
         * S = ignored
         * A/D = steering
         */
        selfPropellingBoat.applySelfPropulsion(
                pressingLeft,
                pressingRight,
                selfPropellingBoat
                        .getWorld()
                        .isClient()
        );

        /*
         * Prevent vanilla boat movement from
         * overwriting the engine velocity.
         */
        ci.cancel();
    }
}
