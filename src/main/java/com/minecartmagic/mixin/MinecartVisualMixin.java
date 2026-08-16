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
public class MinecartVisualMixin {
    @Inject(method = "isGlowing", at = @At("RETURN"), cancellable = true)
    private void makeEnchantedCartGlow(CallbackInfoReturnable<Boolean> cir) {
        MinecartEntity cart = (MinecartEntity) (Object) this;
        ItemStack cartItem = cart.getPickBlockStack();
        
        if (cartItem.getItem() == Items.MINECART) {
            if (cartItem.hasEnchantments()) {
                cir.setReturnValue(true);
            }
        }
    }
}
