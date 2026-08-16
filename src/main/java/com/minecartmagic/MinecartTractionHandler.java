package com.minecartmagic;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class MinecartTractionHandler {

    private static final double SPEED_MULTIPLIER_PER_LEVEL = 0.30;
    private static final double MAX_SPEED_MULTIPLIER = 2.50;

    public static void init() {
        ServerTickEvents.END_WORLD_TICK.register(MinecartTractionHandler::onWorldTick);
    }

    private static void onWorldTick(ServerWorld world) {
        for (AbstractMinecartEntity minecart : world.getEntitiesByClass(
                AbstractMinecartEntity.class,
                minecart -> minecart.isAlive()
        )) {
            int level = ModEnchantments.getTractionLevel(
                    minecart.getPickBlockStack()
            );

            if (level <= 0) {
                continue;
            }

            double multiplier = 1.0 + (SPEED_MULTIPLIER_PER_LEVEL * level);
            multiplier = Math.min(multiplier, MAX_SPEED_MULTIPLIER);

            Vec3d velocity = minecart.getVelocity();

            if (velocity.horizontalLengthSquared() <= 0.000001) {
                continue;
            }

            Vec3d boostedVelocity = new Vec3d(
                    velocity.x * multiplier,
                    velocity.y,
                    velocity.z * multiplier
            );

            double maxHorizontalSpeed =
                    minecart.getMaxSpeed(world) * multiplier;

            double horizontalSpeed = boostedVelocity.horizontalLength();

            if (horizontalSpeed > maxHorizontalSpeed) {
                double scale = maxHorizontalSpeed / horizontalSpeed;

                boostedVelocity = new Vec3d(
                        boostedVelocity.x * scale,
                        boostedVelocity.y,
                        boostedVelocity.z * scale
                );
            }

            minecart.setVelocity(boostedVelocity);
        }
    }
}
