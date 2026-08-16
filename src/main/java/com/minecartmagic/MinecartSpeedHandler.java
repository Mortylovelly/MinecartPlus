package com.minecartmagic;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class MinecartSpeedHandler {

    private static final double VANILLA_MAX_SPEED = 0.4D;

    private MinecartSpeedHandler() {
    }

    public static void init() {
        ServerTickEvents.END_WORLD_TICK.register(
                MinecartSpeedHandler::onWorldTick
        );
    }

    private static void onWorldTick(ServerWorld world) {

        Box worldBox = world.getWorldBorder().getBoundaries();

        for (MinecartEntity minecart : world.getEntitiesByClass(
                MinecartEntity.class,
                worldBox,
                minecart -> true
        )) {

            int level = getTractionLevel(minecart);

            /*
             * Вагонетки без Тяги вообще не трогаем.
             */
            if (level <= 0) {
                continue;
            }

            Vec3d velocity = minecart.getVelocity();

            double horizontalSpeed = Math.sqrt(
                    velocity.x * velocity.x +
                    velocity.z * velocity.z
            );

            /*
             * Стоящую вагонетку не начинаем двигать.
             */
            if (horizontalSpeed < 0.0001D) {
                continue;
            }

            /*
             * Тяга:
             *
             * I   = +30%
             * II  = +60%
             * III = +90%
             */
            double multiplier = 1.0D + (0.30D * level);

            /*
             * Максимальная скорость:
             *
             * vanilla = 0.40
             * Тяга I = 0.52
             * Тяга II = 0.64
             * Тяга III = 0.76
             */
            double maximumSpeed =
                    VANILLA_MAX_SPEED * multiplier;

            /*
             * Увеличиваем текущую горизонтальную скорость,
             * но не превышаем лимит уровня Тяги.
             */
            double targetSpeed = Math.min(
                    horizontalSpeed * multiplier,
                    maximumSpeed
            );

            /*
             * Сохраняем направление движения.
             */
            double directionX = velocity.x / horizontalSpeed;
            double directionZ = velocity.z / horizontalSpeed;

            minecart.setVelocity(
                    directionX * targetSpeed,
                    velocity.y,
                    directionZ * targetSpeed
            );
        }
    }

    private static int getTractionLevel(MinecartEntity minecart) {

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
