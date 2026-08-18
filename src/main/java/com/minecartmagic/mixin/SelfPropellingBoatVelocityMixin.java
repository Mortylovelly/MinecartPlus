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
    private void minecartmagic$handleSelfPropulsion(
            CallbackInfo ci
    ) {
        BoatEntity boat =
                (BoatEntity) (Object) this;

        if (!(boat instanceof SelfPropellingBoatEntity selfPropellingBoat)) {
            return;
        }

        /*
         * Без работающего топлива:
         *
         * полностью ванильная лодка.
         * W/A/S/D работают как обычно.
         */
        if (!selfPropellingBoat.hasFuel()) {
            return;
        }

        boolean clientSide =
                selfPropellingBoat.getWorld().isClient();

        /*
         * Внутри BoatEntity реальные состояния
         * A/D находятся именно в этих полях.
         */
        selfPropellingBoat.applySelfPropulsion(
                pressingLeft,
                pressingRight,
                clientSide
        );

        /*
         * При работающем двигателе не даём
         * ванильной физике повторно изменить
         * нашу скорость.
         */
        ci.cancel();
    }
}
