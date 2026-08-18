package com.minecartmagic.entity;

import com.minecartmagic.ModEnchantments;
import com.minecartmagic.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SelfPropellingBoatEntity extends BoatEntity {

    private static final int MAX_FUEL = 3200;
    private static final int FUEL_PER_COAL = 1600;

    private static final double BASE_MAX_SPEED = 0.35D;
    private static final double ACCELERATION = 0.025D;

    private int fuel;

    private Vec3d pushDirection = Vec3d.ZERO;

    public SelfPropellingBoatEntity(
            EntityType<? extends SelfPropellingBoatEntity> entityType,
            World world
    ) {
        super(entityType, world);

        /*
         * Пока существует только одна версия самоходной лодки:
         * дубовая.
         *
         * Это также необходимо для BoatEntityRenderer,
         * который выбирает ванильную модель по BoatEntity.Type.
         */
        setVariant(Type.OAK);
    }

    public int getFuel() {
        return fuel;
    }

    public int getMaxFuel() {
        return MAX_FUEL;
    }

    public void setFuel(int fuel) {
        this.fuel = Math.max(
                0,
                Math.min(MAX_FUEL, fuel)
        );
    }

    public boolean addFuel(ItemStack stack) {
        if (!stack.isOf(Items.COAL)
                && !stack.isOf(Items.CHARCOAL)) {
            return false;
        }

        if (fuel >= MAX_FUEL) {
            return false;
        }

        fuel = Math.min(
                MAX_FUEL,
                fuel + FUEL_PER_COAL
        );

        return true;
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

    private double getTailwindMultiplier() {
        int level =
                ModEnchantments.getTailwindLevel(this);

        return switch (level) {
            case 1 -> 1.08D;
            case 2 -> 1.14D;
            case 3 -> 1.20D;
            default -> 1.0D;
        };
    }

    private double getMaximumSpeed() {
        return BASE_MAX_SPEED
                * getTailwindMultiplier();
    }

    @Override
    public Item asItem() {
        return ModItems.SELF_PROPELLING_BOAT;
    }

    @Override
    public ActionResult interact(
            PlayerEntity player,
            Hand hand
    ) {
        ItemStack stack =
                player.getStackInHand(hand);

        /*
         * Уголь и древесный уголь заправляют лодку.
         *
         * Направление задаётся направлением взгляда игрока
         * в момент заправки.
         */
        if (stack.isOf(Items.COAL)
                || stack.isOf(Items.CHARCOAL)) {

            if (fuel >= MAX_FUEL) {
                return ActionResult.FAIL;
            }

            if (!getWorld().isClient()) {

                addFuel(stack);

                setPushDirection(
                        player.getRotationVec(1.0F)
                );

                if (!player.getAbilities().creativeMode) {
                    stack.decrement(1);
                }

                player.swingHand(hand);
            }

            return ActionResult.success(
                    getWorld().isClient()
            );
        }

        /*
         * Для остальных предметов сохраняем ванильное
         * поведение BoatEntity:
         *
         * игрок может сесть в лодку.
         */
        return super.interact(
                player,
                hand
        );
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

        /*
         * Один тик = одна единица топлива.
         */
        fuel--;

        Vec3d velocity = getVelocity();

        double horizontalSpeed = Math.sqrt(
                velocity.x * velocity.x
                        + velocity.z * velocity.z
        );

        double maxSpeed =
                getMaximumSpeed();

        if (horizontalSpeed < maxSpeed) {

            double newSpeed = Math.min(
                    maxSpeed,
                    horizontalSpeed + ACCELERATION
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
            DamageSource source,
            float amount
    ) {
        if (isRemoved()) {
            return true;
        }

        ItemStack stack =
                new ItemStack(
                        ModItems.SELF_PROPELLING_BOAT
                );

        /*
         * Возвращаем Tailwind на выпавший предмет.
         */
        int tailwindLevel =
                ModEnchantments.getTailwindLevel(this);

        if (tailwindLevel > 0) {
            var enchantmentRegistry =
                    getRegistryManager()
                            .get(
                                    RegistryKeys.ENCHANTMENT
                            );

            var tailwind =
                    enchantmentRegistry
                            .entryOf(
                                    ModEnchantments.TAILWIND_KEY
                            );

            stack.addEnchantment(
                    tailwind,
                    tailwindLevel
            );
        }

        dropStack(stack);

        discard();

        return true;
    }

    @Override
    protected void writeCustomDataToNbt(
            NbtCompound nbt
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
            NbtCompound nbt
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
