package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecartEntity.class)
public class MinecartSpeedMixin {

    @Inject(method = "getMaxSpeed", at = @At("RETURN"), cancellable = true)
    private void minecartmagic$modifyMaxSpeed(ServerWorld world, CallbackInfoReturnable<Double> cir) {
        AbstractMinecartEntity minecart = (AbstractMinecartEntity) (Object) this;

        int level = ModEnchantments.getTractionLevel(minecart.getPickBlockStack());

        if (level > 0) {
            double multiplier = 1.0 + (0.3 * level);
            cir.setReturnValue(cir.getReturnValue() * multiplier);
        }
    }
}
