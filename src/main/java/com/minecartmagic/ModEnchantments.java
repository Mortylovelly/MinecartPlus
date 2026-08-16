package com.minecartmagic;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.tags.ItemTags;

public class ModEnchantments {
    public static final Enchantment TRACTION = Registry.register(
        BuiltInRegistries.ENCHANTMENT,
        ResourceLocation.fromNamespaceAndPath("minecartmagic", "traction"),
        new Enchantment(
            new Enchantment.Definition(
                BuiltInRegistries.ITEM.getOrCreateTag(ItemTags.MINECART),
                1,
                3,
                new Enchantment.Cost(10, 0),
                new Enchantment.Cost(20, 0),
                10,
                new EquipmentSlot[]{EquipmentSlot.MAINHAND}
            )
        )
    );

    public static void init() {
    }

    public static boolean isMinecart(ItemStack stack) {
        return stack.getItem() == Items.MINECART;
    }

    public static int getTractionLevel(ItemStack stack) {
        return EnchantmentHelper.getItemEnchantmentLevel(TRACTION, stack);
    }
}
