package com.minecartmagic;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class ModEnchantments {
    public static final Enchantment TRACTION = new Enchantment(
        new Enchantment.Definition(
            RegistryEntryList.of(
                RegistryEntry.of(Items.MINECART)
            ),
            1,
            3,
            new Enchantment.Cost(10, 0),
            new Enchantment.Cost(20, 0),
            10,
            new EquipmentSlot[]{EquipmentSlot.MAINHAND}
        )
    );

    public static final RegistryEntry<Enchantment> TRACTION_ENTRY = Registry.registerReference(
        RegistryKeys.ENCHANTMENT,
        Identifier.of("minecartmagic", "traction"),
        TRACTION
    );

    public static void init() {
    }

    public static int getTractionLevel(ItemStack stack) {
        return EnchantmentHelper.getLevel(TRACTION_ENTRY, stack);
    }
}
