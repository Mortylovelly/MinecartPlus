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

    /*
     * Скорость поворота от A/D.
     *
     * Это именно управление направлением лодки,
     * а не изменение скорости двигателя.
     */
    private static final float STEERING_SPEED = 2.5F;

    private int fuel;

    public SelfPropellingBoatEntity(
            EntityType<? extends SelfPropellingBoatEntity> entityType,
            World world
    ) {
        super(entityType, world);

        /*
         * Пока существует только одна версия:
         * дубовая самоходная лодка.
         */
        setVariant(Type.OAK);
    }

    /**
     * Самоходная лодка может иметь пассажира,
     * но пассажир НЕ должен становиться ванильным
     * controlling passenger BoatEntity.
     *
     * Это главное отличие от обычной лодки.
     *
     * Благодаря этому ванильная BoatEntity не пытается
     * включать собственную управляемую лодочную физику
     * поверх нашего двигателя.
     */
    @Override
    public PlayerEntity getControllingPassenger() {
        return null;
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
                Math.min(
                        MAX_FUEL,
                        fuel
                )
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

    private Vec3d getForwardDirection() {
        double yawRadians =
                Math.toRadians(getYaw());

        return new Vec3d(
                -Math.sin(yawRadians),
                0.0D,
                Math.cos(yawRadians)
        );
    }

    /**
     * Собственное управление самоходной лодкой.
     *
     * A:
     * поворот влево.
     *
     * D:
     * поворот вправо.
     *
     * W:
     * полностью игнорируется.
     *
     * S:
     * полностью игнорируется.
     *
     * Двигатель всегда толкает лодку строго вперёд
     * относительно её текущего yaw.
     */
    public void handleSelfPropulsion(
            boolean pressingLeft,
            boolean pressingRight
    ) {
        /*
         * На клиенте не выполняем авторитетную
         * физику двигателя.
         */
        if (getWorld().isClient()) {
            return;
        }

        /*
         * Управлять направлением можно только
         * когда есть пассажир.
         */
        if (!getPassengerList().isEmpty()) {

            if (pressingLeft && !pressingRight) {
                setYaw(
                        getYaw() - STEERING_SPEED
                );
            }

            if (pressingRight && !pressingLeft) {
                setYaw(
                        getYaw() + STEERING_SPEED
                );
            }
        }

        /*
         * Нет топлива.
         */
        if (fuel <= 0) {
            return;
        }

        /*
         * Двигатель работает только в воде.
         */
        if (!isTouchingWater()) {
            return;
        }

        Vec3d forward =
                getForwardDirection();

        Vec3d velocity =
                getVelocity();

        double horizontalSpeed =
                Math.sqrt(
                        velocity.x * velocity.x
                                + velocity.z * velocity.z
                );

        double maxSpeed =
                getMaximumSpeed();

        double newSpeed;

        if (horizontalSpeed < maxSpeed) {

            newSpeed =
                    Math.min(
                            maxSpeed,
                            horizontalSpeed
                                    + ACCELERATION
                    );

        } else {

            newSpeed =
                    maxSpeed;
        }

        /*
         * Расход топлива.
         */
        fuel--;

        /*
         * Сбрасываем старое боковое/заднее движение
         * и задаём скорость строго по носу.
         */
        setVelocity(
                forward.x * newSpeed,
                velocity.y,
                forward.z * newSpeed
        );

        velocityDirty = true;
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
         * Уголь / древесный уголь:
         * заправка двигателя.
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
         * Любое другое взаимодействие —
         * ванильная посадка / выход.
         */
        return super.interact(
                player,
                hand
        );
    }

    @Override
    public void tick() {
        /*
         * Вся ванильная логика BoatEntity
         * всё ещё выполняется.
         *
         * Но наш getControllingPassenger()
         * возвращает null, поэтому пассажир больше
         * не включает ванильную систему управления.
         */
        super.tick();
    }

    @Override
    public boolean damage(
            DamageSource source,
            float amount
    ) {
        if (isRemoved()) {
            return true;
        }

        /*
         * Creative:
         * предмет не выпадает.
         */
        if (source.getAttacker()
                instanceof PlayerEntity player
                && player.getAbilities().creativeMode) {

            discard();

            return true;
        }

        ItemStack stack =
                new ItemStack(
                        ModItems.SELF_PROPELLING_BOAT
                );

        /*
         * Сохраняем Tailwind при дропе.
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
