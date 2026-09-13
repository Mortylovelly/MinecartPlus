package com.minecartmagic.mixin;

import com.minecartmagic.item.AdvancedMinecartItem;
import com.minecartmagic.item.SelfPropellingBoatItem;
import net.minecraft.item.BoatItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MinecartItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class EnchantableVehicleItemMixin {

    @Inject(method = "isEnchantable", at = @At("RETURN"), cancellable = true)
    private void minecartmagic$makeVehicleEnchantable(
            ItemStack stack,
            CallbackInfoReturnable<Boolean> cir
    ) {
        Item item = (Item) (Object) this;

        if (minecartmagic$isEnchantableVehicle(item)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getEnchantability", at = @At("RETURN"), cancellable = true)
    private void minecartmagic$giveVehicleEnchantability(
            CallbackInfoReturnable<Integer> cir
    ) {
        Item item = (Item) (Object) this;

        if (minecartmagic$isEnchantableVehicle(item)) {
            cir.setReturnValue(15);
        }
    }

    private static boolean minecartmagic$isEnchantableVehicle(Item item) {
        return item instanceof BoatItem
                || item instanceof MinecartItem
                || item instanceof SelfPropellingBoatItem
                || item instanceof AdvancedMinecartItem;
    }
}
