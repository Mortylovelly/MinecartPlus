package com.minecartmagic.network;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record BoatScreenPayload() implements FabricPacket {

    public static final Identifier ID =
            new Identifier(
                    "minecartmagic",
                    "open_self_propelling_boat"
            );

    public static final PacketType<BoatScreenPayload> TYPE =
            PacketType.create(
                    ID,
                    BoatScreenPayload::new
            );

    public static final BoatScreenPayload INSTANCE =
            new BoatScreenPayload();

    public BoatScreenPayload(PacketByteBuf buf) {
        this();
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    @Override
    public void write(PacketByteBuf buf) {
    }
}
