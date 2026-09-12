package com.minecartmagic.mixin;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecartEntity.class)
public abstract class MinecartTractionDataMixin
        implements MinecartTractionDataAccess {

    @Unique
    private static final TrackedData<Integer> MINECART_MAGIC$TRACTION_LEVEL =
            DataTracker.registerData(
                    AbstractMinecartEntity.class,
                    TrackedDataHandlerRegistry.INTEGER
            );

    @Inject(
            method = "initDataTracker",
            at = @At("TAIL")
    )
    private void minecartmagic$initTractionDataTracker(
            CallbackInfo ci
    ) {
        ((AbstractMinecartEntity) (Object) this).getDataTracker().startTracking(
                MINECART_MAGIC$TRACTION_LEVEL,
                0
        );
    }

    @Override
    public int minecartmagic$getTractionDataLevel() {
        return ((AbstractMinecartEntity) (Object) this).getDataTracker().get(
                MINECART_MAGIC$TRACTION_LEVEL
        );
    }

    @Override
    public void minecartmagic$setTractionDataLevel(
            int level
    ) {
        ((AbstractMinecartEntity) (Object) this).getDataTracker().set(
                MINECART_MAGIC$TRACTION_LEVEL,
                Math.max(0, level)
        );
    }
}
