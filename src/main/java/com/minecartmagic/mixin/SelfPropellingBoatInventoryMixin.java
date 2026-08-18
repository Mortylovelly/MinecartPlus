package com.minecartmagic.mixin;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
import com.minecartmagic.network.BoatScreenPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class SelfPropellingBoatInventoryMixin {

    @Inject(
            method = "handleInputEvents",
            at = @At("HEAD"),
            cancellable = true
    )
    private void minecartmagic$openBoatInventory(
            CallbackInfo ci
    ) {
        MinecraftClient client =
                (MinecraftClient) (Object) this;

        if (client.currentScreen != null) {
            return;
        }

        if (client.player == null) {
            return;
        }

        if (client.world == null) {
            return;
        }

        if (!(client.player.getVehicle()
                instanceof SelfPropellingBoatEntity)) {
            return;
        }

        if (!client.options.inventoryKey.wasPressed()) {
            return;
        }

        /*
         * Tell the server:
         *
         * "I'm inside a self-propelled boat,
         * open its fuel GUI."
         */
        ClientPlayNetworking.send(
                BoatScreenPayload.INSTANCE
        );

        /*
         * Prevent vanilla E from opening
         * the normal player inventory.
         */
        ci.cancel();
    }
}
