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
         * -----------------------------------------------------
         * ГЛАВНОЕ ИЗМЕНЕНИЕ
         * -----------------------------------------------------
         *
         * Не используем ENGINE_TAILWIND_LEVEL вообще.
         *
         * Берём НАСТОЯЩИЙ уровень зачарования непосредственно
         * из entity.
         *
         * ModEnchantments.getTailwindLevel(BoatEntity):
         *
         * 1. Attachment
         * 2. command tag
         *
         * Поэтому уровень не может потеряться из-за отдельного
         * поля двигателя.
         */
        int tailwindLevel =
                ModEnchantments.getTailwindLevel(
                        selfPropellingBoat
                );

        /*
         * -----------------------------------------------------
         * СКОРОСТЬ
         * -----------------------------------------------------
         *
         * Без зачарования:
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
         * Получаем текущую скорость.
         */
        Vec3d velocity =
                selfPropellingBoat.getVelocity();

        /*
         * -----------------------------------------------------
         * НАПРАВЛЕНИЕ
         * -----------------------------------------------------
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
         * -----------------------------------------------------
         * A / D
         * -----------------------------------------------------
         */
        if (selfPropellingBoat.isEnginePressingLeft()
                && !selfPropellingBoat.isEnginePressingRight()) {

            selfPropellingBoat.setYaw(
                    selfPropellingBoat.getYaw()
                            - 2.5F
            );

            radians =
                    Math.toRadians(
                            selfPropellingBoat.getYaw()
                    );

            forward =
                    new Vec3d(
                            -Math.sin(radians),
                            0.0D,
                            Math.cos(radians)
                    );

        } else if (
                selfPropellingBoat.isEnginePressingRight()
                        && !selfPropellingBoat.isEnginePressingLeft()
        ) {

            selfPropellingBoat.setYaw(
                    selfPropellingBoat.getYaw()
                            + 2.5F
            );

            radians =
                    Math.toRadians(
                            selfPropellingBoat.getYaw()
                    );

            forward =
                    new Vec3d(
                            -Math.sin(radians),
                            0.0D,
                            Math.cos(radians)
                    );
        }

        /*
         * -----------------------------------------------------
         * ТЕКУЩАЯ СКОРОСТЬ ВПЕРЁД
         * -----------------------------------------------------
         */
        double forwardSpeed =
                velocity.x * forward.x
                        + velocity.z * forward.z;

        /*
         * Не разрешаем боковой или обратной скорости
         * увеличивать двигатель.
         */
        forwardSpeed =
                Math.max(
                        0.0D,
                        forwardSpeed
                );

        /*
         * -----------------------------------------------------
         * РАЗГОН
         * -----------------------------------------------------
         */
        double targetSpeed =
                forwardSpeed + 0.025D;

        targetSpeed =
                Math.min(
                        maximumSpeed,
                        targetSpeed
                );

        /*
         * Минимальная скорость работающего двигателя.
         *
         * Даже если лодка начала стоять:
         * двигатель сразу начинает тянуть.
         */
        if (targetSpeed < 0.45D) {

            targetSpeed =
                    Math.min(
                            0.45D,
                            maximumSpeed
                    );
        }

        /*
         * -----------------------------------------------------
         * ПРИМЕНЯЕМ ДВИГАТЕЛЬ
         * -----------------------------------------------------
         *
         * X/Z = наш двигатель
         * Y   = ванильная физика лодки
         *
         * Поэтому мы не ломаем:
         *
         * - плавучесть;
         * - гравитацию;
         * - падение;
         * - движение по воде.
         */
        selfPropellingBoat.setVelocity(
                forward.x * targetSpeed,
                velocity.y,
                forward.z * targetSpeed
        );

        selfPropellingBoat.velocityDirty = true;
    }
}
