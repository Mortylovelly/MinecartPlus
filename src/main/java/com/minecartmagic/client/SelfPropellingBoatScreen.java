package com.minecartmagic.client;

import com.minecartmagic.screen.SelfPropellingBoatScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SelfPropellingBoatScreen
        extends HandledScreen<SelfPropellingBoatScreenHandler> {

    private static final Identifier INVENTORY_TEXTURE =
            Identifier.ofVanilla(
                    "textures/gui/container/inventory.png"
            );

    private static final Identifier FURNACE_TEXTURE =
            Identifier.ofVanilla(
                    "textures/gui/container/furnace.png"
            );

    private static final int WIDTH = 176;

    private static final int HEIGHT = 166;

    /*
     * Верхняя панель самоходной лодки.
     */
    private static final int PANEL_HEIGHT = 76;

    /*
     * С этой координаты начинается vanilla
     * нижний inventory.
     */
    private static final int PLAYER_AREA_Y = 76;

    public SelfPropellingBoatScreen(
            SelfPropellingBoatScreenHandler handler,
            PlayerInventory inventory,
            Text title
    ) {

        super(
                handler,
                inventory,
                title
        );

        backgroundWidth =
                WIDTH;

        backgroundHeight =
                HEIGHT;

        /*
         * Название "Инвентарь" находится
         * непосредственно над первым рядом
         * player slots.
         */
        playerInventoryTitleX = 8;

        playerInventoryTitleY = 77;
    }

    @Override
    protected void drawBackground(
            DrawContext context,
            float delta,
            int mouseX,
            int mouseY
    ) {

        int x =
                (width - backgroundWidth)
                        / 2;

        int y =
                (height - backgroundHeight)
                        / 2;

        /*
         * =================================================
         * НИЖНИЙ VANILLA INVENTORY
         * =================================================
         *
         * Очень важный момент:
         *
         * inventory.png:
         *
         * source Y = 76
         *
         * Именно там начинается нижняя часть
         * обычного inventory GUI.
         *
         * Поэтому:
         * - skin не рисуем;
         * - 2x2 crafting не рисуем;
         * - player inventory выглядит ванильно.
         */
        context.drawTexture(
                INVENTORY_TEXTURE,
                x,
                y + PLAYER_AREA_Y,
                0,
                76,
                WIDTH,
                HEIGHT - PLAYER_AREA_Y,
                256,
                256
        );

        /*
         * =================================================
         * ПАНЕЛЬ САМОХОДНОЙ ЛОДКИ
         * =================================================
         */
        int panelX =
                x;

        int panelY =
                y;

        int panelBottom =
                panelY + PANEL_HEIGHT;

        /*
         * Чёрная рамка.
         */
        context.fill(
                panelX,
                panelY,
                panelX + WIDTH,
                panelBottom,
                0xFF373737
        );

        /*
         * Светлая внутренняя поверхность.
         */
        context.fill(
                panelX + 1,
                panelY + 1,
                panelX + WIDTH - 1,
                panelBottom - 1,
                0xFFC6C6C6
        );

        /*
         * Верхняя светлая линия.
         */
        context.fill(
                panelX + 2,
                panelY + 2,
                panelX + WIDTH - 2,
                panelY + 3,
                0xFFE8E8E8
        );

        /*
         * Нижняя тёмная линия.
         */
        context.fill(
                panelX + 2,
                panelBottom - 3,
                panelX + WIDTH - 2,
                panelBottom - 2,
                0xFF8A8A8A
        );

        /*
         * =================================================
         * ЕДИНСТВЕННЫЙ СЛОТ
         * =================================================
         */
        context.drawTexture(
                FURNACE_TEXTURE,
                panelX + 79,
                panelY + 30,
                56,
                53,
                18,
                18,
                256,
                256
        );

        /*
         * =================================================
         * ПЛАМЯ
         * =================================================
         */
        int flameHeight =
                handler.getFuelProgress();

        if (flameHeight > 0) {

            context.drawTexture(
                    FURNACE_TEXTURE,
                    panelX + 103,
                    panelY + 36
                            + (14 - flameHeight),
                    176,
                    14 - flameHeight,
                    14,
                    flameHeight,
                    256,
                    256
            );
        }

        /*
         * =================================================
         * ШКАЛА ТОПЛИВА
         * =================================================
         */
        int gaugeX =
                panelX + 126;

        int gaugeY =
                panelY + 12;

        int gaugeWidth =
                12;

        int gaugeHeight =
                42;

        /*
         * Рамка.
         */
        context.fill(
                gaugeX,
                gaugeY,
                gaugeX + gaugeWidth,
                gaugeY + gaugeHeight,
                0xFF3A3A3A
        );

        /*
         * Пустая область.
         */
        context.fill(
                gaugeX + 2,
                gaugeY + 2,
                gaugeX + gaugeWidth - 2,
                gaugeY + gaugeHeight - 2,
                0xFF555555
        );

        int percent =
                handler.getFuelPercent();

        int filledHeight =
                Math.round(
                        (gaugeHeight - 4)
                                * percent
                                / 100.0F
                );

        if (filledHeight > 0) {

            int top =
                    gaugeY
                            + gaugeHeight
                            - 2
                            - filledHeight;

            context.fill(
                    gaugeX + 3,
                    top,
                    gaugeX + gaugeWidth - 3,
                    gaugeY + gaugeHeight - 2,
                    0xFFE6A23C
            );

            /*
             * Блик.
             */
            context.fill(
                    gaugeX + 3,
                    top,
                    gaugeX + 4,
                    gaugeY + gaugeHeight - 2,
                    0xFFFFCF69
            );
        }
    }

    @Override
    protected void drawForeground(
            DrawContext context,
            int mouseX,
            int mouseY
    ) {

        /*
         * Заголовок.
         */
        context.drawText(
                textRenderer,
                title,
                8,
                6,
                0x404040,
                false
        );

        /*
         * "Топливо".
         */
        context.drawText(
                textRenderer,
                Text.translatable(
                        "container.minecartmagic.fuel"
                ),
                69,
                17,
                0x404040,
                false
        );

        /*
         * Количество топлива.
         */
        context.drawText(
                textRenderer,
                Text.translatable(
                        "container.minecartmagic.fuel_count",
                        handler.getFuelStackCount()
                ),
                8,
                43,
                0x404040,
                false
        );

        /*
         * Текущее время горения.
         */
        context.drawText(
                textRenderer,
                Text.translatable(
                        "container.minecartmagic.burn_time",
                        handler.getRemainingSeconds()
                ),
                8,
                54,
                0x404040,
                false
        );
    }
}
