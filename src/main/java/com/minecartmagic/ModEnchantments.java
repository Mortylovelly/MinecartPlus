package com.minecartmagic;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class ModEnchantments {
    public static final RegistryKey<Enchantment> TRACTION_KEY = RegistryKey.of(
        RegistryKeys.ENCHANTMENT,
        Identifier.of("minecartmagic", "traction")
    );

    public static void init() {
    }

    public static int getTractionLevel(ItemStack stack) {
        for (var entry : EnchantmentHelper.getEnchantments(stack).getEnchantmentEntries()) {
            RegistryEntry<Enchantment> enchantment = entry.getKey();

            if (enchantment.getKey().isPresent()
                && enchantment.getKey().get().equals(TRACTION_KEY)) {
                return EnchantmentHelper.getLevel(enchantment, stack);
            }
        }

        return 0;
    }
}
