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
     * Верхняя часть меню самоходной лодки.
     *
     * Нижние 90 пикселей будут настоящим
     * ванильным инвентарём игрока.
     */
    private static final int TOP_HEIGHT = 76;

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

        backgroundWidth = WIDTH;
        backgroundHeight = HEIGHT;

        /*
         * HandledScreen иначе сам будет рисовать
         * затемнение/фон за GUI.
         */
        playerInventoryTitleX = 8;
        playerInventoryTitleY = 74;
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
         * ВЕРХНЯЯ ЧАСТЬ — НАША ПАНЕЛЬ ЛОДКИ
         * =================================================
         */

        /*
         * Внешняя тёмная рамка.
         */
        context.fill(
                x,
                y,
                x + WIDTH,
                y + TOP_HEIGHT,
                0xFF373737
        );

        /*
         * Основная внутренняя панель.
         */
        context.fill(
                x + 1,
                y + 1,
                x + WIDTH - 1,
                y + TOP_HEIGHT - 1,
                0xFFC6C6C6
        );

        /*
         * Светлая верхняя грань.
         */
        context.fill(
                x + 2,
                y + 2,
                x + WIDTH - 2,
                y + 3,
                0xFFE8E8E8
        );

        /*
         * Темнее по нижнему краю.
         */
        context.fill(
                x + 2,
                y + TOP_HEIGHT - 3,
                x + WIDTH - 2,
                y + TOP_HEIGHT - 2,
                0xFF8A8A8A
        );

        /*
         * =================================================
         * СЛОТ ТОПЛИВА
         * =================================================
         */

        int fuelX =
                x + 79;

        int fuelY =
                y + 36;

        /*
         * Только 18x18 ванильного слота.
         */
        context.drawTexture(
                FURNACE_TEXTURE,
                fuelX,
                fuelY,
                56,
                53,
                18,
                18,
                256,
                256
        );

        /*
         * =================================================
         * АНИМИРОВАННЫЙ ОГОНЬ
         * =================================================
         */

        int flameHeight =
                handler.getFuelProgress();

        if (flameHeight > 0) {

            context.drawTexture(
                    FURNACE_TEXTURE,
                    x + 103,
                    y + 44
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
         * ШКАЛА ОСТАВШЕГОСЯ ГОРЕНИЯ
         * =================================================
         */

        int gaugeX =
                x + 126;

        int gaugeY =
                y + 18;

        int gaugeWidth =
                12;

        int gaugeHeight =
                42;

        /*
         * Тёмная рамка.
         */
        context.fill(
                gaugeX,
                gaugeY,
                gaugeX + gaugeWidth,
                gaugeY + gaugeHeight,
                0xFF3A3A3A
        );

        /*
         * Внутренний тёмный фон.
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
             * Основное заполнение.
             */
            context.fill(
                    gaugeX + 3,
                    top,
                    gaugeX + gaugeWidth - 3,
                    gaugeY + gaugeHeight - 2,
                    0xFFE6A23C
            );

            /*
             * Подсветка.
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
         * =================================================
         * РАЗДЕЛИТЕЛЬ
         * =================================================
         */
        context.fill(
                x,
                y + TOP_HEIGHT,
                x + WIDTH,
                y + TOP_HEIGHT + 1,
                0xFF555555
        );

        /*
         * =================================================
         * НИЖНЯЯ ЧАСТЬ — НАСТОЯЩИЙ VANILLA INVENTORY
         * =================================================
         *
         * Берём именно кусок vanilla inventory.png,
         * а не рисуем его вручную.
         *
         * В inventory.png нижняя часть стандартного
         * инвентаря начинается примерно с Y=76.
         */
        context.drawTexture(
                INVENTORY_TEXTURE,
                x,
                y + TOP_HEIGHT + 1,
                0,
                76,
                WIDTH,
                HEIGHT - TOP_HEIGHT - 1,
                256,
                256
        );
    }

    @Override
    protected void drawForeground(
            DrawContext context,
            int mouseX,
            int mouseY
    ) {
        /*
         * Название.
         */
        context.drawText(
                textRenderer,
                title,
                8,
                7,
                0x404040,
                false
        );

        /*
         * Подпись топлива.
         */
        context.drawText(
                textRenderer,
                Text.translatable(
                        "container.minecartmagic.fuel"
                ),
                69,
                19,
                0x404040,
                false
        );

        /*
         * Запас топлива в слоте.
         */
        int fuelCount =
                handler.getFuelStackCount();

        context.drawText(
                textRenderer,
                Text.translatable(
                        "container.minecartmagic.fuel_count",
                        fuelCount
                ),
                8,
                57,
                0x404040,
                false
        );

        /*
         * Время горения текущего топлива.
         */
        int seconds =
                handler.getRemainingSeconds();

        context.drawText(
                textRenderer,
                Text.translatable(
                        "container.minecartmagic.burn_time",
                        seconds
                ),
                8,
                68,
                0x404040,
                false
        );

        /*
         * Название инвентаря.
         *
         * Оно находится ровно там же, где
         * у vanilla inventory screen.
         */
        context.drawText(
                textRenderer,
                playerInventoryTitle,
                8,
                74,
                0x404040,
                false
        );
    }
}
