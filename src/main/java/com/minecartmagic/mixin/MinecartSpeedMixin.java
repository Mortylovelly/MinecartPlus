package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecartEntity.class)
public class MinecartSpeedMixin {
    @Inject(method = "getMaxSpeed", at = @At("RETURN"), cancellable = true)
    private void modifyMaxSpeed(CallbackInfoReturnable<Double> cir) {
        MinecartEntity cart = (MinecartEntity) (Object) this;
        ItemStack cartItem = cart.getPickBlockStack();
        
        if (cartItem.getItem() == Items.MINECART) {
            int level = cartItem.getEnchantments().getLevel(ModEnchantments.TRACTION_ENTRY);
            if (level > 0) {
                double baseSpeed = cir.getReturnValue();
                double multiplier = 1.0 + (level * 0.3); // +30%, +60%, +90%
                cir.setReturnValue(baseSpeed * multiplier);
            }
        }
    }
}
