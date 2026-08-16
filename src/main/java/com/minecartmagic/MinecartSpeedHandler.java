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
     * Максимальная скорость ванильной вагонетки
     * примерно 0.40 блока/тик.
     *
     * Тяга:
     * I   = 0.60
     * II  = 0.80
     * III = 1.00
     */
    private static final double SPEED_LEVEL_1 = 0.60D;
    private static final double SPEED_LEVEL_2 = 0.80D;
    private static final double SPEED_LEVEL_3 = 1.00D;

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

            int level = ModEnchantments.getTractionLevel(minecart);

            /*
             * Обычные вагонетки вообще не трогаем.
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
             * Вагонетка стоит.
             * Зачарование само её не запускает.
             */
            if (horizontalSpeed < 0.001D) {
                continue;
            }

            double maximumSpeed;

            switch (level) {
                case 1 -> maximumSpeed = SPEED_LEVEL_1;
                case 2 -> maximumSpeed = SPEED_LEVEL_2;
                default -> maximumSpeed = SPEED_LEVEL_3;
            }

            /*
             * Если вагонетка уже быстрее максимума этого уровня,
             * ничего не делаем.
             */
            if (horizontalSpeed >= maximumSpeed) {
                continue;
            }

            /*
             * Направление движения сохраняем.
             */
            double directionX = velocity.x / horizontalSpeed;
            double directionZ = velocity.z / horizontalSpeed;

            /*
             * Постепенно увеличиваем скорость.
             *
             * Это важно: мы не телепортируем вагонетку
             * и не меняем её позицию.
             */
            double newSpeed = Math.min(
                    maximumSpeed,
                    horizontalSpeed + 0.035D
            );

            minecart.setVelocity(
                    directionX * newSpeed,
                    velocity.y,
                    directionZ * newSpeed
            );
        }
    }
}
