package com.minecartmagic;

import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item TRACTION_BOOK = new Item(new Item.Settings().maxCount(1));

    public static void init() {
        Registry.register(Registries.ITEM, Identifier.of("minecartmagic", "traction_book"), TRACTION_BOOK);
    }

    public static int getTractionLevel(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains("traction_level")) {
            return nbt.getInt("traction_level");
        }
        return 0;
    }
}
