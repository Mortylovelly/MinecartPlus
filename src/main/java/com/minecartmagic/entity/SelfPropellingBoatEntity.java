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

    private static final int MAX_PASSENGERS = 1;

    /*
     * Базовая скорость двигателя.
     *
     * Используется только при работающем топливе.
     */
    private static final double BASE_ENGINE_SPEED = 0.45D;

    private static final double ACCELERATION = 0.025D;

    private static final float STEERING_SPEED = 2.5F;

    /*
     * Плавучесть самоходной лодки.
     *
     * Это не замена ванильной лодки целиком.
     * Она нужна только потому, что при работающем
     * двигателе мы отключаем ванильный updateVelocity().
     */
    private static final double BUOYANCY_MAX_UP = 0.12D;
    private static final double BUOYANCY_ACCELERATION = 0.035D;
    private static final double SURFACE_EPSILON = 0.08D;

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

        /*
         * Забираем только один предмет.
         */
        fuelStack.decrement(1);

        /*
         * Например:
         * лавовое ведро -> пустое ведро.
         */
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

    public double getMaximumSpeed() {

        int level =
                ModEnchantments.getTailwindLevel(
                        this
                );

        return switch (level) {
            case 1 -> 0.62D;
            case 2 -> 0.72D;
            case 3 -> 0.84D;
            default -> BASE_ENGINE_SPEED;
        };
    }

    /*
     * БЕЗ топлива:
     * ванильное управление.
     *
     * С топливом:
     * W/S отключаются,
     * A/D остаются рулём.
     */
    @Override
    public void setInputs(
            boolean pressingLeft,
            boolean pressingRight,
            boolean pressingForward,
            boolean pressingBack
    ) {
        if (hasFuel()) {

            super.setInputs(
                    pressingLeft,
                    pressingRight,
                    false,
                    false
            );

            return;
        }

        super.setInputs(
                pressingLeft,
                pressingRight,
                pressingForward,
                pressingBack
        );
    }

    /*
     * Возвращает вертикальную скорость,
     * которая не позволяет самоходной лодке
     * бесконечно тонуть.
     *
     * BoatEntity предоставляет getWaterHeightBelow()
     * для определения уровня воды. :contentReference[oaicite:1]{index=1}
     */
    private double calculateBuoyancy(
            double currentVerticalVelocity
    ) {
        float waterHeight =
                getWaterHeightBelow();

        /*
         * Если воды под лодкой фактически нет,
         * не вмешиваемся.
         */
        if (Float.isNaN(waterHeight)
                || Float.isInfinite(waterHeight)) {
            return currentVerticalVelocity;
        }

        double depth =
                waterHeight - getY();

        /*
         * Лодка ниже уровня воды:
         * постепенно ускоряем её вверх.
         */
        if (depth > SURFACE_EPSILON) {

            double upward =
                    Math.min(
                            BUOYANCY_MAX_UP,
                            BUOYANCY_ACCELERATION
                                    + depth * 0.08D
                    );

            return Math.max(
                    currentVerticalVelocity,
                    upward
            );
        }

        /*
         * Если лодка практически у поверхности,
         * не позволяем вертикальной скорости
         * продолжать тянуть её вниз.
         */
        if (depth > -SURFACE_EPSILON) {

            return Math.max(
                    currentVerticalVelocity,
                    0.0D
            );
        }

        /*
         * Над поверхностью оставляем ванильное
         * вертикальное поведение.
         */
        return currentVerticalVelocity;
    }

    public void applySelfPropulsion(
            boolean pressingLeft,
            boolean pressingRight,
            boolean clientSide
    ) {
        if (!hasFuel()) {
            return;
        }

        /*
         * Только A/D рулит.
         */
        if (clientSide) {

            if (pressingLeft
                    && !pressingRight) {

                setYaw(
                        getYaw()
                                - STEERING_SPEED
                );

            } else if (
                    pressingRight
                            && !pressingLeft
            ) {

                setYaw(
                        getYaw()
                                + STEERING_SPEED
                );
            }
        }

        /*
         * Даже если лодка находится под водой,
         * двигатель не должен продолжать её топить.
         *
         * Сначала рассчитываем вертикальную
         * плавучесть.
         */
        Vec3d velocity =
                getVelocity();

        double verticalVelocity =
                velocity.y;

        if (isTouchingWater()) {

            verticalVelocity =
                    calculateBuoyancy(
                            verticalVelocity
                    );
        }

        /*
         * Горизонтальная тяга работает
         * только по направлению носа.
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
         * Если лодка сильно погрузилась,
         * не позволяем старой горизонтальной
         * скорости превращаться в странный glide.
         *
         * Берём только движение по носу.
         */
        double forwardSpeed =
                velocity.x * forward.x
                        + velocity.z * forward.z;

        forwardSpeed =
                Math.max(
                        0.0D,
                        forwardSpeed
                );

        double targetSpeed =
                Math.min(
                        getMaximumSpeed(),
                        forwardSpeed
                                + ACCELERATION
                );

        setVelocity(
                forward.x * targetSpeed,
                verticalVelocity,
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
         * открыть GUI.
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
                ModEnchantments.getTailwindLevel(
                        this
                );

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
