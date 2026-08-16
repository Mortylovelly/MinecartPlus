package com.minecartmagic;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
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

    private ModEnchantments() {
    }

    public static void init() {
    }

    /*
     * Получение уровня Тяги с предмета-вагонетки.
     */
    public static int getTractionLevel(ItemStack stack) {

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

    /*
     * Получение уровня Тяги с установленной вагонетки.
     *
     * Уровень сохраняется непосредственно в persistent data
     * Entity и поэтому не зависит от ItemStack после установки.
     */
    public static int getTractionLevel(MinecartEntity minecart) {

        return minecart.getPersistentData()
                .getInt("minecartmagic_traction");
    }

    /*
     * Записываем уровень Тяги на вагонетку.
     */
    public static void setTractionLevel(
            MinecartEntity minecart,
            int level
    ) {

        if (level <= 0) {
            minecart.getPersistentData()
                    .remove("minecartmagic_traction");
            return;
        }

        minecart.getPersistentData()
                .putInt(
                        "minecartmagic_traction",
                        level
                );
    }
}
