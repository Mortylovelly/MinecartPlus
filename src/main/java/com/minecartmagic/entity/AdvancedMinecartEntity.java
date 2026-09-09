package com.minecartmagic.entity;

import com.minecartmagic.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AdvancedMinecartEntity extends MinecartEntity implements GeoEntity {

    private final AnimatableInstanceCache geoCache =
            GeckoLibUtil.createInstanceCache(this);

    private float placementYaw;
    private boolean placementYawSet;

    public AdvancedMinecartEntity(
            EntityType<? extends AdvancedMinecartEntity> type,
            World world
    ) {
        super(type, world);
    }

    public void setPlacementYaw(float yaw) {
        placementYaw = yaw;
        placementYawSet = true;
    }

    public float getPlacementYaw() {
        return placementYawSet ? placementYaw : getYaw();
    }

    @Override
    public void tick() {
        super.tick();

        // AbstractMinecartEntity updates its normal entity yaw from movement.
        // This cart has a dedicated visual orientation chosen when it is placed,
        // so keep the entity yaw locked to that direction as well. Otherwise
        // vanilla minecart rotation can smoothly turn the GeckoLib model sideways.
        if (placementYawSet) {
            float yaw = placementYaw;
            setYaw(yaw);
            setHeadYaw(yaw);
            setBodyYaw(yaw);
            prevYaw = yaw;
        }
    }

    @Override
    public Item asItem() {
        return ModItems.ADVANCED_MINECART;
    }

    @Override
    public ActionResult interact(
            PlayerEntity player,
            Hand hand
    ) {
        // This cart is a controller/engine unit, not a rideable cart.
        // Do not call MinecartEntity.super.interact(), because that would
        // mount the player as a passenger.
        return ActionResult.success(getWorld().isClient());
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

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("MinecartMagicPlacementYawSet", placementYawSet);
        nbt.putFloat("MinecartMagicPlacementYaw", placementYaw);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        placementYawSet = nbt.getBoolean("MinecartMagicPlacementYawSet");
        placementYaw = nbt.getFloat("MinecartMagicPlacementYaw");
    }
}
