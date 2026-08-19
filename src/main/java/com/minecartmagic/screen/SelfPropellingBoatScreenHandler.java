package com.minecartmagic.screen;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
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

    /*
     * =====================================================
     * СЛОТ ТОПЛИВА
     * =====================================================
     *
     * Screen рисует настоящий слот на:
     *
     * X = 79
     * Y = 30
     *
     * Поэтому хитбокс должен иметь ровно те же координаты.
     */
    private static final int FUEL_SLOT_X = 79;
    private static final int FUEL_SLOT_Y = 30;

    /*
     * =====================================================
     * ИНВЕНТАРЬ ИГРОКА
     * =====================================================
     */
    private static final int PLAYER_INVENTORY_Y = 84;

    private static final int HOTBAR_Y = 142;

    private final Inventory fuelInventory;

    private final PropertyDelegate propertyDelegate;

    private final SelfPropellingBoatEntity boat;

    /*
     * =====================================================
     * CLIENT CONSTRUCTOR
     * =====================================================
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
                null
        );
    }

    /*
     * =====================================================
     * SERVER CONSTRUCTOR
     * =====================================================
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
                new BoatPropertyDelegate(boat),
                boat
        );
    }

    private SelfPropellingBoatScreenHandler(
            int syncId,
            PlayerInventory playerInventory,
            Inventory fuelInventory,
            PropertyDelegate propertyDelegate,
            SelfPropellingBoatEntity boat
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

        checkSize(
                fuelInventory,
                1
        );

        /*
         * =================================================
         * ТОПЛИВНЫЙ SLOT
         * =================================================
         */
        addSlot(
                new FuelSlot(
                        fuelInventory,
                        FUEL_SLOT,
                        FUEL_SLOT_X,
                        FUEL_SLOT_Y
                )
        );

        /*
         * =================================================
         * 3 РЯДА ИНВЕНТАРЯ ИГРОКА
         * =================================================
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
                                PLAYER_INVENTORY_Y
                                        + row * 18
                        )
                );
            }
        }

        /*
         * =================================================
         * HOTBAR
         * =================================================
         */
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

    public int getFuelProgress() {

        int burnTime =
                getBurnTime();

        int fuelTime =
                getFuelTime();

        if (burnTime <= 0
                || fuelTime <= 0) {

            return 0;
        }

        return Math.min(
                13,
                (burnTime * 13
                        + fuelTime - 1)
                        / fuelTime
        );
    }

    public int getFuelPercent() {

        int burnTime =
                getBurnTime();

        int fuelTime =
                getFuelTime();

        if (burnTime <= 0
                || fuelTime <= 0) {

            return 0;
        }

        return Math.min(
                100,
                Math.max(
                        0,
                        burnTime * 100
                                / fuelTime
                )
        );
    }

    public int getRemainingSeconds() {

        int burnTime =
                getBurnTime();

        if (burnTime <= 0) {

            return 0;
        }

        return (burnTime + 19) / 20;
    }

    public int getFuelStackCount() {

        return fuelInventory
                .getStack(
                        FUEL_SLOT
                )
                .getCount();
    }

    @Override
    public boolean canUse(
            PlayerEntity player
    ) {

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

    /*
     * =====================================================
     * SHIFT-CLICK
     * =====================================================
     */
    @Override
    public ItemStack quickMove(
            PlayerEntity player,
            int slotIndex
    ) {

        if (slotIndex < 0
                || slotIndex >= slots.size()) {

            return ItemStack.EMPTY;
        }

        Slot slot =
                slots.get(
                        slotIndex
                );

        if (!slot.hasStack()) {

            return ItemStack.EMPTY;
        }

        ItemStack original =
                slot.getStack();

        ItemStack copied =
                original.copy();

        /*
         * =================================================
         * ТОПЛИВО -> ИНВЕНТАРЬ
         * =================================================
         */
        if (slotIndex == FUEL_SLOT) {

            if (!insertItem(
                    original,
                    1,
                    37,
                    true
            )) {

                return ItemStack.EMPTY;
            }

        } else {

            Integer fuelValue =
                    FuelRegistry.INSTANCE.get(
                            original.getItem()
                    );

            /*
             * =================================================
             * ИНВЕНТАРЬ -> ТОПЛИВО
             * =================================================
             */
            if (fuelValue != null
                    && fuelValue > 0) {

                if (!insertItem(
                        original,
                        FUEL_SLOT,
                        FUEL_SLOT + 1,
                        false
                )) {

                    return ItemStack.EMPTY;
                }

            } else {

                /*
                 * =================================================
                 * MAIN INVENTORY -> HOTBAR
                 * =================================================
                 */
                if (slotIndex >= 1
                        && slotIndex < 28) {

                    if (!insertItem(
                            original,
                            28,
                            37,
                            false
                    )) {

                        return ItemStack.EMPTY;
                    }

                } else if (
                        slotIndex >= 28
                                && slotIndex < 37
                ) {

                    /*
                     * HOTBAR -> MAIN INVENTORY
                     */
                    if (!insertItem(
                            original,
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

        if (original.isEmpty()) {

            slot.setStack(
                    ItemStack.EMPTY
            );

        } else {

            slot.markDirty();
        }

        if (original.getCount()
                == copied.getCount()) {

            return ItemStack.EMPTY;
        }

        slot.onTakeItem(
                player,
                original
        );

        return copied;
    }

    @Override
    public void onClosed(
            PlayerEntity player
    ) {

        super.onClosed(
                player
        );

        fuelInventory.onClose(
                player
        );
    }

    /*
     * =====================================================
     * FUEL SLOT
     * =====================================================
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

            Integer fuelValue =
                    FuelRegistry.INSTANCE.get(
                            stack.getItem()
                    );

            return fuelValue != null
                    && fuelValue > 0;
        }

        @Override
        public int getMaxItemCount() {

            return 64;
        }
    }

    /*
     * =====================================================
     * PROPERTY DELEGATE
     * =====================================================
     */
    private static class BoatPropertyDelegate
            implements PropertyDelegate {

        private final SelfPropellingBoatEntity boat;

        private BoatPropertyDelegate(
                SelfPropellingBoatEntity boat
        ) {

            this.boat =
                    boat;
        }

        @Override
        public int get(
                int index
        ) {

            return switch (index) {

                case 0 ->
                        boat.getBurnTime();

                case 1 ->
                        boat.getFuelTime();

                default ->
                        0;
            };
        }

        @Override
        public void set(
                int index,
                int value
        ) {
        }

        @Override
        public int size() {

            return 2;
        }
    }
}
