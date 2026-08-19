package com.minecartmagic.client;

import com.minecartmagic.screen.SelfPropellingBoatScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SelfPropellingBoatScreen
        extends HandledScreen<SelfPropellingBoatScreenHandler> {

    /*
     * Ванильная текстура контейнера.
     *
     * Dispenser имеет ту же стандартную нижнюю часть,
     * которая нам нужна:
     *
     * 3 ряда inventory
     * +
     * hotbar.
     */
    private static final Identifier DISPENSER_TEXTURE =
            Identifier.ofVanilla(
                    "textures/gui/container/dispenser.png"
            );

    /*
     * Ванильная текстура печки.
     *
     * Используем только слот топлива и пламя.
     */
    private static final Identifier FURNACE_TEXTURE =
            Identifier.ofVanilla(
                    "textures/gui/container/furnace.png"
            );

    private static final int WIDTH = 176;

    private static final int HEIGHT = 166;

    /*
     * Всё выше 76 принадлежит нашей самоходной печке.
     *
     * Ниже начинается обычный player inventory.
     */
    private static final int TOP_PANEL_HEIGHT = 76;

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

        /*
         * Стандартные размеры контейнерного GUI.
         */
        backgroundWidth =
                WIDTH;

        backgroundHeight =
                HEIGHT;

        /*
         * "Инвентарь" рисуется там же,
         * где он находится у обычных контейнеров.
         */
        playerInventoryTitleX =
                8;

        playerInventoryTitleY =
                77;
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
         * ОСНОВА ВЕСЬМА ОКНА
         * =====================================================
         *
         * Сначала полностью рисуем ванильный dispenser GUI.
         *
         * Его нижняя часть уже является нормальным
         * Minecraft inventory:
         *
         * [9 слотов]
         * [9 слотов]
         * [9 слотов]
         * [hotbar]
         *
         * Никакой модели игрока.
         * Никакого 2x2 crafting.
         */
        context.drawTexture(
                DISPENSER_TEXTURE,
                x,
                y,
                0,
                0,
                WIDTH,
                HEIGHT,
                256,
                256
        );

        /*
         * =====================================================
         * ВЕРХНЯЯ ПАНЕЛЬ САМОХОДНОЙ ЛОДКИ
         * =====================================================
         *
         * Полностью закрываем верхнюю часть dispenser,
         * чтобы его 3x3 верхних слота тоже не были видны.
         */
        context.fill(
                x,
                y,
                x + WIDTH,
                y + TOP_PANEL_HEIGHT,
                0xFF373737
        );

        /*
         * Внутренняя поверхность.
         */
        context.fill(
                x + 1,
                y + 1,
                x + WIDTH - 1,
                y + TOP_PANEL_HEIGHT - 1,
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
         * Нижняя линия разделения.
         */
        context.fill(
                x + 2,
                y + TOP_PANEL_HEIGHT - 3,
                x + WIDTH - 2,
                y + TOP_PANEL_HEIGHT - 2,
                0xFF8A8A8A
        );

        /*
         * =====================================================
         * СЛОТ ТОПЛИВА
         * =====================================================
         *
         * ScreenHandler:
         *
         * X = 79
         * Y = 36
         *
         * Поэтому отрисовываем его ровно там же.
         */
        context.drawTexture(
                FURNACE_TEXTURE,
                x + 79,
                y + 36,
                56,
                53,
                18,
                18,
                256,
                256
        );

        /*
         * =====================================================
         * ПЛАМЯ ПЕЧКИ
         * =====================================================
         */
        int flameHeight =
                handler.getFuelProgress();

        if (flameHeight > 0) {

            /*
             * Ванильная furnace texture:
             *
             * flame region:
             *
             * x = 176
             * y = 14...27
             */
            context.drawTexture(
                    FURNACE_TEXTURE,
                    x + 103,
                    y + 36
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
         * =====================================================
         * ШКАЛА ТОПЛИВА
         * =====================================================
         *
         * Оставляем нашу дополнительную шкалу,
         * потому что у самоходной лодки это именно
         * двигатель, а не обычная печь.
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
         * Внешняя рамка.
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

            /*
             * Основная полоса топлива.
             */
            context.fill(
                    gaugeX + 3,
                    top,
                    gaugeX + gaugeWidth - 3,
                    gaugeY + gaugeHeight - 2,
                    0xFFE6A23C
            );

            /*
             * Светлый блик.
             */
            context.fill(
                    gaugeX + 3,
                    top,
                    gaugeX + 4,
                    gaugeY + gaugeHeight - 2,
                    0xFFFFCF69
            );
        }

        /*
         * ВАЖНО:
         *
         * Нижнюю часть inventory здесь больше НИКАК
         * не рисуем.
         *
         * Она уже полностью предоставляется
         * ванильной dispenser texture.
         *
         * Реальные ItemStack slots отрисует
         * сам HandledScreen.
         */
    }

    @Override
    protected void drawForeground(
            DrawContext context,
            int mouseX,
            int mouseY
    ) {

        /*
         * =====================================================
         * НАЗВАНИЕ САМОХОДНОЙ ЛОДКИ
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
