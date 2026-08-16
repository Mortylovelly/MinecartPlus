package com.minecartmagic;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class MinecartTractionHandler {

    private static final double BASE_MAX_SPEED = 0.4D;

    private MinecartTractionHandler() {
    }

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(MinecartTractionHandler::tick);
    }

    private static void tick(MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) {

            Box searchBox = new Box(
                    world.getWorldBorder().getBoundWest(),
                    world.getBottomY(),
                    world.getWorldBorder().getBoundNorth(),
                    world.getWorldBorder().getBoundEast(),
                    world.getTopY(),
                    world.getWorldBorder().getBoundSouth()
            );

            for (AbstractMinecartEntity minecart : world.getEntitiesByClass(
                    AbstractMinecartEntity.class,
                    searchBox,
                    minecart -> true
            )) {
                applyTraction(minecart);
            }
        }
    }

    private static void applyTraction(AbstractMinecartEntity minecart) {
        int level = ModEnchantments.getTractionLevel(
                minecart.getPickBlockStack()
        );

        if (level <= 0) {
            return;
        }

        if (!minecart.isOnRail()) {
            return;
        }

        Vec3d velocity = minecart.getVelocity();

        double horizontalSpeed = Math.sqrt(
                velocity.x * velocity.x +
                velocity.z * velocity.z
        );

        if (horizontalSpeed <= BASE_MAX_SPEED) {
            return;
        }

        double multiplier = switch (level) {
            case 1 -> 1.30D;
            case 2 -> 1.60D;
            default -> 1.90D;
        };

        double maxSpeed = BASE_MAX_SPEED * multiplier;

        if (horizontalSpeed <= maxSpeed) {
            return;
        }

        double scale = maxSpeed / horizontalSpeed;

        minecart.setVelocity(
                velocity.x * scale,
                velocity.y,
                velocity.z * scale
        );
    }
}
