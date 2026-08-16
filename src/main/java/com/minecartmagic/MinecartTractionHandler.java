package com.minecartmagic;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class MinecartTractionHandler {

    private static final String TRACTION_TAG_PREFIX = "minecartmagic:traction_";

    private MinecartTractionHandler() {
    }

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(
                MinecartTractionHandler::onServerTick
        );
    }

    /**
     * Сохраняет уровень Тяги непосредственно на вагонетке.
     *
     * Используем command tag, потому что он:
     * - не требует Mixin;
     * - сохраняется вместе с Entity;
     * - доступен на Minecraft 1.21.1;
     * - не зависит от DataComponent API.
     */
    public static void setTractionLevel(
            AbstractMinecartEntity minecart,
            int level
    ) {
        clearTractionLevel(minecart);

        if (level <= 0) {
            return;
        }

        level = Math.min(level, 3);

        minecart.addCommandTag(
                TRACTION_TAG_PREFIX + level
        );

        // Визуальный индикатор зачарованной вагонетки.
        minecart.setGlowing(true);
    }

    /**
     * Получает уровень Тяги с вагонетки.
     */
    public static int getTractionLevel(
            AbstractMinecartEntity minecart
    ) {
        for (String tag : minecart.getCommandTags()) {
            if (tag.equals(TRACTION_TAG_PREFIX + "1")) {
                return 1;
            }

            if (tag.equals(TRACTION_TAG_PREFIX + "2")) {
                return 2;
            }

            if (tag.equals(TRACTION_TAG_PREFIX + "3")) {
                return 3;
            }
        }

        return 0;
    }

    /**
     * Удаляет уровень Тяги.
     */
    private static void clearTractionLevel(
            AbstractMinecartEntity minecart
    ) {
        minecart.removeCommandTag(TRACTION_TAG_PREFIX + "1");
        minecart.removeCommandTag(TRACTION_TAG_PREFIX + "2");
        minecart.removeCommandTag(TRACTION_TAG_PREFIX + "3");

        minecart.setGlowing(false);
    }

    private static void onServerTick(MinecraftServer server) {

        for (ServerWorld world : server.getWorlds()) {

            Box box = new Box(
                    world.getWorldBorder().getBoundWest(),
                    world.getBottomY(),
                    world.getWorldBorder().getBoundNorth(),
                    world.getWorldBorder().getBoundEast(),
                    world.getTopY(),
                    world.getWorldBorder().getBoundSouth()
            );

            for (AbstractMinecartEntity minecart :
                    world.getEntitiesByClass(
                            AbstractMinecartEntity.class,
                            box,
                            entity -> true
                    )) {

                updateMinecart(minecart);
            }
        }
    }

    /**
     * Применяет ускорение.
     *
     * Тяга:
     *
     * I   = +30%
     * II  = +60%
     * III = +90%
     */
    private static void updateMinecart(
            AbstractMinecartEntity minecart
    ) {
        int level = getTractionLevel(minecart);

        if (level <= 0) {
            return;
        }

        // Работает только во время движения по рельсам.
        if (!minecart.isOnRail()) {
            return;
        }

        double multiplier = switch (level) {
            case 1 -> 1.30D;
            case 2 -> 1.60D;
            case 3 -> 1.90D;
            default -> 1.0D;
        };

        Vec3d velocity = minecart.getVelocity();

        double horizontalSpeed = Math.sqrt(
                velocity.x * velocity.x +
                velocity.z * velocity.z
        );

        if (horizontalSpeed <= 0.00001D) {
            return;
        }

        /*
         * Ванильная вагонетка примерно 0.4 блока/тик.
         *
         * Благодаря тому, что мы увеличиваем уже существующую
         * скорость после ванильной обработки рельса, нам не
         * требуется вмешиваться в protected getMaxSpeed().
         */
        double vanillaSpeed = 0.40D;
        double targetSpeed = vanillaSpeed * multiplier;

        /*
         * Плавное приближение к максимальной скорости.
         */
        double newSpeed = Math.min(
                targetSpeed,
                horizontalSpeed + 0.025D * multiplier
        );

        if (newSpeed <= horizontalSpeed) {
            return;
        }

        double scale = newSpeed / horizontalSpeed;

        minecart.setVelocity(
                velocity.x * scale,
                velocity.y,
                velocity.z * scale
        );
    }
}
