package com.minecartmagic;

import com.mojang.serialization.MapCodec;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class ModEnchantments {
    public static final RegistryKey<Enchantment> TRACTION_KEY = RegistryKey.of(
        RegistryKeys.ENCHANTMENT,
        Identifier.of("minecartmagic", "traction")
    );

    public static final TagKey<Item> ENCHANTABLE_MINECART = TagKey.of(
        RegistryKeys.ITEM,
        Identifier.of("minecartmagic", "enchantable_minecart")
    );

    public static final Enchantment TRACTION = new Enchantment(
        new Enchantment.Definition(
            RegistryEntryList.of(RegistryEntry.of(Items.MINECART)),
            1,
            3,
            new Enchantment.Cost(10, 0),
            new Enchantment.Cost(20, 0),
            10,
            new EquipmentSlot[]{EquipmentSlot.MAINHAND}
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
        return net.minecraft.enchantment.EnchantmentHelper.getLevel(TRACTION_ENTRY, stack);
    }
}
