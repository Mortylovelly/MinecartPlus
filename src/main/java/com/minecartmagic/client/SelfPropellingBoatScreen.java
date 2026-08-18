package com.minecartmagic.client;

import com.minecartmagic.screen.SelfPropellingBoatScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SelfPropellingBoatScreen
        extends HandledScreen<SelfPropellingBoatScreenHandler> {

    private static final Identifier FURNACE_TEXTURE =
            Identifier.ofVanilla(
                    "textures/gui/container/furnace.png"
            );

    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;

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
         * Весь ванильный фон.
         */
        context.drawTexture(
                FURNACE_TEXTURE,
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
         * -------------------------------------------------
         * Убираем НЕ нужные визуальные элементы печки.
         * -------------------------------------------------
         */

        /*
         * Верхний INPUT SLOT.
         */
        cover(
                context,
                x + 56,
                y + 17,
                18,
                18
        );

        /*
         * OUTPUT SLOT.
         */
        cover(
                context,
                x + 116,
                y + 36,
                18,
                18
        );

        /*
         * Стрелка переплавки.
         */
        cover(
                context,
                x + 79,
                y + 34,
                26,
                16
        );

        /*
         * -------------------------------------------------
         * ЕДИНСТВЕННЫЙ слот самоходной лодки.
         *
         * Это ровно та же координата, что у настоящего
         * FuelSlot в ScreenHandler:
         *
         * x = 79
         * y = 37
         * -------------------------------------------------
         */
        drawVanillaSlot(
                context,
                x + 79,
                y + 37
        );

        /*
         * Пламя.
         */
        int flameHeight =
                handler.getFuelProgress();

        if (flameHeight > 0) {

            context.drawTexture(
                    FURNACE_TEXTURE,
                    x + 80,
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
    }

    /*
     * Рисует один обычный vanilla slot.
     *
     * Берём его прямо из GUI-текстуры печи.
     */
    private void drawVanillaSlot(
            DrawContext context,
            int x,
            int y
    ) {
        context.drawTexture(
                FURNACE_TEXTURE,
                x,
                y,
                56,
                53,
                18,
                18,
                256,
                256
        );
    }

    /*
     * Перекрывает часть обычной печной GUI,
     * которая нам не нужна.
     */
    private void cover(
            DrawContext context,
            int x,
            int y,
            int width,
            int height
    ) {
        context.fill(
                x,
                y,
                x + width,
                y + height,
                0xFFC6C6C6
        );
    }

    @Override
    protected void drawForeground(
            DrawContext context,
            int mouseX,
            int mouseY
    ) {

        context.drawText(
                textRenderer,
                title,
                8,
                6,
                0x404040,
                false
        );

        context.drawText(
                textRenderer,
                playerInventoryTitle,
                8,
                72,
                0x404040,
                false
        );
    }
}
