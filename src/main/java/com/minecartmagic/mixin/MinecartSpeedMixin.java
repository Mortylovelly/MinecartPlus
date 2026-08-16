package com.minecartmagic.mixin;

import net.minecraft.entity.vehicle.MinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecartEntity.class)
public class MinecartSpeedMixin {
    @Inject(method = "getMaxSpeed", at = @At("RETURN"), cancellable = true)
    private void modifyMaxSpeed(CallbackInfoReturnable<Double> cir) {
        MinecartEntity cart = (MinecartEntity) (Object) this;
        int level = cart.getPersistentData().getInt("traction_level");
        if (level > 0) {
            double baseSpeed = cir.getReturnValue();
            double multiplier = 1.0 + (level * 0.3);
            cir.setReturnValue(baseSpeed * multiplier);
        }
    }
}
