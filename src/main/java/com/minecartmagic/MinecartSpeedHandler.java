package com.minecartmagic;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class MinecartSpeedHandler {

    private static final Box SEARCH_BOX = new Box(
            -30_000_000.0D,
            -2_048.0D,
            -30_000_000.0D,
            30_000_000.0D,
            2_048.0D,
            30_000_000.0D
    );

    /*
     * Базовая максимальная скорость ванильной вагонетки.
     */
    private static final double VANILLA_MAX_SPEED = 0.40D;

    private MinecartSpeedHandler() {
    }

    public static void init() {
        ServerTickEvents.END_WORLD_TICK.register(
                MinecartSpeedHandler::onWorldTick
        );
    }

    private static void onWorldTick(ServerWorld world) {

        for (MinecartEntity minecart : world.getEntitiesByClass(
                MinecartEntity.class,
                SEARCH_BOX,
                entity -> true
        )) {

            /*
             * Получаем уровень Тяги из command tag,
             * который ставится при размещении зачарованной вагонетки.
             */
            int level = getTractionLevel(minecart);

            /*
             * Обычные вагонетки вообще не изменяем.
             */
            if (level <= 0) {
                continue;
            }

            /*
             * Тяга работает только на рельсах.
             */
            if (!minecart.isOnRail()) {
                continue;
            }

            Vec3d velocity = minecart.getVelocity();

            double horizontalSpeed = Math.sqrt(
                    velocity.x * velocity.x
                            + velocity.z * velocity.z
            );

            /*
             * Стоящую вагонетку самостоятельно не запускаем.
             */
            if (horizontalSpeed <= 0.0001D) {
                continue;
            }

            /*
             * Тяга:
             *
             * I   = +30%
             * II  = +60%
             * III = +90%
             */
            double multiplier = switch (level) {
                case 1 -> 1.30D;
                case 2 -> 1.60D;
                case 3 -> 1.90D;
                default -> 1.00D;
            };

            /*
             * Новый предел скорости.
             *
             * Vanilla: 0.40
             * I:       0.52
             * II:      0.64
             * III:     0.76
             */
            double maximumSpeed =
                    VANILLA_MAX_SPEED * multiplier;

            /*
             * Ускоряем вагонетку постепенно.
             */
            double targetSpeed = Math.min(
                    maximumSpeed,
                    horizontalSpeed + 0.035D
            );

            if (targetSpeed <= horizontalSpeed) {
                continue;
            }

            /*
             * Сохраняем направление движения.
             */
            double directionX =
                    velocity.x / horizontalSpeed;

            double directionZ =
                    velocity.z / horizontalSpeed;

            minecart.setVelocity(
                    directionX * targetSpeed,
                    velocity.y,
                    directionZ * targetSpeed
            );
        }
    }

    /*
     * Получаем уровень Тяги непосредственно
     * из command tags Entity.
     */
    private static int getTractionLevel(
            MinecartEntity minecart
    ) {

        /*
         * Проверяем от максимального уровня к минимальному.
         */
        for (int level = 3; level >= 1; level--) {

            if (minecart.getCommandTags().contains(
                    MinecartPlacementHandler.getTractionTag(level)
            )) {
                return level;
            }
        }

        return 0;
    }
}
