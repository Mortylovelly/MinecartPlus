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
         * Нет работающего топлива:
         *
         * полностью ванильная лодка.
         */
        if (!selfPropellingBoat.hasFuel()) {
            return;
        }

        /*
         * КРИТИЧЕСКИ ВАЖНО:
         *
         * На суше мы вообще не вмешиваемся
         * в updateVelocity().
         *
         * Значит:
         * - лодка падает;
         * - работает гравитация;
         * - работает столкновение с землёй;
         * - нет левитации;
         * - двигатель не толкает лодку по суше.
         *
         * Когда она попадёт в воду, этот метод
         * начнёт перехватывать ванильную физику.
         */
        if (!selfPropellingBoat.isTouchingWater()) {
            return;
        }

        boolean clientSide =
                selfPropellingBoat
                        .getWorld()
                        .isClient();

        /*
         * В воде:
         *
         * A/D = руль
         * W/S = игнорируются
         * двигатель = автоматическая тяга
         */
        selfPropellingBoat.applySelfPropulsion(
                pressingLeft,
                pressingRight,
                clientSide
        );

        /*
         * Не даём ванильной лодке после этого
         * перезаписать нашу горизонтальную скорость.
         */
        ci.cancel();
    }
}
