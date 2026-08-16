package com.minecartmagic;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.BuiltInRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public class ModEnchantments {
    public static final TagKey<Item> ENCHANTABLE_MINECART = TagKey.of(RegistryKeys.ITEM, Identifier.of("minecartmagic", "enchantable_minecart"));

    public static final Enchantment TRACTION = Registry.register(
        BuiltInRegistries.ENCHANTMENT,
        Identifier.of("minecartmagic", "traction"),
        new Enchantment(
            new Enchantment.Properties(
                ENCHANTABLE_MINECART, // supported items tag
                TagKey.of(RegistryKeys.ENCHANTMENT, Identifier.of("minecartmagic", "traction_tag")), // primary tag
                1, // min level
                3, // max level
                Enchantment.constantCost(10), // min cost
                Enchantment.constantCost(20), // max cost
                10 // anvil cost
            )
        )
    );

    public static final RegistryEntry<Enchantment> TRACTION_ENTRY = BuiltInRegistries.ENCHANTMENT.getEntry(TRACTION);

    public static void init() {
    }
}
