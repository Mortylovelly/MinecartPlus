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
            at = @At("HEAD")
    )
    private void minecartmagic$openBoatScreen(
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

        /*
         * Только когда игрок уже сидит
         * в самоходной лодке.
         */
        if (!(client.player.getVehicle()
                instanceof SelfPropellingBoatEntity)) {
            return;
        }

        /*
         * Используется стандартная клавиша Inventory.
         *
         * По умолчанию это E.
         */
        if (!client.options.inventoryKey.wasPressed()) {
            return;
        }

        /*
         * Отправляем существующий пакет открытия меню.
         */
        ClientPlayNetworking.send(
                BoatScreenPayload.INSTANCE
        );
    }
}
