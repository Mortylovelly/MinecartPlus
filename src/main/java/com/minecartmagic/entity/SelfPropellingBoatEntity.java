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
     * Скорость поворота A/D в градусах за тик.
     */
    private static final float STEERING_SPEED = 2.5F;

    private int fuel;

    public SelfPropellingBoatEntity(
            EntityType<? extends SelfPropellingBoatEntity> entityType,
            World world
    ) {
        super(entityType, world);

        /*
         * Пока доступна только одна версия:
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

    public boolean hasFuel() {
        return fuel > 0;
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
        double radians =
                Math.toRadians(
                        getYaw()
                );

        return new Vec3d(
                -Math.sin(radians),
                0.0D,
                Math.cos(radians)
        );
    }

    /**
     * Полностью автономный режим.
     *
     * С топливом:
     *
     * A = поворот влево
     * D = поворот вправо
     * W = игнорируется
     * S = игнорируется
     *
     * Лодка сама постоянно получает тягу вперёд.
     */
    public void applySelfPropulsion(
            boolean pressingLeft,
            boolean pressingRight
    ) {
        if (getWorld().isClient()) {
            return;
        }

        if (!hasFuel()) {
            return;
        }

        if (!isTouchingWater()) {
            return;
        }

        /*
         * A / D управляют направлением.
         *
         * Одновременно нажатые A+D
         * не дают вращения.
         */
        if (pressingLeft && !pressingRight) {
            setYaw(
                    getYaw() - STEERING_SPEED
            );
        } else if (pressingRight && !pressingLeft) {
            setYaw(
                    getYaw() + STEERING_SPEED
            );
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

        double targetSpeed;

        if (horizontalSpeed < maxSpeed) {

            targetSpeed =
                    Math.min(
                            maxSpeed,
                            horizontalSpeed
                                    + ACCELERATION
                    );

        } else {

            targetSpeed =
                    maxSpeed;
        }

        /*
         * Одну единицу топлива за тик.
         */
        fuel--;

        /*
         * Полностью задаём горизонтальную скорость
         * по текущему носу лодки.
         *
         * Поэтому старый боковой/random glide
         * не сохраняется.
         */
        setVelocity(
                forward.x * targetSpeed,
                velocity.y,
                forward.z * targetSpeed
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
         * заправляем двигатель.
         */
        if (stack.isOf(Items.COAL)
                || stack.isOf(Items.CHARCOAL)) {

            if (fuel >= MAX_FUEL) {
                return ActionResult.FAIL;
            }

            if (!getWorld().isClient()) {

                addFuel(stack);

                /*
                 * Creative не расходует уголь.
                 */
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
         * Если это не топливо —
         * используем полностью ванильное
         * поведение BoatEntity.
         *
         * Благодаря этому без топлива:
         *
         * W/A/S/D = обычная лодка.
         */
        return super.interact(
                player,
                hand
        );
    }

    @Override
    public void tick() {
        /*
         * Ничего не переопределяем.
         *
         * BoatEntity.tick() занимается:
         * пассажиром,
         * посадкой,
         * выходом,
         * ориентацией,
         * обычной физикой.
         *
         * Когда топлива нет —
         * всё работает ванильно.
         *
         * Когда топливо есть —
         * SelfPropellingBoatVelocityMixin
         * перехватывает updateVelocity().
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
         * ломаем без выпадения предмета.
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
         * Сохраняем Tailwind.
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
