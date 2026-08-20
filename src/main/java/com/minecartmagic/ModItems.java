package com.minecartmagic;

import com.minecartmagic.item.SelfPropellingBoatItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
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
                                    .maxCount(1),
                            BoatEntity.Type.OAK
                    )
            );

    public static final Item SELF_PROPELLING_SPRUCE_BOAT =
            registerBoat(
                    "self_propelling_spruce_boat",
                    BoatEntity.Type.SPRUCE
            );

    public static final Item SELF_PROPELLING_BIRCH_BOAT =
            registerBoat(
                    "self_propelling_birch_boat",
                    BoatEntity.Type.BIRCH
            );

    public static final Item SELF_PROPELLING_JUNGLE_BOAT =
            registerBoat(
                    "self_propelling_jungle_boat",
                    BoatEntity.Type.JUNGLE
            );

    public static final Item SELF_PROPELLING_ACACIA_BOAT =
            registerBoat(
                    "self_propelling_acacia_boat",
                    BoatEntity.Type.ACACIA
            );

    public static final Item SELF_PROPELLING_DARK_OAK_BOAT =
            registerBoat(
                    "self_propelling_dark_oak_boat",
                    BoatEntity.Type.DARK_OAK
            );

    public static final Item SELF_PROPELLING_MANGROVE_BOAT =
            registerBoat(
                    "self_propelling_mangrove_boat",
                    BoatEntity.Type.MANGROVE
            );

    public static final Item SELF_PROPELLING_CHERRY_BOAT =
            registerBoat(
                    "self_propelling_cherry_boat",
                    BoatEntity.Type.CHERRY
            );

    public static final Item SELF_PROPELLING_BAMBOO_RAFT =
            registerBoat(
                    "self_propelling_bamboo_raft",
                    BoatEntity.Type.BAMBOO
            );

    private ModItems() {
    }

    private static Item registerBoat(
            String id,
            BoatEntity.Type variant
    ) {
        return Registry.register(
                Registries.ITEM,
                MinecartMagicMod.id(id),
                new SelfPropellingBoatItem(
                        new Item.Settings()
                                .maxCount(1),
                        variant
                )
        );
    }

    public static Item getSelfPropellingBoatItem(
            BoatEntity.Type type
    ) {
        return switch (type) {
            case SPRUCE -> SELF_PROPELLING_SPRUCE_BOAT;
            case BIRCH -> SELF_PROPELLING_BIRCH_BOAT;
            case JUNGLE -> SELF_PROPELLING_JUNGLE_BOAT;
            case ACACIA -> SELF_PROPELLING_ACACIA_BOAT;
            case DARK_OAK -> SELF_PROPELLING_DARK_OAK_BOAT;
            case MANGROVE -> SELF_PROPELLING_MANGROVE_BOAT;
            case CHERRY -> SELF_PROPELLING_CHERRY_BOAT;
            case BAMBOO -> SELF_PROPELLING_BAMBOO_RAFT;
            case OAK -> SELF_PROPELLING_BOAT;
        };
    }

    public static void init() {

        ItemGroupEvents.modifyEntriesEvent(
                ItemGroups.TOOLS
        ).register(entries -> {
            entries.add(SELF_PROPELLING_BOAT);
            entries.add(SELF_PROPELLING_SPRUCE_BOAT);
            entries.add(SELF_PROPELLING_BIRCH_BOAT);
            entries.add(SELF_PROPELLING_JUNGLE_BOAT);
            entries.add(SELF_PROPELLING_ACACIA_BOAT);
            entries.add(SELF_PROPELLING_DARK_OAK_BOAT);
            entries.add(SELF_PROPELLING_MANGROVE_BOAT);
            entries.add(SELF_PROPELLING_CHERRY_BOAT);
            entries.add(SELF_PROPELLING_BAMBOO_RAFT);
        });

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
