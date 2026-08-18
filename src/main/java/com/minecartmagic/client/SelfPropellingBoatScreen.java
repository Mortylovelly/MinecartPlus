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
     * Наше меню стало выше.
     *
     * Это НЕ двигает слоты игрока.
     * Оно только закрывает несколько пикселей
     * старого верхнего фона.
     */
    private static final int TOP_HEIGHT = 82;

    /*
     * Реальная верхняя граница панели
     * самоходной лодки.
     */
    private static final int PANEL_Y = 6;

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
         * Не меняем стандартное расположение
         * названия инвентаря.
         */
        playerInventoryTitleX = 8;
        playerInventoryTitleY = 70;
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
         * Только нижняя его часть.
         *
         * ВАЖНО:
         * его экранная позиция не меняется.
         */
        context.drawTexture(
                INVENTORY_TEXTURE,
                x,
                y + 76,
                0,
                76,
                WIDTH,
                HEIGHT - 76,
                256,
                256
        );

        /*
         * =================================================
         * НАША ПАНЕЛЬ САМОХОДНОЙ ЛОДКИ
         * =================================================
         *
         * Она начинается чуть ниже верхней границы,
         * чтобы закрыть выступающий кусок старого GUI.
         */
        int panelX =
                x;

        int panelY =
                y + PANEL_Y;

        int panelBottom =
                panelY + TOP_HEIGHT;

        /*
         * Внешняя рамка.
         */
        context.fill(
                panelX,
                panelY,
                panelX + WIDTH,
                panelBottom,
                0xFF373737
        );

        /*
         * Основная светлая часть.
         */
        context.fill(
                panelX + 1,
                panelY + 1,
                panelX + WIDTH - 1,
                panelBottom - 1,
                0xFFC6C6C6
        );

        /*
         * Светлая верхняя грань.
         */
        context.fill(
                panelX + 2,
                panelY + 2,
                panelX + WIDTH - 2,
                panelY + 3,
                0xFFE8E8E8
        );

        /*
         * Нижняя грань.
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
         * СЛОТ ТОПЛИВА
         * =================================================
         *
         * Все элементы нашего меню также смещены
         * на PANEL_Y.
         */
        context.drawTexture(
                FURNACE_TEXTURE,
                panelX + 79,
                panelY + 36,
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
                    panelY + 42
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
         * ШКАЛА
         * =================================================
         */

        int gaugeX =
                panelX + 126;

        int gaugeY =
                panelY + 15;

        int gaugeWidth =
                12;

        int gaugeHeight =
                42;

        context.fill(
                gaugeX,
                gaugeY,
                gaugeX + gaugeWidth,
                gaugeY + gaugeHeight,
                0xFF3A3A3A
        );

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
         * Текст тоже смещаем вместе с панелью.
         */
        int textOffset =
                PANEL_Y;

        context.drawText(
                textRenderer,
                title,
                8,
                7 + textOffset,
                0x404040,
                false
        );

        context.drawText(
                textRenderer,
                Text.translatable(
                        "container.minecartmagic.fuel"
                ),
                69,
                18 + textOffset,
                0x404040,
                false
        );

        context.drawText(
                textRenderer,
                Text.translatable(
                        "container.minecartmagic.fuel_count",
                        handler.getFuelStackCount()
                ),
                8,
                50 + textOffset,
                0x404040,
                false
        );

        context.drawText(
                textRenderer,
                Text.translatable(
                        "container.minecartmagic.burn_time",
                        handler.getRemainingSeconds()
                ),
                8,
                61 + textOffset,
                0x404040,
                false
        );
    }
}
