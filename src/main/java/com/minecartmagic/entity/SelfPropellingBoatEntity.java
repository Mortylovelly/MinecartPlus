package com.minecartmagic.entity;

import com.minecartmagic.ModEnchantments;
import com.minecartmagic.ModItems;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SelfPropellingBoatEntity extends BoatEntity {

    private static final int FUEL_PER_COAL = 3600;
    private static final int MAX_FUEL = 7200;

    private static final double BASE_MAX_SPEED = 0.42D;
    private static final double ACCELERATION = 0.018D;

    public static final TrackedData<Integer> FUEL =
            DataTracker.registerData(
                    SelfPropellingBoatEntity.class,
                    TrackedDataHandlerRegistry.INTEGER
            );

    public static final TrackedData<Boolean> LIT =
            DataTracker.registerData(
                    SelfPropellingBoatEntity.class,
                    TrackedDataHandlerRegistry.BOOLEAN
            );

    private Vec3d pushDirection = Vec3d.ZERO;

    public SelfPropellingBoatEntity(
            EntityType<? extends BoatEntity> entityType,
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

        builder.add(FUEL, 0);
        builder.add(LIT, false);
    }

    @Override
    protected Item asItem() {
        return ModItems.SELF_PROPELLING_BOAT;
    }

    public int getFuel() {
        return getDataTracker().get(FUEL);
    }

    public boolean isLit() {
        return getDataTracker().get(LIT);
    }

    private void setFuel(int fuel) {
        int safeFuel = Math.max(
                0,
                Math.min(MAX_FUEL, fuel)
        );

        getDataTracker().set(
                FUEL,
                safeFuel
        );

        getDataTracker().set(
                LIT,
                safeFuel > 0
        );
    }

    private boolean isFuel(ItemStack stack) {
        return stack.isOf(net.minecraft.item.Items.COAL)
                || stack.isOf(net.minecraft.item.Items.CHARCOAL);
    }

    @Override
    public ActionResult interact(
            PlayerEntity player,
            Hand hand
    ) {
        ItemStack stack =
                player.getStackInHand(hand);

        if (isFuel(stack)) {

            if (!getEntityWorld().isClient()) {

                int oldFuel = getFuel();

                if (oldFuel < MAX_FUEL) {

                    setFuel(
                            oldFuel + FUEL_PER_COAL
                    );

                    Vec3d direction =
                            player.getRotationVec(1.0F);

                    Vec3d horizontal =
                            new Vec3d(
                                    direction.x,
                                    0.0D,
                                    direction.z
                            );

                    if (horizontal.lengthSquared()
                            > 0.000001D) {

                        pushDirection =
                                horizontal.normalize();
                    }

                    if (!player.isCreative()) {
                        stack.decrement(1);
                    }
                }
            }

            return ActionResult.success(
                    getEntityWorld().isClient()
            );
        }

        return super.interact(
                player,
                hand
        );
    }

    @Override
    public void tick() {
        super.tick();

        if (getEntityWorld().isClient()) {
            return;
        }

        int fuel = getFuel();

        if (fuel <= 0) {
            setFuel(0);
            return;
        }

        /*
         * Топливо расходуется только когда лодка
         * находится в воде.
         */
        if (!isTouchingWater()) {
            return;
        }

        if (pushDirection.lengthSquared()
                <= 0.000001D) {

            return;
        }

        setFuel(fuel - 1);

        applySelfPropulsion();
    }

    private void applySelfPropulsion() {

        Vec3d direction =
                pushDirection.normalize();

        int tailwindLevel =
                ModEnchantments.getTailwindLevel(this);

        double maxSpeed =
                BASE_MAX_SPEED;

        if (tailwindLevel > 0) {

            maxSpeed = switch (tailwindLevel) {
                case 1 -> 0.50D;
                case 2 -> 0.58D;
                default -> 0.68D;
            };
        }

        Vec3d velocity =
                getVelocity();

        Vec3d horizontalVelocity =
                new Vec3d(
                        velocity.x,
                        0.0D,
                        velocity.z
                );

        double forwardSpeed =
                horizontalVelocity.dotProduct(
                        direction
                );

        double newForwardSpeed =
                Math.min(
                        maxSpeed,
                        Math.max(
                                0.0D,
                                forwardSpeed
                        ) + ACCELERATION
                );

        Vec3d sideways =
                horizontalVelocity.subtract(
                        direction.multiply(
                                forwardSpeed
                        )
                );

        /*
         * Постепенно убираем боковой снос,
         * чтобы лодка двигалась именно в заданном
         * направлении, как печная вагонетка.
         */
        sideways =
                sideways.multiply(0.75D);

        Vec3d newHorizontalVelocity =
                sideways.add(
                        direction.multiply(
                                newForwardSpeed
                        )
                );

        setVelocity(
                newHorizontalVelocity.x,
                velocity.y,
                newHorizontalVelocity.z
        );

        /*
         * Поворачиваем корпус в направлении движения.
         */
        float targetYaw =
                (float) (
                        Math.atan2(
                                -direction.x,
                                direction.z
                        )
                                * (180.0D / Math.PI)
                );

        setYaw(targetYaw);
    }

    @Override
    public void killAndDropItem(
            Item selfAsItem
    ) {
        if (getEntityWorld().isClient()) {
            return;
        }

        ItemStack stack =
                new ItemStack(
                        ModItems.SELF_PROPELLING_BOAT
                );

        int tailwindLevel =
                ModEnchantments.getTailwindLevel(this);

        if (tailwindLevel > 0) {

            Registry<Enchantment> enchantmentRegistry =
                    getRegistryManager().get(
                            RegistryKeys.ENCHANTMENT
                    );

            RegistryEntry<Enchantment> tailwind =
                    enchantmentRegistry
                            .getEntry(
                                    ModEnchantments.TAILWIND_KEY
                            )
                            .orElse(null);

            if (tailwind != null) {
                stack.addEnchantment(
                        tailwind,
                        tailwindLevel
                );
            }
        }

        dropStack(stack);

        discard();
    }

    @Override
    protected void writeCustomDataToNbt(
            net.minecraft.nbt.NbtCompound nbt
    ) {
        super.writeCustomDataToNbt(nbt);

        nbt.putInt(
                "Fuel",
                getFuel()
        );

        nbt.putDouble(
                "PushX",
                pushDirection.x
        );

        nbt.putDouble(
                "PushY",
                pushDirection.y
        );

        nbt.putDouble(
                "PushZ",
                pushDirection.z
        );
    }

    @Override
    protected void readCustomDataFromNbt(
            net.minecraft.nbt.NbtCompound nbt
    ) {
        super.readCustomDataFromNbt(nbt);

        setFuel(
                nbt.getInt("Fuel")
        );

        pushDirection =
                new Vec3d(
                        nbt.getDouble("PushX"),
                        nbt.getDouble("PushY"),
                        nbt.getDouble("PushZ")
                );

        if (pushDirection.lengthSquared()
                > 0.000001D) {

            pushDirection =
                    new Vec3d(
                            pushDirection.x,
                            0.0D,
                            pushDirection.z
                    ).normalize();
        }
    }
}
