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
            at = @At("TAIL")
    )
    private void minecartmagic$applyEngineAfterVanillaPhysics(
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
         * Без топлива двигатель не работает.
         */
        if (!selfPropellingBoat.hasFuel()) {
            return;
        }

        /*
         * На суше двигатель не работает.
         */
        if (!selfPropellingBoat.isTouchingWater()) {
            return;
        }

        /*
         * Передаём существующему двигателю
         * только фактическое состояние A/D
         * и сторону мира.
         *
         * Сам двигатель НЕ меняет yaw.
         * Поворот остаётся ванильным.
         */
        selfPropellingBoat.applySelfPropulsion(
                pressingLeft,
                pressingRight,
                boat.getWorld().isClient()
        );
    }
}
