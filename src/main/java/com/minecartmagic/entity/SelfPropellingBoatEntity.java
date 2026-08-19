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

    /*
     * Уровень Попутного ветра, которым оснащён двигатель.
     *
     * Это DataTracker самой НАШЕЙ сущности.
     * Никаких Mixin/DataTracker-расширений ванильных вагонеток
     * здесь нет.
     */
    private static final TrackedData<Integer> ENGINE_TAILWIND_LEVEL =
            DataTracker.registerData(
                    SelfPropellingBoatEntity.class,
                    TrackedDataHandlerRegistry.INTEGER
            );

    private static final int MAX_PASSENGERS = 1;

    /*
     * Управление в двигательном режиме.
     *
     * ВАЖНО:
     * мы НЕ используем pressingLeft/pressingRight
     * ванильной BoatEntity как источник истины.
     *
     * Иначе ванильная лодка начинает одновременно
     * обрабатывать своё управление.
     */
    private boolean enginePressingLeft;
    private boolean enginePressingRight;

    /*
     * Скорость двигателя без Попутного ветра.
     */
    private static final double BASE_ENGINE_SPEED = 0.45D;

    /*
     * Разгон.
     */
    private static final double ENGINE_ACCELERATION = 0.025D;

    /*
     * Поворот A/D.
     */
    private static final float ENGINE_STEERING_SPEED = 2.5F;

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

        setVariant(
                Type.OAK
        );
    }

    @Override
    protected int getMaxPassengers() {
        return MAX_PASSENGERS;
    }

    @Override
    protected void initDataTracker(
            DataTracker.Builder builder
    ) {
        super.initDataTracker(
                builder
        );

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

    /*
     * =========================================================
     * ТОПЛИВО
     * =========================================================
     */

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
         * Потребляем только ОДИН предмет.
         */
        fuelStack.decrement(1);

        /*
         * Lava bucket -> empty bucket
         * и прочие recipe remainder.
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

    /*
     * =========================================================
     * УРОВЕНЬ TAILWIND
     * =========================================================
     *
     * На сервере периодически проверяем attachment.
     *
     * Это делает систему устойчивой даже если сущность
     * была создана старым способом или загружена из мира.
     */
    private void synchronizeTailwindLevel() {

        int attachmentLevel =
                ModEnchantments.getTailwindLevel(
                        this
                );

        int storedLevel =
                getEngineTailwindLevel();

        if (attachmentLevel != storedLevel) {

            setEngineTailwindLevel(
                    attachmentLevel
            );
        }
    }

    /*
     * =========================================================
     * СКОРОСТЬ ДВИГАТЕЛЯ
     * =========================================================
     *
     * 0 = 0.45
     * I = 0.62
     * II = 0.72
     * III = 0.84
     */
    public double getMaximumSpeed() {

        int level =
                getEngineTailwindLevel();

        return switch (level) {

            case 1 -> 0.62D;

            case 2 -> 0.72D;

            case 3 -> 0.84D;

            default -> BASE_ENGINE_SPEED;
        };
    }

    /*
     * =========================================================
     * INPUT
     * =========================================================
     *
     * Без топлива:
     * ванильная лодка получает обычные inputs.
     *
     * С топливом:
     * ванильные inputs очищаются полностью,
     * а A/D сохраняются отдельно для нашего двигателя.
     */
    @Override
    public void setInputs(
            boolean pressingLeft,
            boolean pressingRight,
            boolean pressingForward,
            boolean pressingBack
    ) {

        if (!hasFuel()) {

            /*
             * В обычном режиме всё ванильно.
             */
            enginePressingLeft = false;
            enginePressingRight = false;

            super.setInputs(
                    pressingLeft,
                    pressingRight,
                    pressingForward,
                    pressingBack
            );

            return;
        }

        /*
         * Очищаем ванильные inputs.
         *
         * Это очень важно:
         * ванильная лодка не должна одновременно
         * грести сама и работать двигателем.
         */
        super.setInputs(
                false,
                false,
                false,
                false
        );

        /*
         * Сохраняем только A/D
         * в собственные поля двигателя.
         */
        enginePressingLeft =
                pressingLeft;

        enginePressingRight =
                pressingRight;
    }

    public boolean isEnginePressingLeft() {
        return enginePressingLeft;
    }

    public boolean isEnginePressingRight() {
        return enginePressingRight;
    }

    /*
     * =========================================================
     * ДВИГАТЕЛЬ
     * =========================================================
     */
    public void applySelfPropulsion() {

        if (!hasFuel()) {
            return;
        }

        /*
         * На суше двигатель НЕ работает.
         */
        if (!isTouchingWater()) {
            return;
        }

        /*
         * A/D — управление направлением.
         */
        if (enginePressingLeft
                && !enginePressingRight) {

            setYaw(
                    getYaw()
                            - ENGINE_STEERING_SPEED
            );

        } else if (
                enginePressingRight
                        && !enginePressingLeft
        ) {

            setYaw(
                    getYaw()
                            + ENGINE_STEERING_SPEED
            );
        }

        Vec3d velocity =
                getVelocity();

        /*
         * Направление носа лодки.
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
         * Берём только скорость по направлению носа.
         *
         * Боковой glide уничтожается.
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
         * Плавный разгон.
         */
        double targetSpeed =
                forwardSpeed
                        + ENGINE_ACCELERATION;

        targetSpeed =
                Math.min(
                        maximumSpeed,
                        targetSpeed
                );

        /*
         * Минимальная скорость самого двигателя.
         */
        if (targetSpeed < BASE_ENGINE_SPEED) {

            targetSpeed =
                    Math.min(
                            BASE_ENGINE_SPEED,
                            maximumSpeed
                    );
        }

        /*
         * X/Z = двигатель.
         *
         * Y = ванильная физика.
         *
         * Это сохраняет:
         * - гравитацию;
         * - падение;
         * - плавучесть;
         * - переход вода/суша.
         */
        setVelocity(
                forward.x * targetSpeed,
                velocity.y,
                forward.z * targetSpeed
        );

        velocityDirty = true;
    }

    /*
     * =========================================================
     * ITEM
     * =========================================================
     */

    @Override
    public Item asItem() {
        return ModItems.SELF_PROPELLING_BOAT;
    }

    /*
     * =========================================================
     * INTERACTION
     * =========================================================
     */

    @Override
    public ActionResult interact(
            PlayerEntity player,
            Hand hand
    ) {

        /*
         * Shift + ПКМ снаружи → GUI.
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

        /*
         * Обычная посадка.
         */
        return super.interact(
                player,
                hand
        );
    }

    /*
     * =========================================================
     * TICK
     * =========================================================
     */

    @Override
    public void tick() {

        /*
         * Сервер:
         * топливо + синхронизация enchantment.
         */
        if (!getWorld().isClient()) {

            tickFuel();

            synchronizeTailwindLevel();
        }

        /*
         * Полностью ванильная BoatEntity физика.
         *
         * Двигатель вызывается отдельным Mixin
         * после updateVelocity().
         */
        super.tick();
    }

    /*
     * =========================================================
     * DROP
     * =========================================================
     */

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
         * entity просто удаляется.
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
                fuelInventory.removeStack(0);

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

        int tailwindLevel =
                getEngineTailwindLevel();

        /*
         * На всякий случай fallback
         * на attachment.
         */
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

    /*
     * =========================================================
     * NBT
     * =========================================================
     */

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
