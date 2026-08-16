package com.minecartmagic;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModEnchantments {
    public static final TagKey<Item> ENCHANTABLE_MINECART = TagKey.of(RegistryKeys.ITEM, Identifier.of("minecartmagic", "enchantable_minecart"));
    public static final TagKey<Enchantment> TRACTION_TAG = TagKey.of(RegistryKeys.ENCHANTMENT, Identifier.of("minecartmagic", "traction_tag"));

    public static final Enchantment TRACTION = Registry.register(
        Registries.ENCHANTMENT,
        Identifier.of("minecartmagic", "traction"),
        new Enchantment(
            new Enchantment.Definition(
                ENCHANTABLE_MINECART,
                TRACTION_TAG,
                1, // weight
                3, // max level
                Enchantment.constantCost(10),
                Enchantment.constantCost(20),
                10, // anvil cost
                new EquipmentSlot[]{EquipmentSlot.MAINHAND}
            )
        )
    );

    public static final RegistryEntry<Enchantment> TRACTION_ENTRY = Registries.ENCHANTMENT.getEntry(TRACTION).orElseThrow();

    public static void init() {}
}
