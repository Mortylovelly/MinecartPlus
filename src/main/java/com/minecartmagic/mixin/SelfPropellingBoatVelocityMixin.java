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
         * =====================================================
         * ПОЛУЧАЕМ НАСТОЯЩИЙ TAILWIND
         * =====================================================
         *
         * Это основной источник.
         *
         * ModEnchantments.getTailwindLevel(entity)
         * проверяет:
         *
         * 1. Attachment
         * 2. command tag
         */
        int actualTailwindLevel =
                ModEnchantments.getTailwindLevel(
                        selfPropellingBoat
                );

        /*
         * =====================================================
         * ТЕКУЩИЙ TRACKER
         * =====================================================
         */
        int engineTailwindLevel =
                selfPropellingBoat.getEngineTailwindLevel();

        /*
         * Если реальный Tailwind присутствует,
         * синхронизируем tracker.
         *
         * Главное:
         *
         * НИКОГДА не записываем 0 поверх уже установленного
         * уровня только потому, что Attachment ещё не успел
         * появиться на конкретном тике.
         */
        if (actualTailwindLevel > 0) {

            if (actualTailwindLevel
                    != engineTailwindLevel) {

                selfPropellingBoat.setEngineTailwindLevel(
                        actualTailwindLevel
                );
            }

        } else if (engineTailwindLevel > 0) {

            /*
             * Уровень уже установлен непосредственно
             * на самоходной лодке.
             *
             * Оставляем его.
             */
            actualTailwindLevel =
                    engineTailwindLevel;
        }

        /*
         * =====================================================
         * ПРИМЕНЯЕМ ДВИГАТЕЛЬ
         * =====================================================
         *
         * applySelfPropulsion()
         * теперь использует обновлённый tracker.
         */
        selfPropellingBoat.applySelfPropulsion();
    }
}
