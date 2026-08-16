package com.minecartmagic;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.Identifier;

public final class ModEnchantments {

    public static final Identifier TRACTION_ID = Identifier.of(
        MinecartMagicMod.MOD_ID,
        "traction"
    );

    public static final RegistryKey<Enchantment> TRACTION_KEY = RegistryKey.of(
        RegistryKeys.ENCHANTMENT,
        TRACTION_ID
    );

    private ModEnchantments() {
    }

    public static void init() {
        MinecartMagicMod.LOGGER.info("Minecart Magic enchantments loaded!");
    }

    public static int getTractionLevel(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        for (var entry : EnchantmentHelper.getEnchantments(stack).getEnchantmentEntries()) {
            RegistryEntry<Enchantment> enchantment = entry.getKey();

            if (enchantment.matchesKey(TRACTION_KEY)) {
                return entry.getIntValue();
            }
        }

        return 0;
    }
}
