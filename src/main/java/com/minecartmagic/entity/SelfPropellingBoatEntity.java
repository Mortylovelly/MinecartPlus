package com.minecartmagic.entity;

import com.minecartmagic.ModEnchantments;
import com.minecartmagic.ModItems;
import com.minecartmagic.screen.SelfPropellingBoatScreenHandler;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class SelfPropellingBoatEntity
        extends BoatEntity
        implements ExtendedScreenHandlerFactory<Integer> {

    private static final int MAX_PASSENGERS = 1;

    private final SimpleInventory fuelInventory =
            new SimpleInventory(1);

    /*
     * Remaining ticks of the currently burning fuel item.
     */
    private int burnTime;

    /*
     * Total burn ticks of the current fuel item.
     *
     * Used by the GUI to calculate the flame progress.
     */
    private int fuelTime;

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

    public SimpleInventory getFuelInventory() {
        return fuelInventory;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getFuelTime() {
        return fuelTime;
    }

    public boolean hasFuel() {
        return burnTime > 0;
    }

    private void startBurningFuel() {

        if (burnTime > 0) {
            return;
        }

        ItemStack stack =
                fuelInventory.getStack(0);

        if (stack.isEmpty()) {
            fuelTime = 0;
            return;
        }

        Integer fuelValue =
                FuelRegistry.INSTANCE.get(
                        stack.getItem()
                );

        if (fuelValue == null
                || fuelValue <= 0) {
            fuelTime = 0;
            return;
        }

        burnTime =
                fuelValue;

        fuelTime =
                fuelValue;

        /*
         * Consume one fuel item.
         */
        Item fuelItem =
                stack.getItem();

        stack.decrement(1);

        /*
         * Vanilla-style recipe remainder support.
         *
         * For example, lava bucket -> empty bucket.
         */
        if (stack.isEmpty()
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

        /*
         * Start another fuel item when the current one ended.
         */
        if (burnTime <= 0) {
            startBurningFuel();
        }

        /*
         * Burn one tick.
         */
        if (burnTime > 0) {
            burnTime--;
        }

        /*
         * If absolutely nothing is burning,
         * clear the display value.
         */
        if (burnTime <= 0) {
            burnTime = 0;
            fuelTime = 0;
        }
    }

    private double getTailwindMultiplier() {

        int level =
                ModEnchantments.getTailwindLevel(
                        this
                );

        return switch (level) {
            case 1 -> 1.08D;
            case 2 -> 1.14D;
            case 3 -> 1.20D;
            default -> 1.0D;
        };
    }

    public double getMaximumSpeed() {
        return 0.35D
                * getTailwindMultiplier();
    }

    private Vec3dWrapper getForwardDirection() {
        double radians =
                Math.toRadians(
                        getYaw()
                );

        return new Vec3dWrapper(
                -Math.sin(radians),
                Math.cos(radians)
        );
    }

    /*
     * Kept as a small internal value object so the movement
     * method doesn't have to construct a full Vec3d just
     * to obtain horizontal direction.
     */
    private record Vec3dWrapper(
            double x,
            double z
    ) {
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
         * A/D steer the boat only while the engine is running.
         */
        if (clientSide) {

            if (pressingLeft && !pressingRight) {

                setYaw(
                        getYaw() - 2.5F
                );

            } else if (pressingRight
                    && !pressingLeft) {

                setYaw(
                        getYaw() + 2.5F
                );
            }
        }

        if (!isTouchingWater()) {
            return;
        }

        Vec3dWrapper forward =
                getForwardDirection();

        var velocity =
                getVelocity();

        double horizontalSpeed =
                Math.sqrt(
                        velocity.x * velocity.x
                                + velocity.z * velocity.z
                );

        double maxSpeed =
                getMaximumSpeed();

        double targetSpeed =
                Math.min(
                        maxSpeed,
                        horizontalSpeed
                                + 0.025D
                );

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
         * Shift + right click from outside:
         *
         * open fuel GUI.
         */
        if (player.isSneaking()
                && !hasPassenger(player)) {

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
         * Everything else uses vanilla BoatEntity:
         *
         * mounting,
         * dismounting,
         * normal interaction.
         */
        return super.interact(
                player,
                hand
        );
    }

    private boolean hasPassenger(
            PlayerEntity player
    ) {
        return getPassengerList()
                .contains(
                        player
                );
    }

    @Override
    public void tick() {

        /*
         * Fuel must be processed BEFORE
         * BoatEntity physics.
         *
         * That way the engine sees the newly started
         * fuel item during this tick.
         */
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

        /*
         * Creative does not drop anything.
         */
        if (source.getAttacker()
                instanceof PlayerEntity player
                && player.getAbilities()
                .creativeMode) {

            discard();

            return true;
        }

        /*
         * Return fuel currently stored in the GUI.
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
         * Return the boat itself.
         *
         * Tailwind is preserved.
         */
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
                burnTime
        );

        nbt.putInt(
                "FuelTime",
                fuelTime
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

        burnTime =
                nbt.getInt(
                        "BurnTime"
                );

        fuelTime =
                nbt.getInt(
                        "FuelTime"
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
        return new SelfPropellingBoatScreenHandler(
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
