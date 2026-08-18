package com.minecartmagic.network;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;

public final class ModNetworking {

    private ModNetworking() {
    }

    public static void init() {

        PayloadTypeRegistry.playC2S().register(
                BoatScreenPayload.ID,
                BoatScreenPayload.CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(
                BoatScreenPayload.ID,
                (payload, context) -> {

                    PlayerEntity player =
                            context.player();

                    context.server().execute(() -> {

                        if (!(player.getVehicle()
                                instanceof SelfPropellingBoatEntity boat)) {
                            return;
                        }

                        if (boat.isRemoved()) {
                            return;
                        }

                        player.openHandledScreen(
                                boat
                        );
                    });
                }
        );
    }
}
