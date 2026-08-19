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
         * Работаем только с нашей сущностью.
         */
        if (!(boat instanceof SelfPropellingBoatEntity selfPropellingBoat)) {
            return;
        }

        /*
         * Без топлива:
         * вообще не вмешиваемся.
         */
        if (!selfPropellingBoat.hasFuel()) {
            return;
        }

        /*
         * На суше:
         * никакого двигателя.
         */
        if (!selfPropellingBoat.isTouchingWater()) {
            return;
        }

        /*
         * После полностью ванильного
         * расчёта скорости лодки
         * применяем только наш двигатель.
         */
        selfPropellingBoat.applySelfPropulsion();
    }
}
