package com.minecartmagic.mixin;

import com.minecartmagic.BoatTailwindData;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatEntity.class)
public abstract class BoatTailwindDataMixin {

    @Inject(
            method = "initDataTracker",
            at = @At("TAIL")
    )
    private void minecartmagic$initTailwindData(
            DataTracker.Builder builder,
            CallbackInfo ci
    ) {
        builder.add(
                BoatTailwindData.TAILWIND_LEVEL,
                0
        );
    }
}
