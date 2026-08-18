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
         * Пока у нас только одна версия самоходной лодки:
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
     * Двигатель самоходной лодки.
     *
     * Этот метод вызывается из Mixin в конце
     * BoatEntity.updateVelocity().
     *
     * Благодаря этому:
     *
     * - сначала ванильная лодка обрабатывает A/D;
     * - затем мы берём уже актуальный yaw;
     * - W/S не имеют значения для двигателя;
     * - лодка всегда толкается строго вперёд;
     * - пассажир не отключает двигатель.
     */
    public void applySelfPropulsion() {

        if (getWorld().isClient()) {
            return;
        }

        if (fuel <= 0) {
            return;
        }

        if (!isTouchingWater()) {
            return;
        }

        /*
         * Направление строго по текущему yaw лодки.
         *
         * В Minecraft:
         * X = -sin(yaw)
         * Z =  cos(yaw)
         */
        double yawRadians =
                Math.toRadians(
                        getYaw()
                );

        Vec3d forward =
                new Vec3d(
                        -Math.sin(yawRadians),
                        0.0D,
                        Math.cos(yawRadians)
                );

        if (forward.lengthSquared()
                <= 0.000001D) {
            return;
        }

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
         * Двигатель потребляет топливо только тогда,
         * когда реально работает на воде.
         */
        fuel--;

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
         * ПКМ углём / древесным углём:
         * заправляем двигатель.
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
         * Всё остальное отдаём ванильной BoatEntity:
         * посадка, выход и т.д.
         */
        return super.interact(
                player,
                hand
        );
    }

    @Override
    public void tick() {
        /*
         * Здесь НЕ трогаем скорость.
         *
         * Вся физика двигателя теперь подключается
         * через BoatEntity.updateVelocity().
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
         * В Creative лодка ломается без выпадения предмета.
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
         * Возвращаем Tailwind на предмет.
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
