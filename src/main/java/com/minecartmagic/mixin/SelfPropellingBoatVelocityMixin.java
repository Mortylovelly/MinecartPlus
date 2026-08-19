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
         * Здесь НЕТ ModEnchantments.getTailwindLevel().
         *
         * Здесь НЕТ Attachment.
         *
         * Здесь НЕТ command tag.
         *
         * Берём именно то значение, которое было записано
         * в двигатель самоходной лодки.
         *
         * Это и есть уровень зачарования, с которым
         * была установлена лодка.
         */
        int tailwindLevel =
                selfPropellingBoat.getEngineTailwindLevel();

        /*
         * Защита диапазона:
         *
         * 0 = нет
         * 1 = I
         * 2 = II
         * 3 = III
         */
        tailwindLevel =
                Math.max(
                        0,
                        Math.min(
                                3,
                                tailwindLevel
                        )
                );

        /*
         * =====================================================
         * СКОРОСТЬ ДВИГАТЕЛЯ
         * =====================================================
         *
         * БЕЗ зачарования:
         * 0.45
         *
         * Tailwind I:
         * 0.62
         *
         * Tailwind II:
         * 0.72
         *
         * Tailwind III:
         * 0.84
         */
        double maximumSpeed =
                switch (tailwindLevel) {

                    case 1 -> 0.62D;

                    case 2 -> 0.72D;

                    case 3 -> 0.84D;

                    default -> 0.45D;
                };

        /*
         * =====================================================
         * РУЛЕНИЕ
         * =====================================================
         */
        if (selfPropellingBoat.isEnginePressingLeft()
                && !selfPropellingBoat.isEnginePressingRight()) {

            selfPropellingBoat.setYaw(
                    selfPropellingBoat.getYaw()
                            - 2.5F
            );

        } else if (
                selfPropellingBoat.isEnginePressingRight()
                        && !selfPropellingBoat.isEnginePressingLeft()
        ) {

            selfPropellingBoat.setYaw(
                    selfPropellingBoat.getYaw()
                            + 2.5F
            );
        }

        /*
         * =====================================================
         * ТЕКУЩАЯ СКОРОСТЬ
         * =====================================================
         */
        var velocity =
                selfPropellingBoat.getVelocity();

        /*
         * Направление носа лодки.
         */
        double radians =
                Math.toRadians(
                        selfPropellingBoat.getYaw()
                );

        double forwardX =
                -Math.sin(radians);

        double forwardZ =
                Math.cos(radians);

        /*
         * Скорость, направленная вперёд.
         */
        double forwardSpeed =
                velocity.x * forwardX
                        + velocity.z * forwardZ;

        forwardSpeed =
                Math.max(
                        0.0D,
                        forwardSpeed
                );

        /*
         * Плавный разгон.
         */
        double targetSpeed =
                forwardSpeed + 0.025D;

        /*
         * Главное ограничение:
         *
         * Tailwind I/II/III реально меняют максимальную
         * скорость САМОГО ДВИГАТЕЛЯ.
         */
        targetSpeed =
                Math.min(
                        targetSpeed,
                        maximumSpeed
                );

        /*
         * Если двигатель только начал работать,
         * не оставляем лодку на нулевой скорости.
         */
        if (targetSpeed < 0.45D) {

            targetSpeed =
                    Math.min(
                            0.45D,
                            maximumSpeed
                    );
        }

        /*
         * =====================================================
         * ПРИМЕНЯЕМ СКОРОСТЬ
         * =====================================================
         *
         * X/Z = двигатель
         * Y   = ванильная физика
         */
        selfPropellingBoat.setVelocity(
                forwardX * targetSpeed,
                velocity.y,
                forwardZ * targetSpeed
        );

        selfPropellingBoat.velocityDirty = true;
    }
}
