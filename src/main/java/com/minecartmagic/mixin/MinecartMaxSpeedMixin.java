package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecartEntity.class)
public abstract class MinecartMaxSpeedMixin {

    @Inject(
            method = "getMaxSpeed()D",
            at = @At("RETURN"),
            cancellable = true
    )
    private void minecartmagic$increaseMaxSpeed(
            CallbackInfoReturnable<Double> cir
    ) {
        AbstractMinecartEntity minecart =
                (AbstractMinecartEntity) (Object) this;

        int level =
                ModEnchantments.getTractionLevel(minecart);

        if (level <= 0) {
            return;
        }

        double vanillaSpeed =
                cir.getReturnValue();

        double multiplier = switch (level) {
            case 1 -> 1.60D;
            case 2 -> 2.20D;
            default -> 2.80D;
        };

        cir.setReturnValue(
                vanillaSpeed * multiplier
        );
    }
}
