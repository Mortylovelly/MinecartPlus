package com.minecartmagic;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class MinecartTractionHandler {

    private static final String TRACTION_KEY = "MinecartMagicTraction";

    /*
     * Обычная максимальная скорость вагонетки примерно 0.4 блока/тик.
     *
     * Тяга I   = x1.30
     * Тяга II  = x1.60
     * Тяга III = x1.90
     */
    private static final double BASE_SPEED = 0.40D;

    private MinecartTractionHandler() {
    }

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(
                MinecartTractionHandler::onServerTick
        );
    }

    public static void setTractionLevel(
            AbstractMinecartEntity minecart,
            int level
    ) {
        if (level <= 0) {
            clearTractionLevel(minecart);
            return;
        }

        NbtCompound nbt = new NbtCompound();
        nbt.putInt(TRACTION_KEY, Math.min(level, 3));

        minecart.setComponent(
                DataComponentTypes.CUSTOM_DATA,
                NbtComponent.of(nbt)
        );
    }

    public static int getTractionLevel(
            AbstractMinecartEntity minecart
    ) {
        NbtComponent component =
                minecart.get(DataComponentTypes.CUSTOM_DATA);

        if (component == null) {
            return 0;
        }

        NbtCompound nbt = component.copyNbt();

        if (!nbt.contains(TRACTION_KEY)) {
            return 0;
        }

        return Math.max(
                0,
                Math.min(
                        3,
                        nbt.getInt(TRACTION_KEY).orElse(0)
                )
        );
    }

    private static void clearTractionLevel(
            AbstractMinecartEntity minecart
    ) {
        minecart.setComponent(
                DataComponentTypes.CUSTOM_DATA,
                NbtComponent.of(new NbtCompound())
        );

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

    private static void updateMinecart(
            AbstractMinecartEntity minecart
    ) {
        int level = getTractionLevel(minecart);

        if (level <= 0) {
            return;
        }

        /*
         * Тяга работает именно на рельсах.
         */
        if (!minecart.isOnRail()) {
            return;
        }

        double multiplier = switch (level) {
            case 1 -> 1.30D;
            case 2 -> 1.60D;
            default -> 1.90D;
        };

        double maxSpeed = BASE_SPEED * multiplier;

        Vec3d velocity = minecart.getVelocity();

        double horizontalSpeed = Math.sqrt(
                velocity.x * velocity.x
                        + velocity.z * velocity.z
        );

        if (horizontalSpeed <= 0.00001D) {
            return;
        }

        /*
         * Не разгоняем вагонетку мгновенно до максимума.
         * Добавляем небольшой процент текущей скорости,
         * чтобы движение оставалось нормальным.
         */
        double targetSpeed = Math.min(
                maxSpeed,
                horizontalSpeed * 1.08D
        );

        if (targetSpeed <= horizontalSpeed) {
            return;
        }

        double scale = targetSpeed / horizontalSpeed;

        minecart.setVelocity(
                velocity.x * scale,
                velocity.y,
                velocity.z * scale
        );
    }
}
