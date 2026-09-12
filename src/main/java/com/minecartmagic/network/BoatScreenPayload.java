package com.minecartmagic.network;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record BoatScreenPayload() implements FabricPacket {

    public static final BoatScreenPayload INSTANCE = new BoatScreenPayload();

    public static final Identifier ID =
            new Identifier(
                    "minecartmagic",
                    "open_self_propelling_boat"
            );

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public void write(PacketByteBuf buf) {
    }
}
