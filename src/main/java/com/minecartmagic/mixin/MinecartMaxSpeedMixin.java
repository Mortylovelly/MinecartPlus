package com.minecartmagic.mixin;

import com.minecartmagic.MinecartPlacementHandler;
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
        if (!((Object) this instanceof MinecartEntity minecart)) {
            return;
        }

        int level = 0;

        for (int i = 3; i >= 1; i--) {
            if (minecart.getCommandTags().contains(
                    MinecartPlacementHandler.getTractionTag(i)
            )) {
                level = i;
                break;
            }
        }

        if (level <= 0) {
            return;
        }

        double vanillaSpeed = cir.getReturnValue();

        /*
         * Тяга I   = 1.60x
         * Тяга II  = 2.20x
         * Тяга III = 2.80x
         */
        double multiplier = switch (level) {
            case 1 -> 1.60D;
            case 2 -> 2.20D;
            default -> 2.80D;
        };

        cir.setReturnValue(vanillaSpeed * multiplier);
    }
}
