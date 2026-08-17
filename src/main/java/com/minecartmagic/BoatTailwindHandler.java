package com.minecartmagic;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public final class BoatTailwindHandler {

    private BoatTailwindHandler() {
    }

    public static void init() {
        ServerTickEvents.END_WORLD_TICK.register(
                BoatTailwindHandler::onWorldTick
        );
    }

    private static void onWorldTick(
            ServerWorld world
    ) {
        for (var entity : world.iterateEntities()) {

            if (!(entity instanceof BoatEntity boat)) {
                continue;
            }

            if (!boat.isAlive() || !boat.isTouchingWater()) {
                continue;
            }

            int level =
                    ModEnchantments.getTailwindLevel(boat);

            if (level <= 0) {
                continue;
            }

            /*
             * Попутный ветер:
             *
             * I   = 0.60 блока/тик
             * II  = 0.80 блока/тик
             * III = 1.00 блока/тик
             */
            double maxSpeed = switch (level) {
                case 1 -> 0.60D;
                case 2 -> 0.80D;
                default -> 1.00D;
            };

            /*
             * Скорость набирается постепенно,
             * чтобы лодка не получала резкий телепорт
             * скорости каждый тик.
             */
            double acceleration = switch (level) {
                case 1 -> 0.020D;
                case 2 -> 0.032D;
                default -> 0.045D;
            };

            Vec3d velocity =
                    boat.getVelocity();

            double horizontalSpeed =
                    Math.sqrt(
                            velocity.x * velocity.x
                                    + velocity.z * velocity.z
                    );

            /*
             * Лодка стоит — ждём, пока ванильная
             * физика сама начнёт движение.
             */
            if (horizontalSpeed <= 0.00001D) {
                continue;
            }

            if (horizontalSpeed >= maxSpeed) {
                continue;
            }

            double newSpeed =
                    Math.min(
                            horizontalSpeed + acceleration,
                            maxSpeed
                    );

            double scale =
                    newSpeed / horizontalSpeed;

            boat.setVelocity(
                    velocity.x * scale,
                    velocity.y,
                    velocity.z * scale
            );
        }
    }
}
