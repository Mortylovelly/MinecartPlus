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

    public SelfPropellingBoatEntity(
            EntityType<? extends SelfPropellingBoatEntity> entityType,
            World world
    ) {
        super(entityType, world);

        /*
         * Пока у самоходной лодки только одна версия:
         * дубовая.
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

    /**
     * Получает строго горизонтальное направление,
     * куда в данный момент смотрит нос лодки.
     *
     * Никакого сохранённого вектора от игрока здесь нет.
     */
    private Vec3d getForwardDirection() {
        Vec3d look =
                getRotationVec(1.0F);

        Vec3d horizontal =
                new Vec3d(
                        look.x,
                        0.0D,
                        look.z
                );

        if (horizontal.lengthSquared()
                <= 0.000001D) {
            return Vec3d.ZERO;
        }

        return horizontal.normalize();
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
         * Уголь и древесный уголь заправляют двигатель.
         *
         * Направление здесь НЕ сохраняем.
         *
         * Лодка всегда должна ехать туда, куда
         * направлен её нос.
         */
        if (stack.isOf(Items.COAL)
                || stack.isOf(Items.CHARCOAL)) {

            if (fuel >= MAX_FUEL) {
                return ActionResult.FAIL;
            }

            if (!getWorld().isClient()) {

                addFuel(stack);

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
         * Обычное взаимодействие BoatEntity:
         * можно сесть в лодку.
         */
        return super.interact(
                player,
                hand
        );
    }

    @Override
    public void tick() {
        /*
         * Сначала полностью выполняем ванильный BoatEntity.tick().
         *
         * Именно здесь BoatEntity обрабатывает пассажира,
         * повороты и обычную физику лодки.
         *
         * Поэтому A/D продолжают работать.
         */
        super.tick();

        if (getWorld().isClient()) {
            return;
        }

        /*
         * Нет топлива -> двигателя нет.
         */
        if (fuel <= 0) {
            return;
        }

        /*
         * Двигатель работает только на воде.
         */
        if (!isTouchingWater()) {
            return;
        }

        Vec3d forward =
                getForwardDirection();

        if (forward.lengthSquared()
                <= 0.000001D) {
            return;
        }

        /*
         * Один тик = одна единица топлива.
         */
        fuel--;

        Vec3d velocity =
                getVelocity();

        double horizontalSpeed =
                Math.sqrt(
                        velocity.x * velocity.x
                                + velocity.z * velocity.z
                );

        double maxSpeed =
                getMaximumSpeed();

        /*
         * Ускоряемся строго по носу.
         *
         * Важное отличие от старой версии:
         * мы больше не используем pushDirection,
         * который мог быть направлен куда угодно.
         */
        if (horizontalSpeed < maxSpeed) {

            double newSpeed =
                    Math.min(
                            maxSpeed,
                            horizontalSpeed
                                    + ACCELERATION
                    );

            setVelocity(
                    forward.x * newSpeed,
                    velocity.y,
                    forward.z * newSpeed
            );

        } else {

            setVelocity(
                    forward.x * maxSpeed,
                    velocity.y,
                    forward.z * maxSpeed
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
         * Возвращаем Tailwind на выпавшую лодку.
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
                    enchantmentRegistry.entryOf(
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
    }

    @Override
    protected void readCustomDataFromNbt(
            NbtCompound nbt
    ) {
        super.readCustomDataFromNbt(nbt);

        setFuel(
                nbt.getInt("Fuel")
        );
    }
}
