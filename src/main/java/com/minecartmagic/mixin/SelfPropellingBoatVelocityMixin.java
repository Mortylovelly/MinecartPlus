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

        if (!(boat
                instanceof SelfPropellingBoatEntity selfPropellingBoat)) {
            return;
        }

        /*
         * НЕТ горящего топлива:
         *
         * вообще ничего не делаем.
         *
         * Это полностью ванильная лодка.
         */
        if (!selfPropellingBoat.hasFuel()) {
            return;
        }

        boolean clientSide =
                selfPropellingBoat
                        .getWorld()
                        .isClient();

        selfPropellingBoat.applySelfPropulsion(
                pressingLeft,
                pressingRight,
                clientSide
        );

        /*
         * При работающем двигателе ванильный
         * updateVelocity больше НЕ выполняется.
         *
         * Поэтому W/S не смогут добавить скорость,
         * затормозить или создать новый glide.
         */
        ci.cancel();
    }
}
