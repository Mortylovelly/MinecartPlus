package com.minecartmagic;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;

public class MinecartTractionHandler {

    private static final double LEVEL_1_MULTIPLIER = 1.30;
    private static final double LEVEL_2_MULTIPLIER = 1.60;
    private static final double LEVEL_3_MULTIPLIER = 1.90;

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(MinecartTractionHandler::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) {

            for (AbstractMinecartEntity minecart : world.getEntitiesByClass(
                    AbstractMinecartEntity.class,
                    new Box(
                            world.getWorldBorder().getBoundWest(),
                            world.getBottomY(),
                            world.getWorldBorder().getBoundNorth(),
                            world.getWorldBorder().getBoundEast(),
                            world.getTopY(),
                            world.getWorldBorder().getBoundSouth()
                    ),
                    entity -> true
            )) {

                int level = ModEnchantments.getTractionLevel(
                        minecart.getPickBlockStack()
                );

                if (level <= 0) {
                    continue;
                }

                double multiplier;

                switch (level) {
                    case 1 -> multiplier = LEVEL_1_MULTIPLIER;
                    case 2 -> multiplier = LEVEL_2_MULTIPLIER;
                    default -> multiplier = LEVEL_3_MULTIPLIER;
                }

                double velocityX = minecart.getVelocity().x;
                double velocityY = minecart.getVelocity().y;
                double velocityZ = minecart.getVelocity().z;

                double horizontalSpeed =
                        Math.sqrt(
                                velocityX * velocityX +
                                velocityZ * velocityZ
                        );

                if (horizontalSpeed <= 0.0001) {
                    continue;
                }

                double boostedSpeed = horizontalSpeed * multiplier;

                double maxSpeed = 0.4;

                if (boostedSpeed > maxSpeed) {
                    boostedSpeed = maxSpeed;
                }

                double scale = boostedSpeed / horizontalSpeed;

                minecart.setVelocity(
                        velocityX * scale,
                        velocityY,
                        velocityZ * scale
                );
            }
        }
    }
}
