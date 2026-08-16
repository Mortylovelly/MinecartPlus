package com.minecartmagic;

public final class MinecartSpeedHandler {

    private MinecartSpeedHandler() {
    }

    public static void init() {
        /*
         * Скорость вагонетки теперь изменяется
         * непосредственно через MinecartMaxSpeedMixin.
         *
         * Здесь намеренно ничего не происходит.
         * Никакого setVelocity каждый тик,
         * чтобы не ломать ванильную физику вагонетки.
         */
    }
}
