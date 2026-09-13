package com.minecartmagic.mixin;

import com.minecartmagic.ModItems;
import net.minecraft.item.BoatItem;
import net.minecraft.item.ChestBoatItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class EnchantableVehicleItemMixin {

    @Inject(method = "isEnchantable", at = @At("RETURN"), cancellable = true)
    private void minecartmagic$makeOriginalTargetEnchantable(
            ItemStack stack,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (minecartmagic$isOriginalEnchantableTarget(stack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getEnchantability", at = @At("RETURN"), cancellable = true)
    private void minecartmagic$giveOriginalEnchantability(
            CallbackInfoReturnable<Integer> cir
    ) {
        Item item = (Item) (Object) this;

        if (item == Items.MINECART
                || item instanceof BoatItem
                || item instanceof ChestBoatItem
                || item == ModItems.SELF_PROPELLING_BOAT) {
            cir.setReturnValue(15);
        }
    }

    private static boolean minecartmagic$isOriginalEnchantableTarget(ItemStack stack) {
        Item item = stack.getItem();

        return item == Items.MINECART
                || item instanceof BoatItem
                || item instanceof ChestBoatItem
                || item == ModItems.SELF_PROPELLING_BOAT;
    }
}
