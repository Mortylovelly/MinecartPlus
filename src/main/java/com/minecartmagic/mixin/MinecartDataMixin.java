package com.minecartmagic.mixin;

import com.minecartmagic.MinecartTractionData;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecartEntity.class)
public abstract class MinecartDataMixin {

    @Inject(
            method = "initDataTracker",
            at = @At("TAIL")
    )
    private void minecartmagic$initTractionData(
            DataTracker.Builder builder,
            CallbackInfo ci
    ) {
        if (!builder.isUsed()) {
            builder.add(
                    MinecartTractionData.TRACTION_LEVEL,
                    0
            );
        }
    }
}
