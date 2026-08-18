package com.minecartmagic;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModEntities {

    public static final EntityType<SelfPropellingBoatEntity>
            SELF_PROPELLING_BOAT =
            Registry.register(
                    Registries.ENTITY_TYPE,
                    MinecartMagicMod.id(
                            "self_propelling_boat"
                    ),
                    EntityType.Builder
                            .create(
                                    SelfPropellingBoatEntity::new,
                                    SpawnGroup.MISC
                            )
                            .dimensions(
                                    1.375F,
                                    0.5625F
                            )
                            .maxTrackingRange(10)
                            .trackingTickInterval(3)
                            .build(
                                    MinecartMagicMod.id(
                                            "self_propelling_boat"
                                    ).toString()
                            )
            );

    private ModEntities() {
    }

    public static void init() {
    }
}
