package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecart.class)
public class MinecartSpeedMixin {
    @Inject(method = "getMaxSpeed", at = @At("RETURN"), cancellable = true)
    private void modifyMaxSpeed(CallbackInfoReturnable<Double> cir) {
        Minecart cart = (Minecart) (Object) this;
        ItemStack cartItem = cart.getPickResult();
        
        if (cartItem.getItem() == Items.MINECART) {
            int level = ModEnchantments.getTractionLevel(cartItem);
            if (level > 0) {
                double baseSpeed = cir.getReturnValue();
                double multiplier = 1.0 + (level * 0.3);
                cir.setReturnValue(baseSpeed * multiplier);
            }
        }
    }
}
