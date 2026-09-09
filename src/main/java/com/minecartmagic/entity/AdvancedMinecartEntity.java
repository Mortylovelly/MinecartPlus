package com.minecartmagic.entity;

import com.minecartmagic.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AdvancedMinecartEntity extends MinecartEntity implements GeoEntity {

    private final AnimatableInstanceCache geoCache =
            GeckoLibUtil.createInstanceCache(this);

    public AdvancedMinecartEntity(
            EntityType<? extends AdvancedMinecartEntity> type,
            World world
    ) {
        super(type, world);
    }

    @Override
    public Item asItem() {
        return ModItems.ADVANCED_MINECART;
    }

    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {
        // No animations yet. The model is already prepared for GeckoLib animations.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
