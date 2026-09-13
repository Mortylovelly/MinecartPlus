package com.minecartmagic.mixin;

import com.minecartmagic.ModEnchantments;
import com.minecartmagic.ModItems;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.BoatItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.EnchantmentScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EnchantmentScreenHandler.class)
public abstract class VehicleEnchantmentScreenMixin {

    @Inject(
            method = "generateEnchantments",
            at = @At("RETURN"),
            cancellable = true
    )
    private void minecartmagic$addVehicleEnchantments(
            ItemStack stack,
            int slot,
            int level,
            CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir
    ) {
        if (!minecartmagic$isTractionTarget(stack)
                && !minecartmagic$isTailwindTarget(stack)) {
            return;
        }

        List<EnchantmentLevelEntry> entries = cir.getReturnValue();

        if (minecartmagic$isTractionTarget(stack)) {
            minecartmagic$addIfPossible(
                    entries,
                    Registries.ENCHANTMENT.get(
                            ModEnchantments.TRACTION_KEY.getValue()
                    ),
                    level
            );
        }

        if (minecartmagic$isTailwindTarget(stack)) {
            minecartmagic$addIfPossible(
                    entries,
                    Registries.ENCHANTMENT.get(
                            ModEnchantments.TAILWIND_KEY.getValue()
                    ),
                    level
            );
        }

        cir.setReturnValue(entries);
    }

    private static void minecartmagic$addIfPossible(
            List<EnchantmentLevelEntry> entries,
            Enchantment enchantment,
            int power
    ) {
        if (enchantment == null) {
            return;
        }

        for (int level = enchantment.getMaxLevel(); level >= 1; level--) {
            if (power >= enchantment.getMinPower(level)
                    && power <= enchantment.getMaxPower(level)) {
                for (EnchantmentLevelEntry entry : entries) {
                    if (entry.enchantment == enchantment) {
                        return;
                    }
                }

                entries.add(
                        new EnchantmentLevelEntry(
                                enchantment,
                                level
                        )
                );
                return;
            }
        }
    }

    private static boolean minecartmagic$isTractionTarget(ItemStack stack) {
        return stack.isOf(Items.MINECART);
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
