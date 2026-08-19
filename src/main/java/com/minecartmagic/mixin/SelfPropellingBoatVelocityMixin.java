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

    private static final double BASE_ENGINE_SPEED = 0.45D;
    private static final double ENGINE_ACCELERATION = 0.025D;
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
         * Без топлива двигателя нет.
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
         * TAILWIND
         * =====================================================
         *
         * ГЛАВНОЕ:
         *
         * Здесь мы больше НЕ используем только
         * ENGINE_TAILWIND_LEVEL как источник скорости.
         *
         * Сначала напрямую читаем настоящий Tailwind
         * с entity через существующую Attachment-систему.
         *
         * Это та же самая система, которую использует
         * остальной мод.
         */
        int tailwindLevel =
                ModEnchantments.getTailwindLevel(
                        selfPropellingBoat
                );

        /*
         * Резервный источник для уже существующих
         * самоходных лодок / старых сохранений.
         */
        if (tailwindLevel <= 0) {

            tailwindLevel =
                    selfPropellingBoat.getEngineTailwindLevel();
        }

        /*
         * Не даём некорректным данным выйти за пределы
         * нашего зачарования I-III.
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
         * МАКСИМАЛЬНАЯ СКОРОСТЬ
         * =====================================================
         *
         * Без зачарования:
         *
         * 0.45
         *
         * Tailwind I:
         *
         * 0.62
         *
         * Tailwind II:
         *
         * 0.72
         *
         * Tailwind III:
         *
         * 0.84
         */
        double maximumSpeed =
                switch (tailwindLevel) {

                    case 1 -> 0.62D;

                    case 2 -> 0.72D;

                    case 3 -> 0.84D;

                    default -> BASE_ENGINE_SPEED;
                };

        /*
         * =====================================================
         * РУЛЕНИЕ
         * =====================================================
         *
         * A = влево
         * D = вправо
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
         * Текущая скорость.
         */
        Vec3d velocity =
                selfPropellingBoat.getVelocity();

        /*
         * =====================================================
         * НАПРАВЛЕНИЕ НОСА ЛОДКИ
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
         * СКОРОСТЬ ПО НАПРАВЛЕНИЮ ДВИГАТЕЛЯ
         * =====================================================
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

        targetSpeed =
                Math.min(
                        maximumSpeed,
                        targetSpeed
                );

        /*
         * Когда двигатель только включился,
         * лодка должна начать двигаться сразу.
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
         * ПРИМЕНЕНИЕ ДВИГАТЕЛЯ
         * =====================================================
         *
         * X/Z = двигатель
         * Y   = ванильная физика
         *
         * Поэтому не ломаем:
         *
         * - падение;
         * - плавучесть;
         * - вертикальную физику;
         * - переход вода/воздух.
         */
        selfPropellingBoat.setVelocity(
                forward.x * targetSpeed,
                velocity.y,
                forward.z * targetSpeed
        );

        selfPropellingBoat.velocityDirty = true;
    }
}
