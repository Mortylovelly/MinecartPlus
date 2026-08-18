package com.minecartmagic;

import com.minecartmagic.item.SelfPropellingBoatItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.RegistryWrapper;

public final class ModItems {

    public static final Item SELF_PROPELLING_BOAT =
            Registry.register(
                    Registries.ITEM,
                    MinecartMagicMod.id(
                            "self_propelling_boat"
                    ),
                    new SelfPropellingBoatItem(
                            new Item.Settings()
                                    .maxCount(1)
                    )
            );

    private ModItems() {
    }

    public static void init() {

        /*
         * Самоходная лодка.
         */
        ItemGroupEvents.modifyEntriesEvent(
                ItemGroups.TOOLS
        ).register(entries -> {
            entries.add(
                    SELF_PROPELLING_BOAT
            );
        });

        /*
         * Готовые книги:
         *
         * Тяга I
         * Тяга II
         * Тяга III
         *
         * Попутный ветер I
         * Попутный ветер II
         * Попутный ветер III
         */
        ItemGroupEvents.modifyEntriesEvent(
                ItemGroups.INGREDIENTS
        ).register(entries -> {

            RegistryWrapper.WrapperLookup lookup =
                    entries.getContext().lookup();

            RegistryWrapper.Impl<Enchantment> enchantmentLookup =
                    lookup.getWrapperOrThrow(
                            RegistryKeys.ENCHANTMENT
                    );

            RegistryEntry<Enchantment> traction =
                    enchantmentLookup.getOrThrow(
                            ModEnchantments.TRACTION_KEY
                    );

            RegistryEntry<Enchantment> tailwind =
                    enchantmentLookup.getOrThrow(
                            ModEnchantments.TAILWIND_KEY
                    );

            entries.add(
                    createEnchantedBook(
                            traction,
                            1
                    )
            );

            entries.add(
                    createEnchantedBook(
                            traction,
                            2
                    )
            );

            entries.add(
                    createEnchantedBook(
                            traction,
                            3
                    )
            );

            entries.add(
                    createEnchantedBook(
                            tailwind,
                            1
                    )
            );

            entries.add(
                    createEnchantedBook(
                            tailwind,
                            2
                    )
            );

            entries.add(
                    createEnchantedBook(
                            tailwind,
                            3
                    )
            );
        });
    }

    private static ItemStack createEnchantedBook(
            RegistryEntry<Enchantment> enchantment,
            int level
    ) {
        ItemStack stack =
                new ItemStack(
                        Items.ENCHANTED_BOOK
                );

        stack.addEnchantment(
                enchantment,
                level
        );

        return stack;
    }
}
