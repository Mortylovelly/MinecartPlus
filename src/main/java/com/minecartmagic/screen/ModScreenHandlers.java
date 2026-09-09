package com.minecartmagic.screen;

import com.minecartmagic.MinecartMagicMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.network.codec.PacketCodecs;

public final class ModScreenHandlers {

    public static final ExtendedScreenHandlerType<
            SelfPropellingBoatScreenHandler,
            Integer
            > SELF_PROPELLING_BOAT =
            new ExtendedScreenHandlerType<>(
                    (
                            syncId,
                            playerInventory,
                            entityId
                    ) ->
                            new SelfPropellingBoatScreenHandler(
                                    syncId,
                                    playerInventory,
                                    entityId
                            ),
                    PacketCodecs.VAR_INT
            );

    public static final ExtendedScreenHandlerType<
            SelfPropellingMinecartScreenHandler,
            Integer
            > SELF_PROPELLING_MINECART =
            new ExtendedScreenHandlerType<>(
                    (
                            syncId,
                            playerInventory,
                            entityId
                    ) ->
                            new SelfPropellingMinecartScreenHandler(
                                    syncId,
                                    playerInventory,
                                    entityId
                            ),
                    PacketCodecs.VAR_INT
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
