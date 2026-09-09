package com.minecartmagic.mixin;

import com.minecartmagic.screen.SelfPropellingMinecartAccess;
import com.minecartmagic.screen.SelfPropellingMinecartScreenHandler;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FurnaceMinecartEntity.class)
public abstract class FurnaceMinecartGuiMixin
        implements ExtendedScreenHandlerFactory<Integer>, SelfPropellingMinecartAccess {

    @Shadow
    private int fuel;

    @Shadow
    public double pushX;

    @Shadow
    public double pushZ;

    @Unique
    private int minecartmagic$fuelTime;

    @Unique
    private final SimpleInventory minecartmagic$fuelInventory =
            new SimpleInventory(1);

    @Override
    public SimpleInventory minecartmagic$getFuelInventory() {
        return minecartmagic$fuelInventory;
    }

    @Override
    public int minecartmagic$getBurnTime() {
        return Math.max(0, fuel);
    }

    @Override
    public int minecartmagic$getFuelTime() {
        return Math.max(0, minecartmagic$fuelTime);
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void minecartmagic$openEngineGui(
            PlayerEntity player,
            Hand hand,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        FurnaceMinecartEntity minecart =
                (FurnaceMinecartEntity) (Object) this;

        if (!minecart.getWorld().isClient()) {
            // Preserve the important part of vanilla furnace-minecart
            // interaction: the fuel determines the direction away from
            // the player who activates the engine.
            double directionX = minecart.getX() - player.getX();
            double directionZ = minecart.getZ() - player.getZ();
            double length = Math.sqrt(directionX * directionX + directionZ * directionZ);

            if (length > 1.0E-6D) {
                pushX = directionX / length;
                pushZ = directionZ / length;
            }

            player.openHandledScreen(this);
        }

        cir.setReturnValue(
                ActionResult.success(minecart.getWorld().isClient())
        );
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void minecartmagic$prepareEngine(CallbackInfo ci) {
        FurnaceMinecartEntity minecart =
                (FurnaceMinecartEntity) (Object) this;

        if (minecart.getWorld().isClient()) {
            return;
        }

        if (fuel <= 0) {
            minecartmagic$startFuelIfAvailable(minecart);
        }

        if (fuel <= 0) {
            pushX = 0.0D;
            pushZ = 0.0D;

            Vec3d velocity = minecart.getVelocity();
            if (velocity.x != 0.0D || velocity.z != 0.0D) {
                minecart.setVelocity(0.0D, velocity.y, 0.0D);
                minecart.velocityDirty = true;
            }
        } else if (pushX * pushX + pushZ * pushZ < 1.0E-8D) {
            // Fallback for a cart that was fueled through another path and
            // therefore has no stored furnace direction yet.
            float yaw = minecart.getYaw();
            double radians = Math.toRadians(yaw);
            pushX = -Math.sin(radians);
            pushZ = Math.cos(radians);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void minecartmagic$stopWhenFuelAndReserveAreEmpty(CallbackInfo ci) {
        FurnaceMinecartEntity minecart =
                (FurnaceMinecartEntity) (Object) this;

        if (minecart.getWorld().isClient() || fuel > 0) {
            return;
        }

        ItemStack reserve = minecartmagic$fuelInventory.getStack(0);
        Integer reserveValue = reserve.isEmpty()
                ? null
                : FuelRegistry.INSTANCE.get(reserve.getItem());

        if (reserveValue == null || reserveValue <= 0) {
            pushX = 0.0D;
            pushZ = 0.0D;

            Vec3d velocity = minecart.getVelocity();
            minecart.setVelocity(0.0D, velocity.y, 0.0D);
            minecart.velocityDirty = true;
        }
    }

    @Unique
    private void minecartmagic$startFuelIfAvailable(FurnaceMinecartEntity minecart) {
        ItemStack fuelStack = minecartmagic$fuelInventory.getStack(0);

        if (fuelStack.isEmpty()) {
            minecartmagic$fuelTime = 0;
            return;
        }

        Integer fuelValue = FuelRegistry.INSTANCE.get(fuelStack.getItem());
        if (fuelValue == null || fuelValue <= 0) {
            minecartmagic$fuelTime = 0;
            return;
        }

        fuel = fuelValue;
        minecartmagic$fuelTime = fuelValue;

        Item fuelItem = fuelStack.getItem();
        fuelStack.decrement(1);

        if (fuelStack.isEmpty() && fuelItem.hasRecipeRemainder()) {
            Item remainder = fuelItem.getRecipeRemainder();
            if (remainder != null) {
                minecartmagic$fuelInventory.setStack(0, new ItemStack(remainder));
            }
        }

        minecartmagic$fuelInventory.markDirty();
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("container.minecartmagic.self_propelling_minecart");
    }

    @Override
    public ScreenHandler createMenu(
            int syncId,
            PlayerInventory playerInventory,
            PlayerEntity player
    ) {
        return new SelfPropellingMinecartScreenHandler(
                syncId,
                playerInventory,
                (FurnaceMinecartEntity) (Object) this
        );
    }

    @Override
    public Integer getScreenOpeningData(ServerPlayerEntity player) {
        return ((FurnaceMinecartEntity) (Object) this).getId();
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void minecartmagic$writeGuiFuel(NbtCompound nbt, CallbackInfo ci) {
        FurnaceMinecartEntity minecart =
                (FurnaceMinecartEntity) (Object) this;

        nbt.putInt("MinecartMagicFuelTime", minecartmagic$fuelTime);
        nbt.put(
                "MinecartMagicFuelInventory",
                minecartmagic$fuelInventory.toNbtList(minecart.getRegistryManager())
        );
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void minecartmagic$readGuiFuel(NbtCompound nbt, CallbackInfo ci) {
        FurnaceMinecartEntity minecart =
                (FurnaceMinecartEntity) (Object) this;

        minecartmagic$fuelTime = nbt.getInt("MinecartMagicFuelTime");

        if (nbt.contains("MinecartMagicFuelInventory", NbtElement.LIST_TYPE)) {
            minecartmagic$fuelInventory.readNbtList(
                    nbt.getList("MinecartMagicFuelInventory", NbtElement.COMPOUND_TYPE),
                    minecart.getRegistryManager()
            );
        }
    }
}
