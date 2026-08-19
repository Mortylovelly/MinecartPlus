package com.minecartmagic.client;

import com.minecartmagic.screen.SelfPropellingBoatScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class SelfPropellingBoatScreen
        extends HandledScreen<SelfPropellingBoatScreenHandler> {

    private static final int WIDTH = 176;

    /*
     * Верхняя панель:
     *
     * 0 - 75
     *
     * Нижняя панель:
     *
     * 76 - 165
     */
    private static final int TOP_PANEL_HEIGHT = 76;

    private static final int HEIGHT = 166;

    /*
     * Первый ряд инвентаря.
     */
    private static final int PLAYER_INVENTORY_Y = 84;

    /*
     * Hotbar.
     */
    private static final int HOTBAR_Y = 142;

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
         * Название player inventory.
         *
         * Именно такая схема используется у нормальных
         * контейнерных GUI: контейнер сверху, инвентарь снизу.
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
         * =====================================================
         * ВСЁ ОКНО
         * =====================================================
         */
        context.fill(
                x,
                y,
                x + WIDTH,
                y + HEIGHT,
                0xFF373737
        );

        /*
         * =====================================================
         * ВЕРХНЯЯ ПАНЕЛЬ САМОХОДНОЙ ЛОДКИ
         * =====================================================
         */
        context.fill(
                x + 1,
                y + 1,
                x + WIDTH - 1,
                y + TOP_PANEL_HEIGHT,
                0xFFC6C6C6
        );

        /*
         * Верхняя светлая рамка.
         */
        context.fill(
                x + 2,
                y + 2,
                x + WIDTH - 2,
                y + 3,
                0xFFE8E8E8
        );

        /*
         * Нижняя тёмная линия верхнего контейнера.
         */
        context.fill(
                x + 2,
                y + TOP_PANEL_HEIGHT - 2,
                x + WIDTH - 2,
                y + TOP_PANEL_HEIGHT,
                0xFF8A8A8A
        );

        /*
         * =====================================================
         * СЛОТ ТОПЛИВА
         * =====================================================
         *
         * Сам Slot в ScreenHandler находится:
         *
         * X = 79
         * Y = 30
         *
         * Поэтому здесь используется ровно та же позиция.
         */
        drawSlotBackground(
                context,
                x + 79,
                y + 30
        );

        /*
         * =====================================================
         * ПЛАМЯ
         * =====================================================
         */
        int flameHeight =
                handler.getFuelProgress();

        if (flameHeight > 0) {

            int flameX =
                    x + 103;

            int flameY =
                    y + 36;

            /*
             * Общая высота пламени:
             * 14 пикселей.
             */
            int fullHeight = 14;

            /*
             * Наша отрисовка делает пламя простой
             * ванильной оранжевой полосой.
             */
            context.fill(
                    flameX,
                    flameY
                            + (fullHeight - flameHeight),
                    flameX + 14,
                    flameY + fullHeight,
                    0xFFE6A23C
            );

            context.fill(
                    flameX,
                    flameY
                            + (fullHeight - flameHeight),
                    flameX + 2,
                    flameY + fullHeight,
                    0xFFFFCF69
            );
        }

        /*
         * =====================================================
         * ИНДИКАТОР ТОПЛИВА
         * =====================================================
         */
        int gaugeX =
                x + 126;

        int gaugeY =
                y + 12;

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

            context.fill(
                    gaugeX + 3,
                    top,
                    gaugeX + 4,
                    gaugeY + gaugeHeight - 2,
                    0xFFFFCF69
            );
        }

        /*
         * =====================================================
         * НИЖНЯЯ ПАНЕЛЬ ИНВЕНТАРЯ ИГРОКА
         * =====================================================
         *
         * ВАЖНО:
         *
         * Здесь больше НЕТ inventory.png.
         *
         * Поэтому:
         *
         * - нет модели игрока;
         * - нет 2x2 crafting;
         * - нет верхней части обычного inventory;
         * - нет наложения текстуры.
         *
         * Только контейнерный фон и реальные слоты.
         */
        context.fill(
                x + 1,
                y + TOP_PANEL_HEIGHT,
                x + WIDTH - 1,
                y + HEIGHT - 1,
                0xFFC6C6C6
        );

        /*
         * Разделительная тёмная линия.
         */
        context.fill(
                x + 1,
                y + TOP_PANEL_HEIGHT,
                x + WIDTH - 1,
                y + TOP_PANEL_HEIGHT + 2,
                0xFF8A8A8A
        );

        /*
         * =====================================================
         * 3 РЯДА ИНВЕНТАРЯ
         * =====================================================
         */
        for (int row = 0; row < 3; row++) {

            for (int column = 0; column < 9; column++) {

                drawSlotBackground(
                        context,
                        x + 8 + column * 18,
                        y + PLAYER_INVENTORY_Y
                                + row * 18
                );
            }
        }

        /*
         * =====================================================
         * HOTBAR
         * =====================================================
         */
        for (int column = 0; column < 9; column++) {

            drawSlotBackground(
                    context,
                    x + 8 + column * 18,
                    y + HOTBAR_Y
            );
        }
    }

    /*
     * Отрисовка стандартного контейнерного слота.
     *
     * Мы рисуем его вручную, чтобы полностью исключить
     * inventory.png и всё, что относится к экрану игрока.
     */
    private void drawSlotBackground(
            DrawContext context,
            int x,
            int y
    ) {

        /*
         * Внешняя тёмная рамка.
         */
        context.fill(
                x,
                y,
                x + 18,
                y + 18,
                0xFF373737
        );

        /*
         * Внутренняя поверхность.
         */
        context.fill(
                x + 1,
                y + 1,
                x + 17,
                y + 17,
                0xFF8B8B8B
        );

        /*
         * Верхняя/левая подсветка.
         */
        context.fill(
                x + 1,
                y + 1,
                x + 17,
                y + 2,
                0xFF555555
        );

        context.fill(
                x + 1,
                y + 1,
                x + 2,
                y + 17,
                0xFF555555
        );
    }

    @Override
    protected void drawForeground(
            DrawContext context,
            int mouseX,
            int mouseY
    ) {

        /*
         * =====================================================
         * НАЗВАНИЕ
         * =====================================================
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
         * =====================================================
         * "ТОПЛИВО"
         * =====================================================
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
         * =====================================================
         * КОЛИЧЕСТВО ТОПЛИВА
         * =====================================================
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
         * =====================================================
         * ВРЕМЯ ГОРЕНИЯ
         * =====================================================
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
