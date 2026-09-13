package com.minecartmagic;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.BoatItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public final class LegacyEnchantments {

    private LegacyEnchantments() {
    }

    public static void init() {
        register(
                ModEnchantments.TRACTION_KEY,
                createEnchantment(false)
        );

        register(
                ModEnchantments.TAILWIND_KEY,
                createEnchantment(true)
        );
    }

    private static void register(
            RegistryKey<Enchantment> key,
            Enchantment enchantment
    ) {
        if (!Registries.ENCHANTMENT.containsId(key.getValue())) {
            Registry.register(
                    Registries.ENCHANTMENT,
                    key.getValue(),
                    enchantment
            );
        }
    }

    private static Enchantment createEnchantment(boolean tailwind) {
        return new Enchantment(
                Enchantment.Rarity.COMMON,
                EnchantmentTarget.VANISHABLE,
                new EquipmentSlot[]{EquipmentSlot.MAINHAND}
        ) {
            @Override
            public int getMinPower(int level) {
                return 10 + (level - 1) * 10;
            }

            @Override
            public int getMaxPower(int level) {
                return 30 + (level - 1) * 15;
            }

            @Override
            public int getMaxLevel() {
                return 3;
            }

            @Override
            public boolean isAcceptableItem(ItemStack stack) {
                Item item = stack.getItem();

                if (!tailwind) {
                    return item == Items.MINECART;
                }

                return item instanceof BoatItem
                        || item == Items.OAK_CHEST_BOAT
                        || item == Items.SPRUCE_CHEST_BOAT
                        || item == Items.BIRCH_CHEST_BOAT
                        || item == Items.JUNGLE_CHEST_BOAT
                        || item == Items.ACACIA_CHEST_BOAT
                        || item == Items.DARK_OAK_CHEST_BOAT
                        || item == Items.MANGROVE_CHEST_BOAT
                        || item == Items.CHERRY_CHEST_BOAT
                        || item == Items.BAMBOO_CHEST_RAFT
                        || item == ModItems.SELF_PROPELLING_BOAT;
            }

            @Override
            public boolean isAvailableForRandomSelection() {
                return true;
            }

            @Override
            public boolean isAvailableForEnchantedBookOffer() {
                return true;
            }
        };
    }
}
