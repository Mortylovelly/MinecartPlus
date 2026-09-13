package com.minecartmagic;

import com.minecartmagic.entity.AdvancedMinecartEntity;
import com.minecartmagic.entity.SelfPropellingBoatEntity;
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
                            .setDimensions(
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

    public static final EntityType<AdvancedMinecartEntity>
            ADVANCED_MINECART =
            Registry.register(
                    Registries.ENTITY_TYPE,
                    MinecartMagicMod.id(
                            "advanced_minecart"
                    ),
                    EntityType.Builder
                            .create(
                                    AdvancedMinecartEntity::new,
                                    SpawnGroup.MISC
                            )
                            .setDimensions(
                                    1.0F,
                                    0.7F
                            )
                            .maxTrackingRange(10)
                            .trackingTickInterval(3)
                            .build(
                                    MinecartMagicMod.id(
                                            "advanced_minecart"
                                    ).toString()
                            )
            );

    private ModEntities() {
    }

    public static void init() {
    }
}
