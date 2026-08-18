package com.minecartmagic.screen;

import com.minecartmagic.MinecartMagicMod;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

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

    static {
        Registry.register(
                Registries.SCREEN_HANDLER,
                MinecartMagicMod.id(
                        "self_propelling_boat"
                ),
                SELF_PROPELLING_BOAT
        );
    }

    private ModScreenHandlers() {
    }

    public static void init() {
    }
}
