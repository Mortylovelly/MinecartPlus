package com.minecartmagic.network;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ModNetworking {

    private ModNetworking() {
    }

    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(
                BoatScreenPayload.TYPE,
                (server, player, handler, buf, responseSender) ->
                        server.execute(() -> {
                            if (!(player.getVehicle() instanceof SelfPropellingBoatEntity boat)) {
                                return;
                            }

                            if (boat.isRemoved()) {
                                return;
                            }

                            player.openHandledScreen(boat);
                        })
        );
    }
}
