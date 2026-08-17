package com.minecartmagic;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class ModEnchantments {

    public static final RegistryKey<Enchantment> TRACTION_KEY =
            RegistryKey.of(
                    RegistryKeys.ENCHANTMENT,
                    Identifier.of(MinecartMagicMod.MOD_ID, "traction")
            );

    private static final String TRACTION_TAG_PREFIX =
            "minecartmagic_traction_";

    private ModEnchantments() {
    }

    public static void init() {
    }

    public static int getTractionLevel(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        for (var entry :
                EnchantmentHelper.getEnchantments(stack)
                        .getEnchantmentEntries()) {

            RegistryEntry<Enchantment> enchantment =
                    entry.getKey();

            if (enchantment.getKey().isPresent()
                    && enchantment.getKey().get().equals(TRACTION_KEY)) {

                return EnchantmentHelper.getLevel(
                        enchantment,
                        stack
                );
            }
        }

        return 0;
    }

    public static int getTractionLevel(
            AbstractMinecartEntity minecart
    ) {
        for (String tag : minecart.getCommandTags()) {

            if (!tag.startsWith(TRACTION_TAG_PREFIX)) {
                continue;
            }

            String value =
                    tag.substring(TRACTION_TAG_PREFIX.length());

            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }

        return 0;
    }

    public static void setTractionLevel(
            AbstractMinecartEntity minecart,
            int level
    ) {
        minecart.getCommandTags().removeIf(
                tag -> tag.startsWith(TRACTION_TAG_PREFIX)
        );

        if (level <= 0) {
            return;
        }

        minecart.addCommandTag(
                TRACTION_TAG_PREFIX + level
        );
    }

    public static String getTractionTag(int level) {
        return TRACTION_TAG_PREFIX + level;
    }
}
