package com.minecartmagic;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;

public final class MinecartTractionData {

    public static final TrackedData<Integer> TRACTION_LEVEL =
            DataTracker.registerData(
                    AbstractMinecartEntity.class,
                    TrackedDataHandlerRegistry.INTEGER
            );

    private MinecartTractionData() {
    }

    public static int get(AbstractMinecartEntity minecart) {
        return minecart.getDataTracker().get(TRACTION_LEVEL);
    }

    public static void set(
            AbstractMinecartEntity minecart,
            int level
    ) {
        minecart.getDataTracker().set(
                TRACTION_LEVEL,
                Math.max(0, level)
        );
    }
}
