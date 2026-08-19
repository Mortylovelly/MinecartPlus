package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
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
         * Работаем только с нашей самоходной лодкой.
         */
        if (!(boat instanceof SelfPropellingBoatEntity selfPropellingBoat)) {
            return;
        }

        /*
         * Нет топлива -> двигатель выключен.
         */
        if (!selfPropellingBoat.hasFuel()) {
            return;
        }

        /*
         * На суше двигатель не тянет.
         */
        if (!selfPropellingBoat.isTouchingWater()) {
            return;
        }

        /*
         * КРИТИЧЕСКОЕ МЕСТО.
         *
         * Перед каждым расчётом двигателя берём НАСТОЯЩИЙ
         * уровень minecartmagic:tailwind с entity.
         *
         * 0 = нет зачарования
         * 1 = Tailwind I
         * 2 = Tailwind II
         * 3 = Tailwind III
         */
        int tailwindLevel =
                ModEnchantments.getTailwindLevel(
                        selfPropellingBoat
                );

        /*
         * Если attachment уже содержит зачарование,
         * обновляем синхронизированное значение двигателя.
         */
        if (tailwindLevel > 0) {
            selfPropellingBoat.setEngineTailwindLevel(
                    Math.min(
                            3,
                            tailwindLevel
                    )
            );
        }

        /*
         * Теперь applySelfPropulsion() получает
         * актуальный уровень Попутного ветра.
         *
         * Его собственная формула:
         *
         * 0  = 0.45
         * I  = 0.62
         * II = 0.72
         * III= 0.84
         */
        selfPropellingBoat.applySelfPropulsion();
    }
}
