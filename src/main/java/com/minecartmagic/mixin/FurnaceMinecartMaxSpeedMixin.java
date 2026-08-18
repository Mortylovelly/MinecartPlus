package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FurnaceMinecartEntity.class)
public abstract class FurnaceMinecartMaxSpeedMixin {

    @Inject(
            method = "getMaxSpeed",
            at = @At("RETURN"),
            cancellable = true
    )
    private void minecartmagic$increaseFurnaceMinecartSpeed(
            CallbackInfoReturnable<Double> cir
    ) {
        FurnaceMinecartEntity minecart =
                (FurnaceMinecartEntity) (Object) this;

        int level =
                ModEnchantments.getTractionLevel(minecart);

        if (level <= 0) {
            return;
        }

        double vanillaSpeed =
                cir.getReturnValue();

        double multiplier = switch (level) {
            case 1 -> 1.75D;
            case 2 -> 2.40D;
            default -> 3.10D;
        };

        cir.setReturnValue(
                vanillaSpeed * multiplier
        );
    }
}
