package com.minecartmagic;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public final class MinecartTractionHandler {

    private static final double VANILLA_SPEED = 0.4D;

    private MinecartTractionHandler() {
    }

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(
                MinecartTractionHandler::onServerTick
        );
    }

    private static void onServerTick(MinecraftServer server) {

        for (ServerWorld world : server.getWorlds()) {

            for (AbstractMinecartEntity minecart :
                    world.getEntitiesByClass(
                            AbstractMinecartEntity.class,
                            minecartBox(minecartWorld(world)),
                            entity -> true
                    )) {

                updateMinecart(minecart);
            }
        }
    }

    private static ServerWorld minecartWorld(ServerWorld world) {
        return world;
    }

    private static net.minecraft.util.math.Box minecartBox(ServerWorld world) {
        return new net.minecraft.util.math.Box(
                world.getWorldBorder().getBoundWest(),
                world.getBottomY(),
                world.getWorldBorder().getBoundNorth(),
                world.getWorldBorder().getBoundEast(),
                world.getTopY(),
                world.getWorldBorder().getBoundSouth()
        );
    }

    private static void updateMinecart(AbstractMinecartEntity minecart) {

        if (!minecart.isOnRail()) {
            return;
        }

        ItemStack stack = minecart.getPickBlockStack();

        if (stack == null || stack.isEmpty()) {
            return;
        }

        int level = ModEnchantments.getTractionLevel(stack);

        if (level <= 0) {
            return;
        }

        double multiplier = switch (level) {
            case 1 -> 1.30D;
            case 2 -> 1.60D;
            case 3 -> 1.90D;
            default -> 1.90D;
        };

        Vec3d velocity = minecart.getVelocity();

        double horizontalSpeed = Math.sqrt(
                velocity.x * velocity.x +
                velocity.z * velocity.z
        );

        if (horizontalSpeed <= 0.00001D) {
            return;
        }

        double targetSpeed = VANILLA_SPEED * multiplier;

        if (horizontalSpeed >= targetSpeed) {
            return;
        }

        double scale = targetSpeed / horizontalSpeed;

        // Не меняем вертикальную скорость.
        minecart.setVelocity(
                velocity.x * scale,
                velocity.y,
                velocity.z * scale
        );
    }
}
