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
         * БЕЗ топлива:
         *
         * вообще ничего не меняем.
         *
         * Значит BoatEntity полностью ванильная:
         * W/A/S/D работают как обычно.
         */
        if (!selfPropellingBoat.hasFuel()) {
            return;
        }

        boolean clientSide =
                selfPropellingBoat.getWorld().isClient();

        selfPropellingBoat.applySelfPropulsion(
                pressingLeft,
                pressingRight,
                clientSide
        );

        /*
         * Только теперь отключаем ванильную
         * физику лодки.
         *
         * Это предотвращает:
         *
         * W → движение
         * S → задний ход
         * ванильное торможение
         * перезапись нашей скорости
         *
         * при работающем двигателе.
         */
        ci.cancel();
    }
}
