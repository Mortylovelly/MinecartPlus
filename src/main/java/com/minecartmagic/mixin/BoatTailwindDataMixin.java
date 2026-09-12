package com.minecartmagic.mixin;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatEntity.class)
public abstract class BoatTailwindDataMixin
        implements BoatTailwindDataAccess {

    @Unique
    private static final TrackedData<Integer> MINECART_MAGIC$TAILWIND_LEVEL =
            DataTracker.registerData(
                    BoatEntity.class,
                    TrackedDataHandlerRegistry.INTEGER
            );

    @Inject(
            method = "initDataTracker",
            at = @At("TAIL")
    )
    private void minecartmagic$initTailwindDataTracker(
            CallbackInfo ci
    ) {
        ((BoatEntity) (Object) this).getDataTracker().startTracking(
                MINECART_MAGIC$TAILWIND_LEVEL,
                0
        );
    }

    @Override
    public int minecartmagic$getTailwindDataLevel() {
        return ((BoatEntity) (Object) this).getDataTracker().get(
                MINECART_MAGIC$TAILWIND_LEVEL
        );
    }

    @Override
    public void minecartmagic$setTailwindDataLevel(
            int level
    ) {
        ((BoatEntity) (Object) this).getDataTracker().set(
                MINECART_MAGIC$TAILWIND_LEVEL,
                Math.max(0, level)
        );
    }
}
