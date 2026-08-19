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
            at = @At("TAIL")
    )
    private void minecartmagic$applySelfPropulsion(
            CallbackInfo ci
    ) {
        BoatEntity boat =
                (BoatEntity) (Object) this;

        /*
         * Только наша самоходная лодка.
         */
        if (!(boat instanceof SelfPropellingBoatEntity selfPropellingBoat)) {
            return;
        }

        /*
         * Вся логика двигателя находится только
         * в SelfPropellingBoatEntity.applySelfPropulsion().
         *
         * Здесь не дублируем расчёт скорости.
         */
        selfPropellingBoat.applySelfPropulsion();
    }
}
