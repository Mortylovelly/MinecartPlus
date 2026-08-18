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

        if (!(boat instanceof SelfPropellingBoatEntity selfPropellingBoat)) {
            return;
        }

        /*
         * Нет топлива:
         *
         * вообще ничего не делаем.
         * Полностью ванильная лодка.
         */
        if (!selfPropellingBoat.hasFuel()) {
            return;
        }

        /*
         * На суше НЕ вмешиваемся.
         *
         * Поэтому:
         * - работает гравитация;
         * - лодка падает;
         * - нет левитации;
         * - двигатель не толкает по земле.
         */
        if (!selfPropellingBoat.isTouchingWater()) {
            return;
        }

        boolean clientSide =
                selfPropellingBoat
                        .getWorld()
                        .isClient();

        /*
         * Здесь ванильная физика УЖЕ отработала.
         *
         * Мы теперь меняем только X/Z,
         * сохраняя её Y.
         */
        selfPropellingBoat.applySelfPropulsion(
                pressingLeft,
                pressingRight,
                clientSide
        );
    }
}
