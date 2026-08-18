package com.minecartmagic.entity;

import com.minecartmagic.ModEnchantments;
import com.minecartmagic.ModItems;
import com.minecartmagic.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SelfPropellingBoatEntity extends BoatEntity {

    private static final int MAX_FUEL = 3200;

    private int fuel;

    private Vec3d pushDirection = Vec3d.ZERO;

    public SelfPropellingBoatEntity(
            EntityType<? extends SelfPropellingBoatEntity> entityType,
            World world
    ) {
        super(entityType, world);
    }

    public int getFuel() {
        return fuel;
    }

    public void setFuel(int fuel) {
        this.fuel = Math.max(0, Math.min(MAX_FUEL, fuel));
    }

    public boolean addFuel(ItemStack stack) {
        if (stack.isOf(Items.COAL) || stack.isOf(Items.CHARCOAL)) {
            if (fuel >= MAX_FUEL) {
                return false;
            }

            fuel = Math.min(
                    MAX_FUEL,
                    fuel + 1600
            );

            return true;
        }

        return false;
    }

    public Vec3d getPushDirection() {
        return pushDirection;
    }

    public void setPushDirection(Vec3d direction) {
        if (direction.lengthSquared() <= 0.000001D) {
            pushDirection = Vec3d.ZERO;
            return;
        }

        Vec3d horizontal = new Vec3d(
                direction.x,
                0.0D,
                direction.z
        );

        if (horizontal.lengthSquared() <= 0.000001D) {
            pushDirection = Vec3d.ZERO;
            return;
        }

        pushDirection = horizontal.normalize();
    }

    @Override
    public Item asItem() {
        return ModItems.SELF_PROPELLING_BOAT;
    }

    @Override
    public void tick() {
        super.tick();

        if (getWorld().isClient()) {
            return;
        }

        if (fuel <= 0) {
            return;
        }

        if (!isTouchingWater()) {
            return;
        }

        if (pushDirection.lengthSquared() <= 0.000001D) {
            return;
        }

        fuel--;

        Vec3d velocity = getVelocity();

        double horizontalSpeed = Math.sqrt(
                velocity.x * velocity.x
                        + velocity.z * velocity.z
        );

        double maxSpeed = 0.35D;

        if (horizontalSpeed < maxSpeed) {
            double acceleration = 0.025D;

            double newSpeed = Math.min(
                    maxSpeed,
                    horizontalSpeed + acceleration
            );

            setVelocity(
                    pushDirection.x * newSpeed,
                    velocity.y,
                    pushDirection.z * newSpeed
            );
        } else {
            setVelocity(
                    pushDirection.x * maxSpeed,
                    velocity.y,
                    pushDirection.z * maxSpeed
            );
        }

        velocityDirty = true;
    }

    @Override
    public boolean damage(
            net.minecraft.entity.damage.DamageSource source,
            float amount
    ) {
        if (isRemoved()) {
            return true;
        }

        ItemStack stack = new ItemStack(
                ModItems.SELF_PROPELLING_BOAT
        );

        int tailwindLevel =
                ModEnchantments.getTailwindLevel(this);

        if (tailwindLevel > 0) {
            ModEnchantments.setTailwindLevel(
                    stack,
                    tailwindLevel
            );
        }

        dropStack(stack);

        discard();

        return true;
    }

    @Override
    protected void writeCustomDataToNbt(
            net.minecraft.nbt.NbtCompound nbt
    ) {
        super.writeCustomDataToNbt(nbt);

        nbt.putInt(
                "Fuel",
                fuel
        );

        nbt.putDouble(
                "PushX",
                pushDirection.x
        );

        nbt.putDouble(
                "PushY",
                pushDirection.y
        );

        nbt.putDouble(
                "PushZ",
                pushDirection.z
        );
    }

    @Override
    protected void readCustomDataFromNbt(
            net.minecraft.nbt.NbtCompound nbt
    ) {
        super.readCustomDataFromNbt(nbt);

        setFuel(
                nbt.getInt("Fuel")
        );

        setPushDirection(
                new Vec3d(
                        nbt.getDouble("PushX"),
                        nbt.getDouble("PushY"),
                        nbt.getDouble("PushZ")
                )
        );
    }
}
