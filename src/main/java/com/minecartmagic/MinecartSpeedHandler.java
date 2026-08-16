package com.minecartmagic;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class MinecartSpeedHandler {

    /*
     * Область поиска загруженных вагонеток.
     */
    private static final Box SEARCH_BOX = new Box(
            -30_000_000.0D,
            -2_048.0D,
            -30_000_000.0D,
            30_000_000.0D,
            2_048.0D,
            30_000_000.0D
    );

    /*
     * Максимальная ванильная скорость вагонетки.
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

            int level = getTractionLevel(minecart);

            /*
             * Обычные вагонетки вообще НЕ трогаем.
             *
             * Поэтому ванильная скорость обычных вагонеток
             * не изменяется.
             */
            if (level <= 0) {
                continue;
            }

            /*
             * Тяга действует только на рельсах.
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
             * Максимальная скорость:
             *
             * Vanilla = 0.40
             * Тяга I  = 0.52
             * Тяга II = 0.64
             * Тяга III = 0.76
             */
            double maximumSpeed =
                    VANILLA_MAX_SPEED * multiplier;

            /*
             * Увеличиваем текущую скорость,
             * но не выше максимума уровня.
             */
            double targetSpeed = Math.min(
                    horizontalSpeed * multiplier,
                    maximumSpeed
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

    private static int getTractionLevel(
            MinecartEntity minecart
    ) {
        /*
         * Проверяем начиная с III уровня.
         */
        for (int level = 3; level >= 1; level--) {

            if (minecart.getCommandTags().contains(
                    MinecartPlacementHandler
                            .getTractionTag(level)
            )) {
                return level;
            }
        }

        return 0;
    }
}
