package com.minecartmagic.screen;

import com.minecartmagic.mixin.SelfPropellingMinecartAccess;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class SelfPropellingMinecartScreenHandler extends ScreenHandler {

    public static final int FUEL_SLOT = 0;

    private static final int FUEL_SLOT_X = 79;
    private static final int FUEL_SLOT_Y = 36;

    private static final int PLAYER_INVENTORY_Y = 84;
    private static final int HOTBAR_Y = 142;

    private final Inventory fuelInventory;
    private final PropertyDelegate propertyDelegate;
    private final FurnaceMinecartEntity minecart;

    public SelfPropellingMinecartScreenHandler(
            int syncId,
            PlayerInventory playerInventory,
            int entityId
    ) {
        this(
                syncId,
                playerInventory,
                new net.minecraft.inventory.SimpleInventory(1),
                new ArrayPropertyDelegate(2),
                null
        );
    }

    public SelfPropellingMinecartScreenHandler(
            int syncId,
            PlayerInventory playerInventory,
            FurnaceMinecartEntity minecart
    ) {
        this(
                syncId,
                playerInventory,
                ((SelfPropellingMinecartAccess) minecart)
                        .minecartmagic$getFuelInventory(),
                new MinecartPropertyDelegate(minecart),
                minecart
        );
    }

    private SelfPropellingMinecartScreenHandler(
            int syncId,
            PlayerInventory playerInventory,
            Inventory fuelInventory,
            PropertyDelegate propertyDelegate,
            FurnaceMinecartEntity minecart
    ) {
        super(
                ModScreenHandlers.SELF_PROPELLING_MINECART,
                syncId
        );

        this.fuelInventory = fuelInventory;
        this.propertyDelegate = propertyDelegate;
        this.minecart = minecart;

        checkSize(fuelInventory, 1);

        addSlot(
                new FuelSlot(
                        fuelInventory,
                        FUEL_SLOT,
                        FUEL_SLOT_X,
                        FUEL_SLOT_Y
                )
        );

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(
                        new Slot(
                                playerInventory,
                                column + row * 9 + 9,
                                8 + column * 18,
                                PLAYER_INVENTORY_Y + row * 18
                        )
                );
            }
        }

        for (int slot = 0; slot < 9; slot++) {
            addSlot(
                    new Slot(
                            playerInventory,
                            slot,
                            8 + slot * 18,
                            HOTBAR_Y
                    )
            );
        }

        addProperties(propertyDelegate);
    }

    public int getBurnTime() {
        return propertyDelegate.get(0);
    }

    public int getFuelTime() {
        return propertyDelegate.get(1);
    }

    public int getFuelProgress() {
        int burnTime = getBurnTime();
        int fuelTime = getFuelTime();

        if (burnTime <= 0 || fuelTime <= 0) {
            return 0;
        }

        return Math.min(
                13,
                (burnTime * 13 + fuelTime - 1) / fuelTime
        );
    }

    public int getFuelPercent() {
        int burnTime = getBurnTime();
        int fuelTime = getFuelTime();

        if (burnTime <= 0 || fuelTime <= 0) {
            return 0;
        }

        return Math.min(
                100,
                Math.max(0, burnTime * 100 / fuelTime)
        );
    }

    public int getRemainingSeconds() {
        int burnTime = getBurnTime();

        if (burnTime <= 0) {
            return 0;
        }

        return (burnTime + 19) / 20;
    }

    public int getFuelStackCount() {
        return fuelInventory.getStack(FUEL_SLOT).getCount();
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        if (minecart == null) {
            return true;
        }

        if (minecart.isRemoved()) {
            return false;
        }

        return player.squaredDistanceTo(minecart) <= 64.0D;
    }

    @Override
    public ItemStack quickMove(
            PlayerEntity player,
            int slotIndex
    ) {
        if (slotIndex < 0 || slotIndex >= slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = slots.get(slotIndex);

        if (!slot.hasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack original = slot.getStack();
        ItemStack copied = original.copy();

        if (slotIndex == FUEL_SLOT) {
            if (!insertItem(original, 1, 37, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            Integer fuelValue =
                    FuelRegistry.INSTANCE.get(original.getItem());

            if (fuelValue != null && fuelValue > 0) {
                if (!insertItem(original, FUEL_SLOT, FUEL_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (slotIndex >= 1 && slotIndex < 28) {
                    if (!insertItem(original, 28, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotIndex >= 28 && slotIndex < 37) {
                    if (!insertItem(original, 1, 28, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (original.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        if (original.getCount() == copied.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTakeItem(player, original);
        return copied;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        fuelInventory.onClose(player);
    }

    private static class FuelSlot extends Slot {
        public FuelSlot(
                Inventory inventory,
                int index,
                int x,
                int y
        ) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            Integer fuelValue = FuelRegistry.INSTANCE.get(stack.getItem());
            return fuelValue != null && fuelValue > 0;
        }

        @Override
        public int getMaxItemCount() {
            return 64;
        }
    }

    private static class MinecartPropertyDelegate implements PropertyDelegate {
        private final FurnaceMinecartEntity minecart;

        private MinecartPropertyDelegate(FurnaceMinecartEntity minecart) {
            this.minecart = minecart;
        }

        @Override
        public int get(int index) {
            SelfPropellingMinecartAccess access =
                    (SelfPropellingMinecartAccess) minecart;

            return switch (index) {
                case 0 -> access.minecartmagic$getBurnTime();
                case 1 -> access.minecartmagic$getFuelTime();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int size() {
            return 2;
        }
    }
}
