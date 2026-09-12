package com.minecartmagic.screen;

import com.minecartmagic.MinecartMagicMod;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModScreenHandlers {

    public static final ExtendedScreenHandlerType<SelfPropellingBoatScreenHandler>
            SELF_PROPELLING_BOAT =
            new ExtendedScreenHandlerType<>(
                    (syncId, playerInventory, buf) ->
                            new SelfPropellingBoatScreenHandler(
                                    syncId,
                                    playerInventory,
                                    buf.readInt()
                            )
            );

    public static final ExtendedScreenHandlerType<SelfPropellingMinecartScreenHandler>
            SELF_PROPELLING_MINECART =
            new ExtendedScreenHandlerType<>(
                    (syncId, playerInventory, buf) ->
                            new SelfPropellingMinecartScreenHandler(
                                    syncId,
                                    playerInventory,
                                    buf.readInt()
                            )
            );

    static {
        Registry.register(
                Registries.SCREEN_HANDLER,
                MinecartMagicMod.id("self_propelling_boat"),
                SELF_PROPELLING_BOAT
        );

        Registry.register(
                Registries.SCREEN_HANDLER,
                MinecartMagicMod.id("self_propelling_minecart"),
                SELF_PROPELLING_MINECART
        );
    }

    private ModScreenHandlers() {
    }

    public static void init() {
    }
}
