package com.minecartmagic;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

public class ModEnchantments {
    public static final Enchantment TRACTION = new Enchantment(
        Text.translatable("enchantment.minecartmagic.traction"),
        new Enchantment.Definition(
            RegistryEntryList.of(RegistryEntry.of(Items.MINECART)),
            Optional.empty(),
            1,
            3,
            new Enchantment.Cost(10, 0),
            new Enchantment.Cost(20, 0),
            10,
            List.of(AttributeModifierSlot.MAINHAND)
        ),
        RegistryEntryList.empty(),
        ComponentMap.EMPTY
    );

    public static final RegistryEntry<Enchantment> TRACTION_ENTRY;

    static {
        Registry.register(Registries.ENCHANTMENT, Identifier.of("minecartmagic", "traction"), TRACTION);
        TRACTION_ENTRY = Registries.ENCHANTMENT.getEntry(Identifier.of("minecartmagic", "traction"));
    }

    public static void init() {
    }

    public static int getTractionLevel(net.minecraft.item.ItemStack stack) {
        return EnchantmentHelper.getLevel(TRACTION_ENTRY, stack);
    }
}
