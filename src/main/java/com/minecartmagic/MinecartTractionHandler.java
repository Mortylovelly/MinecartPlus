package com.minecartmagic;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class MinecartTractionHandler {

    private static final double SPEED_MULTIPLIER_PER_LEVEL = 0.30D;

    public static void init() {
        ServerTickEvents.END_WORLD_TICK.register(MinecartTractionHandler::onWorldTick);
    }

    private static void onWorldTick(ServerWorld world) {
        for (AbstractMinecartEntity minecart : world.getEntitiesByClass(
                AbstractMinecartEntity.class,
                minecart -> minecart.getBoundingBox().expand(0.1D),
                minecart -> ModEnchantments.getTractionLevel(minecart.getPickBlockStack()) > 0
        )) {
            int level = ModEnchantments.getTractionLevel(minecart.getPickBlockStack());

            if (level <= 0) {
                continue;
            }

            double multiplier = 1.0D + (SPEED_MULTIPLIER_PER_LEVEL * level);

            Vec3d velocity = minecart.getVelocity();

            if (velocity.lengthSquared() > 0.000001D) {
                minecart.setVelocity(
                        velocity.x * multiplier,
                        velocity.y,
                        velocity.z * multiplier
                );
            }
        }
    }
}
