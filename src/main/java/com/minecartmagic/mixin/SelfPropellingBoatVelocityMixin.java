package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatEntity.class)
public abstract class SelfPropellingBoatVelocityMixin {

    /*
     * =====================================================
     * БАЗОВАЯ СКОРОСТЬ ДВИГАТЕЛЯ
     * =====================================================
     *
     * Без Попутного ветра:
     *
     * 0.45
     *
     * I:
     *
     * 0.62
     *
     * II:
     *
     * 0.72
     *
     * III:
     *
     * 0.84
     */
    private static final double BASE_ENGINE_SPEED = 0.45D;

    private static final double TAILWIND_I_SPEED = 0.62D;

    private static final double TAILWIND_II_SPEED = 0.72D;

    private static final double TAILWIND_III_SPEED = 0.84D;

    /*
     * Скорость разгона.
     */
    private static final double ENGINE_ACCELERATION = 0.025D;

    /*
     * Управление A/D.
     */
    private static final float ENGINE_STEERING_SPEED = 2.5F;

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
         * На суше двигатель не тянет.
         */
        if (!selfPropellingBoat.isTouchingWater()) {

            return;
        }

        /*
         * =====================================================
         * ПОЛУЧАЕМ НАСТОЯЩИЙ TAILWIND
         * =====================================================
         *
         * Никакого ENGINE_TAILWIND_LEVEL здесь больше нет.
         *
         * Получаем непосредственно зачарование,
         * которое лежит на самой самоходной лодке.
         *
         * ModEnchantments.getTailwindLevel(BoatEntity)
         * использует:
         *
         * 1. TAILWIND Attachment
         * 2. command tag
         */
        int tailwindLevel =
                ModEnchantments.getTailwindLevel(
                        selfPropellingBoat
                );

        /*
         * Защита диапазона.
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
         * Вот здесь непосредственно меняется
         * скорость самого двигателя.
         *
         * Никакого другого значения скорости
         * из SelfPropellingBoatEntity больше
         * не берём.
         */
        double maximumSpeed;

        switch (tailwindLevel) {

            case 1 -> maximumSpeed =
                    TAILWIND_I_SPEED;

            case 2 -> maximumSpeed =
                    TAILWIND_II_SPEED;

            case 3 -> maximumSpeed =
                    TAILWIND_III_SPEED;

            default -> maximumSpeed =
                    BASE_ENGINE_SPEED;
        }

        /*
         * =====================================================
         * РУЛЕНИЕ A / D
         * =====================================================
         */
        if (selfPropellingBoat.isEnginePressingLeft()
                && !selfPropellingBoat.isEnginePressingRight()) {

            selfPropellingBoat.setYaw(
                    selfPropellingBoat.getYaw()
                            - ENGINE_STEERING_SPEED
            );

        } else if (
                selfPropellingBoat.isEnginePressingRight()
                        && !selfPropellingBoat.isEnginePressingLeft()
        ) {

            selfPropellingBoat.setYaw(
                    selfPropellingBoat.getYaw()
                            + ENGINE_STEERING_SPEED
            );
        }

        /*
         * =====================================================
         * ТЕКУЩАЯ СКОРОСТЬ
         * =====================================================
         */
        Vec3d velocity =
                selfPropellingBoat.getVelocity();

        /*
         * =====================================================
         * НАПРАВЛЕНИЕ ЛОДКИ
         * =====================================================
         */
        double radians =
                Math.toRadians(
                        selfPropellingBoat.getYaw()
                );

        Vec3d forward =
                new Vec3d(
                        -Math.sin(radians),
                        0.0D,
                        Math.cos(radians)
                );

        /*
         * =====================================================
         * СКОРОСТЬ ВПЕРЁД
         * =====================================================
         *
         * Берём только составляющую скорости,
         * направленную туда, куда смотрит лодка.
         */
        double forwardSpeed =
                velocity.x * forward.x
                        + velocity.z * forward.z;

        forwardSpeed =
                Math.max(
                        0.0D,
                        forwardSpeed
                );

        /*
         * =====================================================
         * РАЗГОН
         * =====================================================
         */
        double targetSpeed =
                forwardSpeed
                        + ENGINE_ACCELERATION;

        /*
         * Главный лимит.
         *
         * Без зачарования:
         * 0.45
         *
         * I:
         * 0.62
         *
         * II:
         * 0.72
         *
         * III:
         * 0.84
         */
        targetSpeed =
                Math.min(
                        targetSpeed,
                        maximumSpeed
                );

        /*
         * Если лодка стоит,
         * двигатель должен начать тянуть
         * сразу.
         */
        if (targetSpeed < BASE_ENGINE_SPEED) {

            targetSpeed =
                    Math.min(
                            BASE_ENGINE_SPEED,
                            maximumSpeed
                    );
        }

        /*
         * =====================================================
         * ПРИМЕНЯЕМ СКОРОСТЬ
         * =====================================================
         *
         * X/Z:
         * двигатель.
         *
         * Y:
         * ванильная физика лодки.
         *
         * Поэтому вертикальная физика не меняется.
         */
        selfPropellingBoat.setVelocity(
                forward.x * targetSpeed,
                velocity.y,
                forward.z * targetSpeed
        );

        selfPropellingBoat.velocityDirty = true;
    }
}
