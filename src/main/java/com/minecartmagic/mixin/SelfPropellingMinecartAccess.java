package com.minecartmagic.mixin;

import net.minecraft.inventory.SimpleInventory;

public interface SelfPropellingMinecartAccess {

    SimpleInventory minecartmagic$getFuelInventory();

    int minecartmagic$getBurnTime();

    int minecartmagic$getFuelTime();
}
