package com.minecartmagic;

import com.minecartmagic.item.SelfPropellingBoatItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.enchantment.Enchantment;

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
         * Готовые книги зачарования.
         *
         * Enchantment registry уже заполнен к моменту
         * формирования Creative Item Group, поэтому здесь
         * безопасно получать RegistryEntry по RegistryKey.
         */
        ItemGroupEvents.modifyEntriesEvent(
                ItemGroups.INGREDIENTS
        ).register(entries -> {

            RegistryEntry<Enchantment> traction =
                    Registries.ENCHANTMENT
                            .getEntry(
                                    ModEnchantments.TRACTION_KEY
                            )
                            .orElse(null);

            RegistryEntry<Enchantment> tailwind =
                    Registries.ENCHANTMENT
                            .getEntry(
                                    ModEnchantments.TAILWIND_KEY
                            )
                            .orElse(null);

            if (traction != null) {
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
            }

            if (tailwind != null) {
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
            }
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
