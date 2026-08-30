package com.minecartmagic.entity;

import com.minecartmagic.ModEnchantments;
import com.minecartmagic.ModItems;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.Entity;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SelfPropellingBoatEntity
        extends BoatEntity
        implements ExtendedScreenHandlerFactory<Integer>, GeoEntity {

    private static final Logger LOGGER =
            LoggerFactory.getLogger("MinecartMagic");

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
     * =====================================================
     * СКОРОСТИ ДВИГАТЕЛЯ
     * =====================================================
     */

    private static final double ENGINE_SPEED_NO_TAILWIND = 0.50D;
    private static final double ENGINE_SPEED_TAILWIND_I = 0.65D;
    private static final double ENGINE_SPEED_TAILWIND_II = 0.75D;
    private static final double ENGINE_SPEED_TAILWIND_III = 0.88D;

    /*
     * Скорость руления.
     */
    private static final float STEERING_SPEED = 2.5F;

    /*
     * =====================================================
     * DEBUG
     * =====================================================
     */

    private int debugTickCounter = 0;

    private int debugLastEngineLevel = -1;
    private int debugLastAttachmentLevel = -1;
    private int debugLastFinalLevel = -1;

    private final SimpleInventory fuelInventory =
            new SimpleInventory(1);

    /*
     * =====================================================
     * GeckoLib
     * =====================================================
     */

    private final AnimatableInstanceCache geoCache =
            GeckoLibUtil.createInstanceCache(this);

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
     * ПАССАЖИР
     * =====================================================
     *
     * Используем стандартную посадку BoatEntity,
     * но после неё разворачиваем пассажира на 180°.
     *
     * Саму лодку НЕ вращаем.
     * Физика и yaw лодки НЕ меняются.
     */

    @Override
    protected void updatePassengerPosition(
            Entity passenger,
            Entity.PositionUpdater positionUpdater
    ) {
        super.updatePassengerPosition(
                passenger,
                positionUpdater
        );

        /*
         * Самоходная лодка визуально развернута
         * относительно стандартной ориентации посадки
         * пассажира.
         *
         * Поэтому разворачиваем только пассажира.
         */

        float yaw =
                passenger.getYaw() + 180.0F;

        passenger.setYaw(
                yaw
        );

        passenger.setHeadYaw(
                yaw
        );

        passenger.setBodyYaw(
                yaw
        );
    }

    @Override
    protected void clampPassengerYaw(
            Entity passenger
    ) {
        /*
         * Не используем стандартное ограничение BoatEntity,
         * которое снова пытается притянуть пассажира
         * к yaw самой лодки.
         *
         * Наш passenger уже намеренно развернут на 180°.
         */
    }

    @Override
    public void onPassengerLookAround(
            Entity passenger
    ) {
        /*
         * Ничего не делаем.
         *
         * Иначе BoatEntity снова синхронизирует направление
         * взгляда пассажира с собственной ориентацией.
         */
    }

    /*
     * =====================================================
     * УРОВЕНЬ TAILWIND
     * =====================================================
     */

    private int getCurrentTailwindLevel() {

        int engineLevel =
                getEngineTailwindLevel();

        int attachmentLevel =
                ModEnchantments.getTailwindLevel(
                        this
                );

        int finalLevel;

        if (engineLevel > 0) {
            finalLevel = engineLevel;
        } else {
            finalLevel = attachmentLevel;
        }

        finalLevel =
                Math.max(
                        0,
                        Math.min(
                                3,
                                finalLevel
                        )
                );

        if (engineLevel != debugLastEngineLevel
                || attachmentLevel != debugLastAttachmentLevel
                || finalLevel != debugLastFinalLevel) {

            LOGGER.info(
                    "[MinecartMagic DEBUG] Self-propelling boat #{} Tailwind changed: " +
                            "engineLevel={}, attachmentLevel={}, finalLevel={}, hasFuel={}, side={}",
                    getId(),
                    engineLevel,
                    attachmentLevel,
                    finalLevel,
                    hasFuel(),
                    getWorld().isClient()
                            ? "CLIENT"
                            : "SERVER"
            );

            debugLastEngineLevel =
                    engineLevel;

            debugLastAttachmentLevel =
                    attachmentLevel;

            debugLastFinalLevel =
                    finalLevel;
        }

        if (finalLevel > 0
                && engineLevel != finalLevel) {

            setEngineTailwindLevel(
                    finalLevel
            );
        }

        return finalLevel;
    }

    /*
     * =====================================================
     * СКОРОСТЬ
     * =====================================================
     */

    public double getMaximumSpeed() {

        int level =
                getCurrentTailwindLevel();

        return switch (level) {

            case 1 ->
                    ENGINE_SPEED_TAILWIND_I;

            case 2 ->
                    ENGINE_SPEED_TAILWIND_II;

            case 3 ->
                    ENGINE_SPEED_TAILWIND_III;

            default ->
                    ENGINE_SPEED_NO_TAILWIND;
        };
    }

    @Override
    public void setInputs(
            boolean pressingLeft,
            boolean pressingRight,
            boolean pressingForward,
            boolean pressingBack
    ) {

        if (!hasFuel()) {

            super.setInputs(
                    pressingLeft,
                    pressingRight,
                    pressingForward,
                    pressingBack
            );

            return;
        }

        super.setInputs(
                pressingLeft,
                pressingRight,
                false,
                false
        );
    }

    /*
     * =====================================================
     * ДВИГАТЕЛЬ
     * =====================================================
     */

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

        int tailwindLevel =
                getCurrentTailwindLevel();

        double targetSpeed =
                getMaximumSpeed();

        double currentForwardSpeed =
                velocity.x * forward.x
                        + velocity.z * forward.z;

        currentForwardSpeed =
                Math.max(
                        0.0D,
                        currentForwardSpeed
                );

        if (currentForwardSpeed > targetSpeed) {

            targetSpeed =
                    currentForwardSpeed;
        }

        debugTickCounter++;

        if (debugTickCounter >= 20) {

            debugTickCounter = 0;

            LOGGER.info(
                    "[MinecartMagic DEBUG] Self-propelling boat #{} ENGINE: " +
                            "tailwind={}, currentSpeed={}, targetSpeed={}, maxSpeed={}, " +
                            "hasFuel={}, water={}, side={}, yaw={}",
                    getId(),
                    tailwindLevel,
                    currentForwardSpeed,
                    targetSpeed,
                    getMaximumSpeed(),
                    hasFuel(),
                    isTouchingWater(),
                    clientSide
                            ? "CLIENT"
                            : "SERVER",
                    getYaw()
            );
        }

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

    /*
     * =====================================================
     * GeckoLib
     * =====================================================
     */

    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {
        /*
         * Анимаций пока нет.
         */
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
