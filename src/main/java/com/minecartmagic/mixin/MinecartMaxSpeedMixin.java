package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
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

        /*
         * Mixin находится на AbstractMinecartEntity,
         * поэтому проверяем, что это именно обычная
         * rideable MinecartEntity.
         */
        if (!((Object) this instanceof MinecartEntity minecart)) {
            return;
        }

        int level =
                ModEnchantments.getTractionLevel(minecart);

        /*
         * Без зачарования абсолютно ничего
         * не меняем в ванильной вагонетке.
         */
        if (level <= 0) {
            return;
        }

        double vanillaSpeed =
                cir.getReturnValue();

        /*
         * Тяга I   = 1.5x
         * Тяга II  = 2.0x
         * Тяга III = 2.5x
         */
        double multiplier = switch (level) {
            case 1 -> 1.50D;
            case 2 -> 2.00D;
            default -> 2.50D;
        };

        cir.setReturnValue(
                vanillaSpeed * multiplier
        );
    }
}
