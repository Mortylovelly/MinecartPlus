package com.minecartmagic.screen;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
import com.minecartmagic.screen.ModScreenHandlers;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class SelfPropellingBoatScreenHandler
        extends ScreenHandler {

    public static final int FUEL_SLOT = 0;

    private final Inventory fuelInventory;
    private final PropertyDelegate propertyDelegate;

    private final SelfPropellingBoatEntity boat;

    private final int entityId;

    /*
     * Client constructor.
     *
     * Client does not have access to the server entity inventory
     * when the handler is initially created, therefore it uses
     * a temporary one which is automatically synchronized from server.
     */
    public SelfPropellingBoatScreenHandler(
            int syncId,
            PlayerInventory playerInventory,
            int entityId
    ) {
        this(
                syncId,
                playerInventory,
                new net.minecraft.inventory.SimpleInventory(1),
                new ArrayPropertyDelegate(2),
                null,
                entityId
        );
    }

    /*
     * Server constructor.
     */
    public SelfPropellingBoatScreenHandler(
            int syncId,
            PlayerInventory playerInventory,
            SelfPropellingBoatEntity boat
    ) {
        this(
                syncId,
                playerInventory,
                boat.getFuelInventory(),
                new PropertyDelegate() {

                    @Override
                    public int get(int index) {
                        return switch (index) {
                            case 0 -> boat.getBurnTime();
                            case 1 -> boat.getFuelTime();
                            default -> 0;
                        };
                    }

                    @Override
                    public void set(
                            int index,
                            int value
                    ) {
                        /*
                         * Client-side synchronization values are
                         * read-only for this use case.
                         */
                    }

                    @Override
                    public int size() {
                        return 2;
                    }
                },
                boat,
                boat.getId()
        );
    }

    private SelfPropellingBoatScreenHandler(
            int syncId,
            PlayerInventory playerInventory,
            Inventory fuelInventory,
            PropertyDelegate propertyDelegate,
            SelfPropellingBoatEntity boat,
            int entityId
    ) {
        super(
                ModScreenHandlers.SELF_PROPELLING_BOAT,
                syncId
        );

        this.fuelInventory =
                fuelInventory;

        this.propertyDelegate =
                propertyDelegate;

        this.boat =
                boat;

        this.entityId =
                entityId;

        checkSize(
                fuelInventory,
                1
        );

        /*
         * Самоходная лодка.
         *
         * Один-единственный топливный слот.
         */
        addSlot(
                new FuelSlot(
                        fuelInventory,
                        FUEL_SLOT,
                        56,
                        53
                )
        );

        /*
         * Player inventory.
         *
         * 27 основных слотов.
         */
        for (int row = 0; row < 3; row++) {

            for (int column = 0; column < 9; column++) {

                addSlot(
                        new Slot(
                                playerInventory,
                                column
                                        + row * 9
                                        + 9,
                                8 + column * 18,
                                84 + row * 18
                        )
                );
            }
        }

        /*
         * Hotbar.
         */
        for (int slot = 0; slot < 9; slot++) {

            addSlot(
                    new Slot(
                            playerInventory,
                            slot,
                            8 + slot * 18,
                            142
                    )
            );
        }

        /*
         * Burn progress:
         *
         * property 0 = current burn time
         * property 1 = total burn time
         */
        addProperties(
                propertyDelegate
        );
    }

    public int getBurnTime() {
        return propertyDelegate.get(0);
    }

    public int getFuelTime() {
        return propertyDelegate.get(1);
    }

    /*
     * Progress of the vanilla-style flame.
     *
     * Returns 0..13.
     */
    public int getFuelProgress() {

        int burnTime =
                getBurnTime();

        int fuelTime =
                getFuelTime();

        if (fuelTime <= 0 || burnTime <= 0) {
            return 0;
        }

        return Math.min(
                13,
                (burnTime * 13 + fuelTime - 1)
                        / fuelTime
        );
    }

    public int getEntityId() {
        return entityId;
    }

    @Override
    public boolean canUse(
            PlayerEntity player
    ) {
        /*
         * Client-side handler does not have the real entity.
         */
        if (boat == null) {
            return true;
        }

        if (boat.isRemoved()) {
            return false;
        }

        return player.squaredDistanceTo(
                boat
        ) <= 64.0D;
    }

    @Override
    public ItemStack quickMove(
            PlayerEntity player,
            int slotIndex
    ) {
        ItemStack newStack =
                ItemStack.EMPTY;

        if (slotIndex < 0
                || slotIndex >= slots.size()) {
            return newStack;
        }

        Slot slot =
                slots.get(slotIndex);

        if (!slot.hasStack()) {
            return newStack;
        }

        ItemStack originalStack =
                slot.getStack();

        newStack =
                originalStack.copy();

        /*
         * Fuel slot -> player inventory.
         */
        if (slotIndex == FUEL_SLOT) {

            if (!insertItem(
                    originalStack,
                    1,
                    37,
                    true
            )) {
                return ItemStack.EMPTY;
            }

            slot.onQuickTransfer(
                    originalStack,
                    newStack
            );

        } else {

            /*
             * Player inventory -> fuel slot.
             */
            if (FuelRegistry.INSTANCE.get(
                    originalStack.getItem()
            ) != null) {

                if (!insertItem(
                        originalStack,
                        FUEL_SLOT,
                        FUEL_SLOT + 1,
                        false
                )) {
                    return ItemStack.EMPTY;
                }

            } else {

                /*
                 * Main inventory -> hotbar
                 * or hotbar -> main inventory.
                 */
                if (slotIndex >= 1
                        && slotIndex < 28) {

                    if (!insertItem(
                            originalStack,
                            28,
                            37,
                            false
                    )) {
                        return ItemStack.EMPTY;
                    }

                } else if (slotIndex >= 28
                        && slotIndex < 37) {

                    if (!insertItem(
                            originalStack,
                            1,
                            28,
                            false
                    )) {
                        return ItemStack.EMPTY;
                    }

                } else {

                    return ItemStack.EMPTY;
                }
            }
        }

        if (originalStack.isEmpty()) {
            slot.setStack(
                    ItemStack.EMPTY
            );
        } else {
            slot.markDirty();
        }

        if (originalStack.getCount()
                == newStack.getCount()) {

            return ItemStack.EMPTY;
        }

        slot.onTakeItem(
                player,
                originalStack
        );

        return newStack;
    }

    @Override
    public void onClosed(
            PlayerEntity player
    ) {
        super.onClosed(player);

        fuelInventory.onClose(
                player
        );
    }

    /*
     * Special fuel-only slot.
     */
    private static class FuelSlot
            extends Slot {

        public FuelSlot(
                Inventory inventory,
                int index,
                int x,
                int y
        ) {
            super(
                    inventory,
                    index,
                    x,
                    y
            );
        }

        @Override
        public boolean canInsert(
                ItemStack stack
        ) {
            return FuelRegistry.INSTANCE.get(
                    stack.getItem()
            ) != null;
        }

        @Override
        public int getMaxItemCount() {
            return 64;
        }
    }
}
