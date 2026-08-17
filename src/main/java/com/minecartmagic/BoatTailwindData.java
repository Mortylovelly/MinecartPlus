package com.minecartmagic;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.vehicle.BoatEntity;

public final class BoatTailwindData {

    public static final TrackedData<Integer> TAILWIND_LEVEL =
            DataTracker.registerData(
                    BoatEntity.class,
                    TrackedDataHandlerRegistry.INTEGER
            );

    private BoatTailwindData() {
    }

    public static int get(BoatEntity boat) {
        return boat.getDataTracker().get(TAILWIND_LEVEL);
    }

    public static void set(
            BoatEntity boat,
            int level
    ) {
        boat.getDataTracker().set(
                TAILWIND_LEVEL,
                Math.max(0, level)
        );
    }
}
