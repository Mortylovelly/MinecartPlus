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

    public static final RegistryEntry<Enchantment> TRACTION_ENTRY =
        RegistryEntry.of(TRACTION_KEY);

    public static void init() {
    }

    public static int getTractionLevel(ItemStack stack) {
        return EnchantmentHelper.getLevel(TRACTION_ENTRY, stack);
    }
}
