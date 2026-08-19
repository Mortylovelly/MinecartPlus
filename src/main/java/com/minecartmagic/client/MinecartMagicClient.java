package com.minecartmagic.client;

import com.minecartmagic.ModEntities;
import com.minecartmagic.entity.SelfPropellingBoatEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;

public class MinecartMagicClient
        implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        /*
         * Renderer самоходной лодки.
         */
        EntityRendererRegistry.register(
                ModEntities.SELF_PROPELLING_BOAT,
                SelfPropellingBoatRenderer::new
        );

        /*
         * Клавиша Inventory.
         *
         * По умолчанию это E.
         *
         * Важно:
         * мы НЕ создаём новую клавишу.
         *
         * Если игрок переназначит Inventory,
         * самоходная лодка тоже будет использовать
         * назначенную ему клавишу.
         */
        ClientTickEvents.START_CLIENT_TICK.register(
                client -> {

                    if (client.player == null
                            || client.world == null) {
                        return;
                    }

                    /*
                     * GUI уже открыт — ничего не делаем.
                     */
                    if (client.currentScreen != null) {
                        return;
                    }

                    /*
                     * Игрок должен находиться именно
                     * в самоходной лодке.
                     */
                    if (!(client.player.getVehicle()
                            instanceof SelfPropellingBoatEntity selfPropellingBoat)) {
                        return;
                    }

                    /*
                     * Читаем стандартную кнопку Inventory.
                     *
                     * По умолчанию:
                     * E
                     */
                    while (
                            client.options.inventoryKey.wasPressed()
                    ) {

                        if (client.interactionManager == null) {
                            return;
                        }

                        /*
                         * Отправляем обычное interaction
                         * на нашу лодку.
                         *
                         * Сервер попадёт в
                         * SelfPropellingBoatInteractionMixin
                         * и откроет существующий ScreenHandler.
                         */
                        client.interactionManager.interactEntity(
                                client.player,
                                selfPropellingBoat,
                                Hand.MAIN_HAND
                        );
                    }
                }
        );
    }
}
