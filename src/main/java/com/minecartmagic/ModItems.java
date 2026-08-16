package com.minecartmagic;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item TRACTION_BOOK = new Item(new Item.Settings().maxCount(1));

    public static void init() {
        Registry.register(Registries.ITEM, Identifier.of("minecartmagic", "traction_book"), TRACTION_BOOK);
    }

    public static ItemStack createTractionBook(int level) {
        ItemStack stack = new ItemStack(TRACTION_BOOK);
        stack.addEnchantment(ModEnchantments.TRACTION, level);
        return stack;
    }
}
