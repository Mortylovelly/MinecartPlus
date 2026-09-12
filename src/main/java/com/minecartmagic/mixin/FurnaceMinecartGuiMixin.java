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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FurnaceMinecartEntity.class)
public abstract class FurnaceMinecartGuiMixin
        implements ExtendedScreenHandlerFactory, SelfPropellingMinecartAccess {

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

    @Unique
    private float minecartmagic$placementYaw;

    @Unique
    private boolean minecartmagic$placementYawInitialized;

    @Unique
    private boolean minecartmagic$engineDirectionInitialized;

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
            player.openHandledScreen(this);
        }

        // Opening the GUI must never change the cart's direction.
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

        if (!minecartmagic$placementYawInitialized) {
            minecartmagic$placementYaw = minecart.getYaw();
            minecartmagic$placementYawInitialized = true;
        }

        if (fuel <= 0) {
            minecartmagic$engineDirectionInitialized = false;
            minecartmagic$startFuelIfAvailable(minecart);
        }

        // Set the engine's initial direction once per burn cycle.
        // After this, vanilla FurnaceMinecartEntity controls pushX/pushZ.
        if (fuel > 0 && !minecartmagic$engineDirectionInitialized) {
            if (pushX * pushX + pushZ * pushZ < 1.0E-8D) {
                double radians = Math.toRadians(minecartmagic$placementYaw);
                pushX = -Math.sin(radians);
                pushZ = Math.cos(radians);
            }

            minecartmagic$engineDirectionInitialized = true;
        }
    }

    @Unique
    private void minecartmagic$startFuelIfAvailable(
            FurnaceMinecartEntity minecart
    ) {
        ItemStack fuelStack =
                minecartmagic$fuelInventory.getStack(0);

        if (fuelStack.isEmpty()) {
            minecartmagic$fuelTime = 0;
            return;
        }

        Integer fuelValue =
                FuelRegistry.INSTANCE.get(fuelStack.getItem());

        if (fuelValue == null || fuelValue <= 0) {
            minecartmagic$fuelTime = 0;
            return;
        }

        fuel = fuelValue;
        minecartmagic$fuelTime = fuelValue;

        Item fuelItem = fuelStack.getItem();
        fuelStack.decrement(1);

        if (fuelStack.isEmpty()
                && fuelItem.hasRecipeRemainder()) {
            Item remainder = fuelItem.getRecipeRemainder();

            if (remainder != null) {
                minecartmagic$fuelInventory.setStack(
                        0,
                        new ItemStack(remainder)
                );
            }
        }

        minecartmagic$fuelInventory.markDirty();
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(
                "container.minecartmagic.self_propelling_minecart"
        );
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
    public Integer getScreenOpeningData(
            ServerPlayerEntity player
    ) {
        return ((FurnaceMinecartEntity) (Object) this).getId();
    }

    @Inject(
            method = "writeCustomDataToNbt",
            at = @At("TAIL")
    )
    private void minecartmagic$writeGuiFuel(
            NbtCompound nbt,
            CallbackInfo ci
    ) {
        FurnaceMinecartEntity minecart =
                (FurnaceMinecartEntity) (Object) this;

        nbt.putInt(
                "MinecartMagicFuelTime",
                minecartmagic$fuelTime
        );

        nbt.put(
                "MinecartMagicFuelInventory",
                minecartmagic$fuelInventory.toNbtList(
                        minecart.getWorld().getWorld().getRegistryManager()
                )
        );
    }

    @Inject(
            method = "readCustomDataFromNbt",
            at = @At("TAIL")
    )
    private void minecartmagic$readGuiFuel(
            NbtCompound nbt,
            CallbackInfo ci
    ) {
        FurnaceMinecartEntity minecart =
                (FurnaceMinecartEntity) (Object) this;

        minecartmagic$fuelTime =
                nbt.getInt("MinecartMagicFuelTime");

        if (nbt.contains(
                "MinecartMagicFuelInventory",
                NbtElement.LIST_TYPE
        )) {
            minecartmagic$fuelInventory.readNbtList(
                    nbt.getList(
                            "MinecartMagicFuelInventory",
                            NbtElement.COMPOUND_TYPE
                    ),
                    minecart.getWorld().getWorld().getRegistryManager()
            );
        }
    }
}
