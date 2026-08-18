package com.minecartmagic;

import com.minecartmagic.item.SelfPropellingBoatItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

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
        ItemGroupEvents.modifyEntriesEvent(
                ItemGroups.TOOLS
        ).register(entries ->
                entries.add(SELF_PROPELLING_BOAT)
        );
    }
}
