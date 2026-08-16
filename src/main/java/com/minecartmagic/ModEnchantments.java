package com.minecartmagic;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModEnchantments {
    public static final Enchantment TRACTION = Registry.register(
        Registries.ENCHANTMENT,
        Identifier.of("minecartmagic", "traction"),
        new TractionEnchantment()
    );

    public static void init() {
    }

    public static class TractionEnchantment extends Enchantment {
        public TractionEnchantment() {
            super(Enchantment.properties(
                TagKey.of(RegistryKeys.ENCHANTMENT, Identifier.of("minecartmagic", "traction_tag")),
                1,
                3,
                Enchantment.constantCost(10),
                Enchantment.constantCost(20),
                10
            ));
        }

        @Override
        public boolean isAcceptableItem(net.minecraft.item.ItemStack stack) {
            return stack.getItem() == net.minecraft.item.Items.MINECART;
        }

        @Override
        public boolean isTreasure() {
            return true;
        }
    }
}
