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
     * Используем только маленькие куски
     * ванильной furnace texture:
     *
     * - рамка слота;
     * - анимированный огонь.
     */
    private static final Identifier FURNACE_TEXTURE =
            Identifier.ofVanilla(
                    "textures/gui/container/furnace.png"
            );

    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;

    /*
     * Верхняя панель.
     */
    private static final int PANEL_TOP_HEIGHT = 70;

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
    }

    @Override
    protected void drawBackground(
            DrawContext context,
            float delta,
            int mouseX,
            int mouseY
    ) {
        int x =
                (width - backgroundWidth) / 2;

        int y =
                (height - backgroundHeight) / 2;

        /*
         * =================================================
         * ВЕРХНЯЯ ПАНЕЛЬ — МЕНЮ ЛОДКИ
         * =================================================
         */

        /*
         * Тёмная внешняя рамка.
         */
        context.fill(
                x,
                y,
                x + WIDTH,
                y + PANEL_TOP_HEIGHT,
                0xFF373737
        );

        /*
         * Основная светлая панель.
         */
        context.fill(
                x + 1,
                y + 1,
                x + WIDTH - 1,
                y + PANEL_TOP_HEIGHT - 1,
                0xFFC6C6C6
        );

        /*
         * Верхняя подсветка.
         */
        context.fill(
                x + 2,
                y + 2,
                x + WIDTH - 2,
                y + 3,
                0xFFE2E2E2
        );

        /*
         * =================================================
         * СЛОТ ТОПЛИВА
         * =================================================
         */

        int fuelSlotX =
                x + 79;

        int fuelSlotY =
                y + 36;

        /*
         * Рамка слота берётся напрямую
         * из ванильной furnace texture.
         */
        context.drawTexture(
                FURNACE_TEXTURE,
                fuelSlotX,
                fuelSlotY,
                56,
                53,
                18,
                18,
                256,
                256
        );

        /*
         * =================================================
         * ОГОНЬ
         * =================================================
         *
         * Это тот же flame sprite,
         * который используется ванильной печью.
         *
         * Высота меняется каждый тик.
         */
        int flameHeight =
                handler.getFuelProgress();

        if (flameHeight > 0) {

            int flameX =
                    x + 103;

            int flameY =
                    y + 43
                            + (14 - flameHeight);

            context.drawTexture(
                    FURNACE_TEXTURE,
                    flameX,
                    flameY,
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
         *
         * Отдельная вертикальная шкала.
         *
         * Полностью заполнена:
         * 100%
         *
         * Пустая:
         * 0%
         */
        int gaugeX =
                x + 126;

        int gaugeY =
                y + 15;

        int gaugeWidth = 12;
        int gaugeHeight = 40;

        /*
         * Внешняя рамка.
         */
        context.fill(
                gaugeX,
                gaugeY,
                gaugeX + gaugeWidth,
                gaugeY + gaugeHeight,
                0xFF373737
        );

        /*
         * Внутренняя область.
         */
        context.fill(
                gaugeX + 2,
                gaugeY + 2,
                gaugeX + gaugeWidth - 2,
                gaugeY + gaugeHeight - 2,
                0xFF555555
        );

        int fuelPercent =
                handler.getFuelPercent();

        int filledHeight =
                Math.round(
                        (gaugeHeight - 4)
                                * fuelPercent
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
             * Маленькая подсветка шкалы.
             */
            context.fill(
                    gaugeX + 3,
                    top,
                    gaugeX + 4,
                    gaugeY + gaugeHeight - 2,
                    0xFFFFC85C
            );
        }

        /*
         * =================================================
         * РАЗДЕЛИТЕЛЬ
         * =================================================
         */

        context.fill(
                x,
                y + PANEL_TOP_HEIGHT,
                x + WIDTH,
                y + PANEL_TOP_HEIGHT + 1,
                0xFF373737
        );

        /*
         * =================================================
         * НИЖНЯЯ ЧАСТЬ — ИНВЕНТАРЬ ИГРОКА
         * =================================================
         *
         * Теперь здесь НЕ белый фон.
         */
        context.fill(
                x,
                y + PANEL_TOP_HEIGHT + 1,
                x + WIDTH,
                y + HEIGHT,
                0xFF3F3F3F
        );

        /*
         * Более светлая внутренняя область.
         */
        context.fill(
                x + 4,
                y + PANEL_TOP_HEIGHT + 5,
                x + WIDTH - 4,
                y + HEIGHT - 4,
                0xFF8B8B8B
        );
    }

    @Override
    protected void drawForeground(
            DrawContext context,
            int mouseX,
            int mouseY
    ) {
        /*
         * Название меню.
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
         * Сколько топлива осталось в слоте.
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
         * Сколько секунд осталось
         * у текущего топлива.
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
         * Подпись над инвентарём игрока.
         */
        context.drawText(
                textRenderer,
                playerInventoryTitle,
                8,
                74,
                0xFFFFFF,
                false
        );
    }
}
