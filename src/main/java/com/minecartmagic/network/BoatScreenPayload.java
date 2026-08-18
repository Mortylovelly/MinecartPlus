package com.minecartmagic.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record BoatScreenPayload()
        implements CustomPayload {

    public static final BoatScreenPayload INSTANCE =
            new BoatScreenPayload();

    public static final CustomPayload.Id<
            BoatScreenPayload
            > ID =
            new CustomPayload.Id<>(
                    Identifier.of(
                            "minecartmagic",
                            "open_self_propelling_boat"
                    )
            );

    public static final PacketCodec<
            RegistryByteBuf,
            BoatScreenPayload
            > CODEC =
            PacketCodec.unit(
                    INSTANCE
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
