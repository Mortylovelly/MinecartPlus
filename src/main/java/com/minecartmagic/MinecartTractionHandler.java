package com.minecartmagic;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class MinecartTractionHandler {

    private static final double SPEED_PER_LEVEL = 0.30;
    private static final double MAX_MULTIPLIER = 2.50;

    public static void init() {
        ServerTickEvents.END_WORLD_TICK.register(MinecartTractionHandler::onWorldTick);
    }

    private static void onWorldTick(ServerWorld world) {
        Box worldBox = new Box(
                -30_000_000,
                world.getBottomY(),
                -30_000_000,
                30_000_000,
                world.getTopY(),
                30_000_000
        );

        for (AbstractMinecartEntity minecart : world.getEntitiesByClass(
                AbstractMinecartEntity.class,
                worldBox,
                AbstractMinecartEntity::isAlive
        )) {
            int level = ModEnchantments.getTractionLevel(
                    minecart.getPickBlockStack()
            );

            if (level <= 0) {
                continue;
            }

            double multiplier = 1.0 + SPEED_PER_LEVEL * level;
            multiplier = Math.min(multiplier, MAX_MULTIPLIER);

            Vec3d velocity = minecart.getVelocity();

            double horizontalSpeed = velocity.horizontalLength();

            if (horizontalSpeed < 0.000001) {
                continue;
            }

            double vanillaMaxSpeed = minecart.getMaxSpeed();
            double targetMaxSpeed = vanillaMaxSpeed * multiplier;

            double targetSpeed = Math.min(
                    horizontalSpeed * multiplier,
                    targetMaxSpeed
            );

            double scale = targetSpeed / horizontalSpeed;

            minecart.setVelocity(
                    velocity.x * scale,
                    velocity.y,
                    velocity.z * scale
            );
        }
    }
}
