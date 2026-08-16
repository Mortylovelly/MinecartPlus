package com.minecartmagic;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

public class ModEnchantments {
    public static final RegistryKey<Enchantment> TRACTION_KEY = RegistryKey.of(
        RegistryKeys.ENCHANTMENT,
        Identifier.of("minecartmagic", "traction")
    );

    public static final Enchantment TRACTION = new Enchantment(
        new Enchantment.Definition(
            RegistryEntryList.of(RegistryEntry.of(Items.MINECART)),
            Optional.empty(),
            1,
            3,
            new Enchantment.Cost(10, 0),
            new Enchantment.Cost(20, 0),
            10,
            List.of(AttributeModifierSlot.MAINHAND)
        )
    );

    public static final RegistryEntry<Enchantment> TRACTION_ENTRY;

    static {
        Registry.register(Registries.ENCHANTMENT, TRACTION_KEY, TRACTION);
        TRACTION_ENTRY = Registries.ENCHANTMENT.getEntry(TRACTION_KEY);
    }

    public static void init() {
    }

    public static int getTractionLevel(ItemStack stack) {
        return EnchantmentHelper.getLevel(TRACTION_ENTRY, stack);
    }
}
