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
         * Только самоходная лодка.
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
         * ГЛАВНОЕ ИЗМЕНЕНИЕ:
         *
         * Сначала читаем настоящий attachment
         * minecartmagic:tailwind.
         *
         * Это реальный уровень зачарования лодки:
         *
         * 0 = нет
         * 1 = Tailwind I
         * 2 = Tailwind II
         * 3 = Tailwind III
         */
        int tailwindLevel =
                ModEnchantments.getTailwindLevel(
                        selfPropellingBoat
                );

        /*
         * Если attachment по какой-либо причине ещё
         * не синхронизирован на этой стороне,
         * используем сохранённое значение двигателя.
         */
        if (tailwindLevel <= 0) {
            tailwindLevel =
                    selfPropellingBoat.getEngineTailwindLevel();
        }

        tailwindLevel =
                Math.max(
                        0,
                        Math.min(
                                3,
                                tailwindLevel
                        )
                );

        /*
         * Сохраняем актуальный уровень в tracker двигателя.
         */
        if (tailwindLevel > 0) {
            selfPropellingBoat.setEngineTailwindLevel(
                    tailwindLevel
            );
        }

        /*
         * ЛИМИТ СКОРОСТИ ДВИГАТЕЛЯ.
         *
         * Без Попутного ветра = 0.45
         * I  = 0.62
         * II = 0.72
         * III = 0.84
         */
        double maximumSpeed =
                switch (tailwindLevel) {
                    case 1 -> 0.62D;
                    case 2 -> 0.72D;
                    case 3 -> 0.84D;
                    default -> BASE_ENGINE_SPEED;
                };

        /*
         * A / D — поворот.
         */
        if (selfPropellingBoat.isEnginePressingLeft()
                && !selfPropellingBoat.isEnginePressingRight()) {

            selfPropellingBoat.setYaw(
                    selfPropellingBoat.getYaw() - 2.5F
            );

        } else if (
                selfPropellingBoat.isEnginePressingRight()
                        && !selfPropellingBoat.isEnginePressingLeft()
        ) {

            selfPropellingBoat.setYaw(
                    selfPropellingBoat.getYaw() + 2.5F
            );
        }

        Vec3d velocity =
                selfPropellingBoat.getVelocity();

        /*
         * Направление носа.
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
         * Скорость вдоль направления лодки.
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
         * Разгон двигателя.
         */
        double targetSpeed =
                forwardSpeed + ENGINE_ACCELERATION;

        /*
         * Здесь Tailwind реально влияет
         * на предел скорости.
         */
        targetSpeed =
                Math.min(
                        targetSpeed,
                        maximumSpeed
                );

        /*
         * При запуске двигателя лодка должна сразу
         * начать двигаться.
         */
        if (targetSpeed < BASE_ENGINE_SPEED) {
            targetSpeed =
                    Math.min(
                            BASE_ENGINE_SPEED,
                            maximumSpeed
                    );
        }

        /*
         * X/Z — двигатель.
         * Y — полностью ванильная физика.
         */
        selfPropellingBoat.setVelocity(
                forwardX * targetSpeed,
                velocity.y,
                forwardZ * targetSpeed
        );

        selfPropellingBoat.velocityDirty = true;
    }
}
