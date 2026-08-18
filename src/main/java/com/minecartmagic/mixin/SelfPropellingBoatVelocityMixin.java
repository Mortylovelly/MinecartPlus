package com.minecartmagic.mixin;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatEntity.class)
public abstract class SelfPropellingBoatVelocityMixin {

    @Inject(
            method = "updateVelocity",
            at = @At("HEAD"),
            cancellable = true
    )
    private void minecartmagic$handleSelfPropulsion(
            CallbackInfo ci
    ) {
        BoatEntity boat =
                (BoatEntity) (Object) this;

        if (!(boat
                instanceof SelfPropellingBoatEntity selfPropellingBoat)) {
            return;
        }

        if (!selfPropellingBoat.hasFuel()) {
            return;
        }

        boolean clientSide =
                selfPropellingBoat
                        .getWorld()
                        .isClient();

        selfPropellingBoat.applySelfPropulsion(
                selfPropellingBoat.isPressingLeft(),
                selfPropellingBoat.isPressingRight(),
                clientSide
        );

        ci.cancel();
    }
}
