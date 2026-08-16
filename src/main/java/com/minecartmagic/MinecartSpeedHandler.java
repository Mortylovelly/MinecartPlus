package com.minecartmagic;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.MinecartEntity;
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

    private static void onWorldTick(net.minecraft.world.World world) {
        if (world.isClient()) {
            return;
        }

        for (Entity entity : world.iterateEntities()) {
            if (!(entity instanceof MinecartEntity minecart)) {
                continue;
            }

            int level = getTractionLevel(minecart);

            if (level <= 0) {
                continue;
            }

            /*
             * Вагонетка без движения не должна сама начинать ехать.
             */
            Vec3d velocity = minecart.getVelocity();

            double horizontalSpeed = Math.sqrt(
                    velocity.x * velocity.x +
                    velocity.z * velocity.z
            );

            if (horizontalSpeed < 0.0001D) {
                continue;
            }

            /*
             * Тяга:
             *
             * I   = 1.30x
             * II  = 1.60x
             * III = 1.90x
             */
            double multiplier = 1.0D + (0.30D * level);

            double maximumSpeed =
                    VANILLA_MAX_SPEED * multiplier;

            /*
             * Не разгоняем вагонетку бесконечно.
             */
            double targetSpeed =
                    Math.min(
                            horizontalSpeed * multiplier,
                            maximumSpeed
                    );

            /*
             * Не трогаем направление движения.
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
