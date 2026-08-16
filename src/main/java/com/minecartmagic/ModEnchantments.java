package com.minecartmagic;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class ModEnchantments {

    public static final RegistryKey<Enchantment> TRACTION_KEY = RegistryKey.of(
            RegistryKeys.ENCHANTMENT,
            Identifier.of(MinecartMagicMod.MOD_ID, "traction")
    );

    private ModEnchantments() {
    }

    public static void init() {
        MinecartMagicMod.LOGGER.info("Minecart Magic enchantments initialized");
    }

    public static int getTractionLevel(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        var enchantments = EnchantmentHelper.getEnchantments(stack);

        for (RegistryEntry<Enchantment> entry : enchantments.getEnchantments()) {
            if (entry.matchesKey(TRACTION_KEY)) {
                return enchantments.getLevel(entry);
            }
        }

        return 0;
    }
}
