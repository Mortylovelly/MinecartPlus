package com.minecartmagic.entity;

import com.minecartmagic.ModEnchantments;
import com.minecartmagic.ModItems;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SelfPropellingBoatEntity
        extends BoatEntity
        implements ExtendedScreenHandlerFactory<Integer> {

    private static final TrackedData<Integer> BURN_TIME =
            DataTracker.registerData(
                    SelfPropellingBoatEntity.class,
                    TrackedDataHandlerRegistry.INTEGER
            );

    private static final TrackedData<Integer> FUEL_TIME =
            DataTracker.registerData(
                    SelfPropellingBoatEntity.class,
                    TrackedDataHandlerRegistry.INTEGER
            );

    private static final TrackedData<Integer> ENGINE_TAILWIND_LEVEL =
            DataTracker.registerData(
                    SelfPropellingBoatEntity.class,
                    TrackedDataHandlerRegistry.INTEGER
            );

    private static final int MAX_PASSENGERS = 1;

    /*
     * Базовая скорость двигателя без Tailwind.
     */
    private static final double BASE_ENGINE_SPEED = 0.60D;

    /*
     * Базовое ускорение.
     */
    private static final double BASE_ACCELERATION = 0.025D;

    /*
     * Дополнительное ускорение от Tailwind.
     *
     * 0  -> 0.025
     * I  -> 0.035
     * II -> 0.045
     * III-> 0.055
     */
    private static final double TAILWIND_ACCELERATION_I = 0.035D;
    private static final double TAILWIND_ACCELERATION_II = 0.045D;
    private static final double TAILWIND_ACCELERATION_III = 0.055D;

    /*
     * Бонус двигателя поверх скорости
     * обычной лодки с тем же Tailwind.
     *
     * 0  -> 0.60
     * I  -> 0.77
     * II -> 0.87
     * III-> 0.99
     */
    private static final double ENGINE_SPEED_BONUS = 0.15D;

    private final SimpleInventory fuelInventory =
            new SimpleInventory(1);

    public SelfPropellingBoatEntity(
            EntityType<? extends SelfPropellingBoatEntity> entityType,
            World world
    ) {
        super(
                entityType,
                world
        );

        setVariant(Type.OAK);
    }

    @Override
    protected int getMaxPassengers() {
        return MAX_PASSENGERS;
    }

    @Override
    protected void initDataTracker(
            DataTracker.Builder builder
    ) {
        super.initDataTracker(builder);

        builder.add(
                BURN_TIME,
                0
        );

        builder.add(
                FUEL_TIME,
                0
        );

        builder.add(
                ENGINE_TAILWIND_LEVEL,
                0
        );
    }

    public SimpleInventory getFuelInventory() {
        return fuelInventory;
    }

    public int getBurnTime() {
        return getDataTracker().get(
                BURN_TIME
        );
    }

    public int getFuelTime() {
        return getDataTracker().get(
                FUEL_TIME
        );
    }

    public int getEngineTailwindLevel() {
        return getDataTracker().get(
                ENGINE_TAILWIND_LEVEL
        );
    }

    public void setEngineTailwindLevel(
            int level
    ) {
        getDataTracker().set(
                ENGINE_TAILWIND_LEVEL,
                Math.max(
                        0,
                        Math.min(
                                3,
                                level
                        )
                )
        );
    }

    private void setBurnTime(
            int value
    ) {
        getDataTracker().set(
                BURN_TIME,
                Math.max(
                        0,
                        value
                )
        );
    }

    private void setFuelTime(
            int value
    ) {
        getDataTracker().set(
                FUEL_TIME,
                Math.max(
                        0,
                        value
                )
        );
    }

    public boolean hasFuel() {
        return getBurnTime() > 0;
    }

    private void startBurningFuel() {

        if (getBurnTime() > 0) {
            return;
        }

        ItemStack fuelStack =
                fuelInventory.getStack(0);

        if (fuelStack.isEmpty()) {
            setFuelTime(0);
            return;
        }

        Integer fuelValue =
                FuelRegistry.INSTANCE.get(
                        fuelStack.getItem()
                );

        if (fuelValue == null
                || fuelValue <= 0) {

            setFuelTime(0);
            return;
        }

        setBurnTime(
                fuelValue
        );

        setFuelTime(
                fuelValue
        );

        Item fuelItem =
                fuelStack.getItem();

        fuelStack.decrement(1);

        if (fuelStack.isEmpty()
                && fuelItem.hasRecipeRemainder()) {

            Item remainder =
                    fuelItem.getRecipeRemainder();

            if (remainder != null) {
                fuelInventory.setStack(
                        0,
                        new ItemStack(
                                remainder
                        )
                );
            }
        }

        fuelInventory.markDirty();
    }

    private void tickFuel() {

        if (getWorld().isClient()) {
            return;
        }

        if (getBurnTime() <= 0) {
            startBurningFuel();
        }

        if (getBurnTime() > 0) {
            setBurnTime(
                    getBurnTime() - 1
            );
        }

        if (getBurnTime() <= 0) {
            setBurnTime(0);
            setFuelTime(0);
        }
    }

    /*
     * =====================================================
     * МАКСИМАЛЬНАЯ СКОРОСТЬ ДВИГАТЕЛЯ
     * =====================================================
     *
     * Обычная лодка:
     *
     * 0  -> 0.45
     * I  -> 0.62
     * II -> 0.72
     * III-> 0.84
     *
     * Самоходная:
     *
     * 0  -> 0.60
     * I  -> 0.77
     * II -> 0.87
     * III-> 0.99
     */
    public double getMaximumSpeed() {

        int level =
                getEngineTailwindLevel();

        /*
         * Если tracker ещё 0,
         * проверяем настоящий Tailwind attachment.
         */
        if (level <= 0) {

            int attachmentLevel =
                    ModEnchantments.getTailwindLevel(
                            this
                    );

            if (attachmentLevel > 0) {

                level =
                        Math.min(
                                3,
                                attachmentLevel
                        );

                setEngineTailwindLevel(
                        level
                );
            }
        }

        double normalBoatSpeed =
                switch (level) {
                    case 1 -> 0.62D;
                    case 2 -> 0.72D;
                    case 3 -> 0.84D;
                    default -> 0.45D;
                };

        /*
         * Двигатель всегда быстрее обычного
         * режима с тем же Tailwind.
         */
        return normalBoatSpeed
                + ENGINE_SPEED_BONUS;
    }

    private double getEngineAcceleration() {

        int level =
                getEngineTailwindLevel();

        /*
         * Дополнительная страховка:
         * если tracker ещё 0, читаем attachment.
         */
        if (level <= 0) {

            int attachmentLevel =
                    ModEnchantments.getTailwindLevel(
                            this
                    );

            if (attachmentLevel > 0) {

                level =
                        Math.min(
                                3,
                                attachmentLevel
                        );

                setEngineTailwindLevel(
                        level
                );
            }
        }

        return switch (level) {
            case 1 -> TAILWIND_ACCELERATION_I;
            case 2 -> TAILWIND_ACCELERATION_II;
            case 3 -> TAILWIND_ACCELERATION_III;
            default -> BASE_ACCELERATION;
        };
    }

    @Override
    public void setInputs(
            boolean pressingLeft,
            boolean pressingRight,
            boolean pressingForward,
            boolean pressingBack
    ) {

        /*
         * Без топлива:
         * обычное управление BoatEntity.
         */
        if (!hasFuel()) {
            super.setInputs(
                    pressingLeft,
                    pressingRight,
                    pressingForward,
                    pressingBack
            );

            return;
        }

        /*
         * С двигателем:
         *
         * A/D остаются штатным рулением BoatEntity.
         * W/S отключены.
         *
         * ВАЖНО:
         * мы больше НЕ делаем собственный
         * setYaw() в applySelfPropulsion().
         *
         * Так client/server используют одну
         * и ту же ванильную систему поворота.
         */
        super.setInputs(
                pressingLeft,
                pressingRight,
                false,
                false
        );
    }

    public void applySelfPropulsion(
            boolean pressingLeft,
            boolean pressingRight,
            boolean clientSide
    ) {

        if (!hasFuel()) {
            return;
        }

        if (!isTouchingWater()) {
            return;
        }

        /*
         * НИКАКОГО setYaw() здесь больше нет.
         *
         * Поворот выполняется ванильным BoatEntity
         * через pressingLeft/pressingRight.
         *
         * Это устраняет причину client/server
         * рассинхронизации, из-за которой появлялось:
         *
         * "moved wrongly!"
         */

        Vec3d velocity =
                getVelocity();

        /*
         * Берём уже актуальный yaw лодки.
         */
        double radians =
                Math.toRadians(
                        getYaw()
                );

        Vec3d forward =
                new Vec3d(
                        -Math.sin(radians),
                        0.0D,
                        Math.cos(radians)
                );

        /*
         * Скорость вдоль направления лодки.
         */
        double forwardSpeed =
                velocity.x * forward.x
                        + velocity.z * forward.z;

        forwardSpeed =
                Math.max(
                        0.0D,
                        forwardSpeed
                );

        /*
         * Лимит двигателя зависит от Tailwind.
         */
        double maximumSpeed =
                getMaximumSpeed();

        /*
         * Tailwind влияет также на силу разгона.
         */
        double acceleration =
                getEngineAcceleration();

        double targetSpeed =
                forwardSpeed
                        + acceleration;

        /*
         * Двигатель не может превышать
         * свой максимальный лимит.
         */
        targetSpeed =
                Math.min(
                        targetSpeed,
                        maximumSpeed
                );

        /*
         * При начале движения двигатель
         * сразу даёт базовую тягу.
         */
        if (targetSpeed < BASE_ENGINE_SPEED) {

            targetSpeed =
                    Math.min(
                            BASE_ENGINE_SPEED,
                            maximumSpeed
                    );
        }

        /*
         * Меняем ТОЛЬКО горизонтальную скорость.
         *
         * Y остаётся полностью ванильной.
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

        /*
         * Shift + ПКМ снаружи:
         * открыть меню топлива.
         */
        if (player.isSneaking()
                && !getPassengerList().contains(player)) {

            if (!getWorld().isClient()) {
                player.openHandledScreen(
                        this
                );
            }

            return ActionResult.success(
                    getWorld().isClient()
            );
        }

        return super.interact(
                player,
                hand
        );
    }

    @Override
    public void tick() {

        tickFuel();

        /*
         * Ванильная физика BoatEntity.
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

        if (source.getAttacker()
                instanceof PlayerEntity player
                && player.getAbilities().creativeMode) {

            discard();

            return true;
        }

        ItemStack fuelStack =
                fuelInventory.removeStack(
                        0
                );

        if (!fuelStack.isEmpty()) {
            dropStack(
                    fuelStack
            );
        }

        ItemStack boatStack =
                new ItemStack(
                        ModItems.SELF_PROPELLING_BOAT
                );

        int tailwindLevel =
                getEngineTailwindLevel();

        if (tailwindLevel <= 0) {

            tailwindLevel =
                    ModEnchantments.getTailwindLevel(
                            this
                    );
        }

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

            boatStack.addEnchantment(
                    tailwind,
                    tailwindLevel
            );
        }

        dropStack(
                boatStack
        );

        discard();

        return true;
    }

    @Override
    protected void writeCustomDataToNbt(
            NbtCompound nbt
    ) {

        super.writeCustomDataToNbt(
                nbt
        );

        nbt.putInt(
                "BurnTime",
                getBurnTime()
        );

        nbt.putInt(
                "FuelTime",
                getFuelTime()
        );

        nbt.putInt(
                "EngineTailwindLevel",
                getEngineTailwindLevel()
        );

        nbt.put(
                "FuelInventory",
                fuelInventory.toNbtList(
                        getRegistryManager()
                )
        );
    }

    @Override
    protected void readCustomDataFromNbt(
            NbtCompound nbt
    ) {

        super.readCustomDataFromNbt(
                nbt
        );

        setBurnTime(
                nbt.getInt(
                        "BurnTime"
                )
        );

        setFuelTime(
                nbt.getInt(
                        "FuelTime"
                )
        );

        setEngineTailwindLevel(
                nbt.getInt(
                        "EngineTailwindLevel"
                )
        );

        /*
         * Совместимость со старыми лодками.
         */
        if (getEngineTailwindLevel() <= 0) {

            int attachmentLevel =
                    ModEnchantments.getTailwindLevel(
                            this
                    );

            if (attachmentLevel > 0) {

                setEngineTailwindLevel(
                        attachmentLevel
                );
            }
        }

        if (nbt.contains(
                "FuelInventory",
                NbtElement.LIST_TYPE
        )) {

            fuelInventory.readNbtList(
                    nbt.getList(
                            "FuelInventory",
                            NbtElement.COMPOUND_TYPE
                    ),
                    getRegistryManager()
            );
        }
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(
                "container.minecartmagic.self_propelling_boat"
        );
    }

    @Override
    public ScreenHandler createMenu(
            int syncId,
            PlayerInventory playerInventory,
            PlayerEntity player
    ) {
        return new com.minecartmagic.screen
                .SelfPropellingBoatScreenHandler(
                        syncId,
                        playerInventory,
                        this
                );
    }

    @Override
    public Integer getScreenOpeningData(
            ServerPlayerEntity player
    ) {
        return getId();
    }
}
