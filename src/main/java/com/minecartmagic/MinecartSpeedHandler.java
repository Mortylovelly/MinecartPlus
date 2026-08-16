package com.minecartmagic;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;

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
        /*
         * Пока оставляем обработчик пустым.
         *
         * Никакой setVelocity().
         * Никакого вмешательства в ванильную физику.
         *
         * Это возвращает обычным вагонеткам
         * полностью ванильное поведение.
         */
        ServerTickEvents.END_WORLD_TICK.register(
                MinecartSpeedHandler::onWorldTick
        );
    }

    private static void onWorldTick(ServerWorld world) {
        /*
         * НИЧЕГО НЕ ДЕЛАЕМ.
         *
         * Скорость будем изменять другим способом,
         * не ломая физику вагонетки.
         */
    }
}
