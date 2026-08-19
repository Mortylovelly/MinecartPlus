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
     * Базовая скорость самоходной лодки
     * без зачарования.
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
     * Пределы двигателя.
     *
     * 0  -> 0.60
     * I  -> 0.77
     * II -> 0.87
     * III-> 0.99
     */
    private static final double BASE_ENGINE_LIMIT = 0.60D;

    private static final double TAILWIND_I_LIMIT = 0.77D;
    private static final double TAILWIND_II_LIMIT = 0.87D;
    private static final double TAILWIND_III_LIMIT = 0.99D;

    /*
     * Прямой множитель тяги двигателя.
     *
     * 0  -> x1.00
     * I  -> x1.20
     * II -> x1.35
     * III-> x1.50
     */
    private static final double TAILWIND_I_MULTIPLIER = 1.20D;
    private static final double TAILWIND_II_MULTIPLIER = 1.35D;
    private static final double TAILWIND_III_MULTIPLIER = 1.50D;

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
     * ПОЛУЧЕНИЕ УРОВНЯ TAILWIND
     * =====================================================
     */
    private int getCurrentTailwindLevel() {

        int level =
                getEngineTailwindLevel();

        /*
         * В штатном случае уровень уже был
         * перенесён SelfPropellingBoatItem
         * при создании лодки.
         */
        if (level > 0) {
            return level;
        }

        /*
         * Резервная проверка настоящего attachment.
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
     * =====================================================
     * МАКСИМАЛЬНАЯ СКОРОСТЬ
     * =====================================================
     */
    public double getMaximumSpeed() {

        int level =
                getCurrentTailwindLevel();

        return switch (level) {

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

    /*
     * =====================================================
     * ПРЯМОЙ БОНУС TAILWIND К ТЯГЕ
     * =====================================================
     *
     * Это уже НЕ просто увеличение потолка.
     *
     * Tailwind непосредственно усиливает
     * двигатель при каждом расчёте движения.
     */
    private double getTailwindEngineMultiplier() {

        int level =
                getCurrentTailwindLevel();

        return switch (level) {

            case 1 ->
                    TAILWIND_I_MULTIPLIER;

            case 2 ->
                    TAILWIND_II_MULTIPLIER;

            case 3 ->
                    TAILWIND_III_MULTIPLIER;

            default ->
                    1.00D;
        };
    }

    /*
     * =====================================================
     * СИЛА ДВИГАТЕЛЯ
     * =====================================================
     */
    private double getEngineAcceleration() {

        /*
         * Сначала получаем обычную тягу.
         */
        double baseAcceleration =
                BASE_ACCELERATION;

        /*
         * Теперь непосредственно умножаем
         * её на Tailwind.
         */
        return baseAcceleration
                * getTailwindEngineMultiplier();
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
         * обычное управление ванильной лодкой.
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
         * С топливом:
         *
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

        /*
         * Не меняем yaw вручную на клиенте.
         *
         * Это оставляем ванильной BoatEntity.
         *
         * Так client/server не расходятся
         * по направлению.
         */

        Vec3d velocity =
                getVelocity();

        /*
         * Используем уже актуальный yaw лодки.
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
         * Проекция текущей скорости
         * на направление движения.
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
         * Получаем Tailwind напрямую
         * из существующей системы.
         */
        int tailwindLevel =
                getCurrentTailwindLevel();

        /*
         * Получаем лимит двигателя.
         */
        double maximumSpeed =
                getMaximumSpeed();

        /*
         * Получаем УСИЛЕННУЮ Tailwind
         * тягу двигателя.
         */
        double acceleration =
                getEngineAcceleration();

        /*
         * Плавно разгоняем лодку.
         */
        double targetSpeed =
                forwardSpeed
                        + acceleration;

        /*
         * Ограничиваем итоговую скорость
         * лимитом двигателя.
         */
        targetSpeed =
                Math.min(
                        targetSpeed,
                        maximumSpeed
                );

        /*
         * При старте двигатель должен
         * сразу дать движение.
         *
         * Сам базовый порог также немного
         * зависит от Tailwind.
         */
        if (targetSpeed < BASE_ENGINE_SPEED) {

            targetSpeed =
                    Math.min(
                            BASE_ENGINE_SPEED,
                            maximumSpeed
                    );
        }

        /*
         * ПРИМЕНЯЕМ ДВИЖЕНИЕ.
         *
         * X/Z управляются двигателем.
         * Y полностью оставляем ванильной.
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
         * открыть существующее меню.
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

        /*
         * Creative.
         */
        if (source.getAttacker()
                instanceof PlayerEntity player
                && player.getAbilities().creativeMode) {

            discard();

            return true;
        }

        /*
         * Возвращаем топливо.
         */
        ItemStack fuelStack =
                fuelInventory.removeStack(
                        0
                );

        if (!fuelStack.isEmpty()) {

            dropStack(
                    fuelStack
            );
        }

        /*
         * Возвращаем самоходную лодку.
         */
        ItemStack boatStack =
                new ItemStack(
                        ModItems.SELF_PROPELLING_BOAT
                );

        /*
         * Сохраняем Tailwind.
         */
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

        /*
         * Восстанавливаем Tailwind
         * для старых сохранённых лодок.
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
