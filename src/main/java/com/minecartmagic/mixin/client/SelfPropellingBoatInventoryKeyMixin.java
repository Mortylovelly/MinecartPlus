package com.minecartmagic.mixin.client;

import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class SelfPropellingBoatInventoryKeyMixin {

    @Inject(
            method = "handleInputEvents",
            at = @At("HEAD")
    )
    private void minecartmagic$openBoatMenuWithInventoryKey(
            CallbackInfo ci
    ) {
        MinecraftClient client =
                (MinecraftClient) (Object) this;

        /*
         * Нам нужен игрок и мир.
         */
        if (client.player == null
                || client.world == null) {
            return;
        }

        /*
         * Уже открыто какое-либо GUI —
         * ничего не делаем.
         */
        if (client.currentScreen != null) {
            return;
        }

        /*
         * Проверяем именно клавишу открытия инвентаря.
         *
         * По умолчанию это E, но если пользователь
         * переназначит Inventory, будет работать
         * уже назначенная клавиша.
         */
        if (!client.options.inventoryKey.wasPressed()) {
            return;
        }

        Entity vehicle =
                client.player.getVehicle();

        /*
         * Работаем только с самоходной лодкой.
         */
        if (!(vehicle instanceof SelfPropellingBoatEntity)) {
            return;
        }

        /*
         * Отправляем на сервер обычное взаимодействие
         * с сущностью через клиентский interaction manager.
         *
         * Это важно: именно так сервер узнает про
         * взаимодействие и вызывает SelfPropellingBoatEntity.interact().
         */
        if (client.interactionManager != null) {
            client.interactionManager.interactEntity(
                    client.player,
                    vehicle,
                    Hand.MAIN_HAND
            );
        }
    }
}
