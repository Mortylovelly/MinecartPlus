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
     * Базовая скорость двигателя без зачарования.
     */
    private static final double BASE_ENGINE_SPEED = 0.60D;

    /*
     * Базовый разгон двигателя.
     */
    private static final double BASE_ACCELERATION = 0.025D;

    /*
     * Скорость руления.
     */
    private static final float STEERING_SPEED = 2.5F;

    /*
     * Прямой множитель скорости двигателя
     * от Попутного ветра.
     *
     * 0  -> x1.00
     * I  -> x1.15
     * II -> x1.30
     * III-> x1.45
     */
    private static final double TAILWIND_I_SPEED_MULTIPLIER = 1.15D;
    private static final double TAILWIND_II_SPEED_MULTIPLIER = 1.30D;
    private static final double TAILWIND_III_SPEED_MULTIPLIER = 1.45D;

    /*
     * Максимальные значения после применения
     * множителя Tailwind.
     *
     * 0  -> 0.60
     * I  -> 0.69
     * II -> 0.78
     * III-> 0.87
     */
    private static final double BASE_ENGINE_LIMIT = 0.60D;
    private static final double TAILWIND_I_LIMIT = 0.69D;
    private static final double TAILWIND_II_LIMIT = 0.78D;
    private static final double TAILWIND_III_LIMIT = 0.87D;

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
     * Получаем настоящий уровень Tailwind
     * самоходной лодки.
     */
    private int getCurrentTailwindLevel() {

        int level =
                getEngineTailwindLevel();

        /*
         * Обычно значение уже устанавливается
         * в SelfPropellingBoatItem во время спавна.
         */
        if (level > 0) {
            return level;
        }

        /*
         * Резервная проверка attachment.
         */
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

        return Math.max(
                0,
                Math.min(
                        3,
                        level
                )
        );
    }

    /*
     * Множитель скорости двигателя.
     */
    private double getTailwindSpeedMultiplier() {

        return switch (getCurrentTailwindLevel()) {

            case 1 ->
                    TAILWIND_I_SPEED_MULTIPLIER;

            case 2 ->
                    TAILWIND_II_SPEED_MULTIPLIER;

            case 3 ->
                    TAILWIND_III_SPEED_MULTIPLIER;

            default ->
                    1.00D;
        };
    }

    /*
     * Максимальная скорость двигателя.
     */
    public double getMaximumSpeed() {

        return switch (getCurrentTailwindLevel()) {

            case 1 ->
                    TAILWIND_I_LIMIT;

            case 2 ->
                    TAILWIND_II_LIMIT;

            case 3 ->
                    TAILWIND_III_LIMIT;

            default ->
                    BASE_ENGINE_LIMIT;
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
         * полностью обычная лодка.
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
         * A/D остаются рулением.
         * W/S отключены.
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

        Vec3d velocity =
                getVelocity();

        /*
         * Используем уже существующий yaw лодки.
         * Не меняем его вручную.
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
         * Текущая скорость вдоль носа.
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
         * Базовый разгон двигателя.
         */
        double acceleration =
                BASE_ACCELERATION;

        /*
         * Сила разгона тоже зависит от Tailwind.
         */
        acceleration *=
                getTailwindSpeedMultiplier();

        /*
         * Разгоняемся.
         */
        double targetSpeed =
                forwardSpeed
                        + acceleration;

        /*
         * Но обязательно держим
         * максимальный лимит.
         */
        targetSpeed =
                Math.min(
                        targetSpeed,
                        getMaximumSpeed()
                );

        /*
         * Начальный запуск двигателя.
         */
        if (targetSpeed < BASE_ENGINE_SPEED) {

            targetSpeed =
                    Math.min(
                            BASE_ENGINE_SPEED,
                            getMaximumSpeed()
                    );
        }

        /*
         * Самое главное:
         *
         * Tailwind дополнительно масштабирует
         * уже рассчитанную скорость двигателя.
         *
         * Поэтому:
         *
         * двигатель + Tailwind I
         * > двигатель без Tailwind
         *
         * двигатель + Tailwind II
         * > Tailwind I
         *
         * двигатель + Tailwind III
         * > Tailwind II
         */
        targetSpeed *=
                getTailwindSpeedMultiplier();

        /*
         * После множителя снова ограничиваем
         * абсолютным лимитом.
         */
        targetSpeed =
                Math.min(
                        targetSpeed,
                        getMaximumSpeed()
                );

        /*
         * Только горизонтальная скорость.
         *
         * Y полностью остаётся ванильной.
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
         * существующее меню.
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
                getCurrentTailwindLevel();

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
