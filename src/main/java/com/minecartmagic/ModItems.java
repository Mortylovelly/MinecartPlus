package com.minecartmagic;

import com.minecartmagic.item.SelfPropellingBoatItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;

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
         * Готовые книги Тяги и Попутного ветра.
         *
         * Enchantment в Minecraft 1.21.1 находится
         * в динамическом registry, поэтому получаем
         * RegistryWrapper через DisplayContext.
         */
        ItemGroupEvents.modifyEntriesEvent(
                ItemGroups.INGREDIENTS
        ).register(entries -> {

            RegistryEntry<Enchantment> traction =
                    entries.getContext()
                            .lookup()
                            .getOrThrow(
                                    RegistryKeys.ENCHANTMENT
                            )
                            .getOrThrow(
                                    ModEnchantments.TRACTION_KEY
                            );

            RegistryEntry<Enchantment> tailwind =
                    entries.getContext()
                            .lookup()
                            .getOrThrow(
                                    RegistryKeys.ENCHANTMENT
                            )
                            .getOrThrow(
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

        EnchantmentHelper.set(
                stack,
                net.minecraft.component.type.ItemEnchantmentsComponent.builder()
                        .add(
                                enchantment,
                                level
                        )
                        .build()
        );

        return stack;
    }
}
