package com.minecartmagic;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class ModEnchantments {

    public static final RegistryKey<Enchantment> TRACTION_KEY =
            RegistryKey.of(
                    RegistryKeys.ENCHANTMENT,
                    Identifier.of(
                            MinecartMagicMod.MOD_ID,
                            "traction"
                    )
            );

    private ModEnchantments() {
    }

    public static void init() {
        /*
         * Ничего регистрировать вручную не нужно.
         *
         * Minecraft 1.21.1 загружает enchantment
         * из data/minecartmagic/enchantment/traction.json.
         */
    }

    public static int getTractionLevel(ItemStack stack) {

        for (var entry :
                EnchantmentHelper
                        .getEnchantments(stack)
                        .getEnchantmentEntries()) {

            RegistryEntry<Enchantment> enchantment =
                    entry.getKey();

            if (enchantment.getKey().isPresent()
                    && enchantment.getKey()
                    .get()
                    .equals(TRACTION_KEY)) {

                return EnchantmentHelper.getLevel(
                        enchantment,
                        stack
                );
            }
        }

        return 0;
    }
}
