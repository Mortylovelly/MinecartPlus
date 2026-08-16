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
public class MinecartVisualMixin {
    @Inject(method = "isGlowing", at = @At("RETURN"), cancellable = true)
    private void makeEnchantedCartGlow(CallbackInfoReturnable<Boolean> cir) {
        Minecart cart = (Minecart) (Object) this;
        ItemStack cartItem = cart.getPickResult();
        
        if (cartItem.getItem() == Items.MINECART) {
            if (ModEnchantments.getTractionLevel(cartItem) > 0) {
                cir.setReturnValue(true);
            }
        }
    }
}
