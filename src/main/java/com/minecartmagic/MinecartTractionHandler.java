package com.minecartmagic;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Vec3d;

public final class MinecartTractionHandler {

    private static final double VANILLA_MAX_SPEED = 0.40D;
    private static final double SPEED_PER_LEVEL = 0.30D;

    private MinecartTractionHandler() {
    }

    public static void init() {
        ServerTickEvents.END_WORLD_TICK.register(MinecartTractionHandler::tick);
    }

    private static void tick(ServerWorld world) {
        for (AbstractMinecartEntity minecart : world.getEntitiesByType(
                TypeFilter.instanceOf(AbstractMinecartEntity.class),
                minecart -> true
        )) {
            int level = ModEnchantments.getTractionLevel(minecart.getPickBlockStack());

            if (level <= 0) {
                continue;
            }

            Vec3d velocity = minecart.getVelocity();

            double horizontalSpeed = Math.sqrt(
                    velocity.x * velocity.x +
                    velocity.z * velocity.z
            );

            if (horizontalSpeed < 0.00001D) {
                continue;
            }

            double targetSpeed = VANILLA_MAX_SPEED * (1.0D + SPEED_PER_LEVEL * level);

            if (horizontalSpeed < targetSpeed) {
                double multiplier = targetSpeed / horizontalSpeed;

                minecart.setVelocity(
                        velocity.x * multiplier,
                        velocity.y,
                        velocity.z * multiplier
                );
            }
        }
    }
}
