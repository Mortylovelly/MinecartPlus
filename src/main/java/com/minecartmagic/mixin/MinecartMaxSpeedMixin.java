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
            method = "getMaxSpeed",
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

        /*
         * Тяга I   = 1.75x
         * Тяга II  = 2.40x
         * Тяга III = 3.10x
         */
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
