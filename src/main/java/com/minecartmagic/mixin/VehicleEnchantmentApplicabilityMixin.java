package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import com.minecartmagic.ModItems;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.BoatItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public abstract class VehicleEnchantmentApplicabilityMixin {

    @Inject(method = "canEnchant", at = @At("RETURN"), cancellable = true)
    private void minecartmagic$allowOriginalVehicleTargets(
            ItemStack stack,
            CallbackInfoReturnable<Boolean> cir
    ) {
        Identifier id = EnchantmentHelper.getEnchantmentId(
                (Enchantment) (Object) this
        );

        if (id == null) {
            return;
        }

        if (ModEnchantments.TRACTION_KEY.getValue().equals(id)) {
            if (stack.isOf(Items.MINECART)) {
                cir.setReturnValue(true);
            }
            return;
        }

        if (ModEnchantments.TAILWIND_KEY.getValue().equals(id)
                && minecartmagic$isTailwindTarget(stack)) {
            cir.setReturnValue(true);
        }
    }

    private static boolean minecartmagic$isTailwindTarget(ItemStack stack) {
        Item item = stack.getItem();

        return item instanceof BoatItem
                || item == Items.OAK_CHEST_BOAT
                || item == Items.SPRUCE_CHEST_BOAT
                || item == Items.BIRCH_CHEST_BOAT
                || item == Items.JUNGLE_CHEST_BOAT
                || item == Items.ACACIA_CHEST_BOAT
                || item == Items.DARK_OAK_CHEST_BOAT
                || item == Items.MANGROVE_CHEST_BOAT
                || item == Items.CHERRY_CHEST_BOAT
                || item == Items.BAMBOO_CHEST_RAFT
                || item == ModItems.SELF_PROPELLING_BOAT;
    }
}
