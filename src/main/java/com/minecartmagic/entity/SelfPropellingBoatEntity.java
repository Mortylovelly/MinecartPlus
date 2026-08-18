package com.minecartmagic.entity;

import com.minecartmagic.ModEnchantments;
import com.minecartmagic.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
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

    private static final TrackedData<Integer> FUEL =
            DataTracker.registerData(
                    SelfPropellingBoatEntity.class,
                    TrackedDataHandlerRegistry.INTEGER
            );

    private static final int MAX_FUEL = 3200;
    private static final int FUEL_PER_COAL = 1600;

    private static final double BASE_MAX_SPEED = 0.35D;
    private static final double ACCELERATION = 0.025D;

    /*
     * Управление рулём:
     * A = влево
     * D = вправо
     */
    private static final float STEERING_SPEED = 2.5F;

    public SelfPropellingBoatEntity(
            EntityType<? extends SelfPropellingBoatEntity> entityType,
            World world
    ) {
        super(entityType, world);

        setVariant(Type.OAK);
    }

    @Override
    protected void initDataTracker(
            DataTracker.Builder builder
    ) {
        super.initDataTracker(builder);

        builder.add(
                FUEL,
                0
        );
    }

    /*
     * Самоходная лодка имеет только одно место.
     *
     * BoatEntity использует этот лимит при проверке
     * возможности посадки пассажира.
     */
    @Override
    protected int getMaxPassengers() {
        return 1;
    }

    public int getFuel() {
        return getDataTracker().get(FUEL);
    }

    public int getMaxFuel() {
        return MAX_FUEL;
    }

    public boolean hasFuel() {
        return getFuel() > 0;
    }

    public void setFuel(int fuel) {
        int clamped =
                Math.max(
                        0,
                        Math.min(
                                MAX_FUEL,
                                fuel
                        )
                );

        getDataTracker().set(
                FUEL,
                clamped
        );
    }

    public boolean addFuel(ItemStack stack) {
        if (!stack.isOf(Items.COAL)
                && !stack.isOf(Items.CHARCOAL)) {
            return false;
        }

        if (getFuel() >= MAX_FUEL) {
            return false;
        }

        setFuel(
                getFuel() + FUEL_PER_COAL
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
     * Самоходный режим.
     *
     * С топливом:
     *
     * W = ничего
     * S = ничего
     * A = поворот влево
     * D = поворот вправо
     *
     * Двигатель всегда толкает лодку вперёд.
     *
     * clientSide = true:
     * используется локальный ввод игрока.
     *
     * clientSide = false:
     * сервер просто использует уже актуальный yaw лодки.
     */
    public void applySelfPropulsion(
            boolean pressingLeft,
            boolean pressingRight,
            boolean clientSide
    ) {
        if (!hasFuel()) {
            return;
        }

        /*
         * Если это сервер — не пытаемся читать
         * клиентский A/D напрямую.
         *
         * Сервер получает актуальный yaw лодки
         * через обычную синхронизацию транспорта.
         */
        if (clientSide) {

            if (pressingLeft && !pressingRight) {
                setYaw(
                        getYaw() - STEERING_SPEED
                );
            } else if (pressingRight && !pressingLeft) {
                setYaw(
                        getYaw() + STEERING_SPEED
                );
            }
        }

        /*
         * Самоходный двигатель работает только на воде.
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
         * Только сервер действительно расходует топливо.
         *
         * На клиенте топливо будет синхронизировано
         * через DataTracker.
         */
        if (!clientSide) {
            setFuel(
                    getFuel() - 1
            );
        }

        /*
         * Полностью задаём горизонтальную скорость
         * по текущему носу лодки.
         *
         * Никаких старых случайных push-векторов.
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
         * заправка двигателя.
         */
        if (stack.isOf(Items.COAL)
                || stack.isOf(Items.CHARCOAL)) {

            if (getFuel() >= MAX_FUEL) {
                return ActionResult.FAIL;
            }

            if (!getWorld().isClient()) {

                if (addFuel(stack)) {

                    if (!player.getAbilities().creativeMode) {
                        stack.decrement(1);
                    }

                    player.swingHand(hand);
                }
            }

            return ActionResult.success(
                    getWorld().isClient()
            );
        }

        /*
         * Без топлива здесь работает обычная
         * посадка BoatEntity.
         *
         * С топливом посадка тоже обычная:
         * пассажир остаётся внутри, но наша
         * физика игнорирует W/S.
         */
        return super.interact(
                player,
                hand
        );
    }

    @Override
    public void tick() {
        /*
         * Мы не вмешиваемся в сам tick.
         *
         * Перехват физики производится
         * SelfPropellingBoatVelocityMixin.
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
         * Tailwind сохраняется.
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
                getFuel()
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
