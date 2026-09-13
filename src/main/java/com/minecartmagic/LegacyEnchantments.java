package com.minecartmagic;

import com.minecartmagic.item.AdvancedMinecartItem;
import com.minecartmagic.item.SelfPropellingBoatItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.BoatItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MinecartItem;
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
                Enchantment.Rarity.RARE,
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

                if (tailwind) {
                    return item instanceof BoatItem
                            || item instanceof SelfPropellingBoatItem;
                }

                return item instanceof MinecartItem
                        || item instanceof AdvancedMinecartItem;
            }
        };
    }
}
