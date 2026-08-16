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

            if (level <= 0) {
                continue;
            }

            if (!minecart.isOnRail()) {
                continue;
            }

            Vec3d velocity = minecart.getVelocity();

            double horizontalSpeed = Math.sqrt(
                    velocity.x * velocity.x
                            + velocity.z * velocity.z
            );

            if (horizontalSpeed <= 0.00001D) {
                continue;
            }

            double multiplier = switch (level) {
                case 1 -> 1.30D;
                case 2 -> 1.60D;
                default -> 1.90D;
            };

            double maximumSpeed = 0.40D * multiplier;

            double targetSpeed = Math.min(
                    maximumSpeed,
                    horizontalSpeed * 1.08D
            );

            if (targetSpeed <= horizontalSpeed) {
                continue;
            }

            double scale = targetSpeed / horizontalSpeed;

            minecart.setVelocity(
                    velocity.x * scale,
                    velocity.y,
                    velocity.z * scale
            );
        }
    }
}
