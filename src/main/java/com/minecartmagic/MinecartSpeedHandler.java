package com.minecartmagic;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class MinecartSpeedHandler {

    /*
     * Весь мир не перебираем через несуществующий API WorldBorder.
     *
     * Это большая область, достаточная для обычного Minecraft-мира.
     * getEntitiesByClass работает только с реально загруженными сущностями
     * в этой области.
     */
    private static final Box SEARCH_BOX = new Box(
            -30_000_000.0D,
            -2_048.0D,
            -30_000_000.0D,
            30_000_000.0D,
            2_048.0D,
            30_000_000.0D
    );

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

            int level = MinecartTractionHandler.getTractionLevel(minecart);

            /*
             * Вагонетки без зачарования вообще не трогаем.
             *
             * Это важно: ванильная вагонетка должна двигаться
             * абсолютно так же, как без мода.
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
             * Стоящую вагонетку не запускаем самостоятельно.
             */
            if (horizontalSpeed <= 0.00001D) {
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
                default -> 1.90D;
            };

            /*
             * Максимальная скорость:
             *
             * vanilla = 0.40
             * I       = 0.52
             * II      = 0.64
             * III     = 0.76
             */
            double maximumSpeed = 0.40D * multiplier;

            /*
             * Плавно увеличиваем скорость.
             *
             * Важно: мы не умножаем скорость на multiplier
             * каждый тик, иначе вагонетка разгонялась бы слишком резко.
             */
            double targetSpeed = Math.min(
                    maximumSpeed,
                    horizontalSpeed * 1.08D
            );

            if (targetSpeed <= horizontalSpeed) {
                continue;
            }

            /*
             * Сохраняем направление движения.
             */
            double scale = targetSpeed / horizontalSpeed;

            minecart.setVelocity(
                    velocity.x * scale,
                    velocity.y,
                    velocity.z * scale
            );
        }
    }
}
