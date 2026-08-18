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
     * Было 76.
     *
     * Подняли нижнюю границу нашей панели,
     * чтобы полностью закрыть остатки верхней
     * части inventory.png.
     */
    private static final int TOP_HEIGHT = 82;

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
         * Стандартные слоты игрока остаются
         * в своих координатах.
         *
         * Название инвентаря опускаем немного ниже,
         * чтобы оно не залезало на панель лодки.
         */
        playerInventoryTitleX = 8;
        playerInventoryTitleY = 81;
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
         * НИЖНЯЯ ЧАСТЬ VANILLA INVENTORY
         * =================================================
         *
         * Берём только нижнюю часть текстуры.
         *
         * Верхняя область:
         * - skin;
         * - armor;
         * - crafting;
         * вообще не используется.
         */
        context.drawTexture(
                INVENTORY_TEXTURE,
                x,
                y + TOP_HEIGHT,
                0,
                76,
                WIDTH,
                HEIGHT - TOP_HEIGHT,
                256,
                256
        );

        /*
         * =================================================
         * ВЕРХНЯЯ ПАНЕЛЬ ЛОДКИ
         * =================================================
         */

        /*
         * Внешняя рамка.
         */
        context.fill(
                x,
                y,
                x + WIDTH,
                y + TOP_HEIGHT,
                0xFF373737
        );

        /*
         * Основная светлая поверхность.
         */
        context.fill(
                x + 1,
                y + 1,
                x + WIDTH - 1,
                y + TOP_HEIGHT - 1,
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
                0xFFE8E8E8
        );

        /*
         * Нижняя грань.
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
         * ЕДИНСТВЕННЫЙ ТОПЛИВНЫЙ СЛОТ
         * =================================================
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
         * =================================================
         * АНИМИРОВАННОЕ ПЛАМЯ
         * =================================================
         */

        int flameHeight =
                handler.getFuelProgress();

        if (flameHeight > 0) {

            context.drawTexture(
                    FURNACE_TEXTURE,
                    x + 103,
                    y + 42
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
                x + 126;

        int gaugeY =
                y + 15;

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
         * Пустая часть.
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
         * Топливо.
         */
        context.drawText(
                textRenderer,
                Text.translatable(
                        "container.minecartmagic.fuel"
                ),
                69,
                18,
                0x404040,
                false
        );

        /*
         * Запас топлива.
         */
        context.drawText(
                textRenderer,
                Text.translatable(
                        "container.minecartmagic.fuel_count",
                        handler.getFuelStackCount()
                ),
                8,
                50,
                0x404040,
                false
        );

        /*
         * Время текущего горения.
         */
        context.drawText(
                textRenderer,
                Text.translatable(
                        "container.minecartmagic.burn_time",
                        handler.getRemainingSeconds()
                ),
                8,
                61,
                0x404040,
                false
        );

        /*
         * playerInventoryTitle здесь не рисуем вручную.
         */
    }
}
