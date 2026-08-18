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
         * ---------------------------------------------
         * ОСНОВНАЯ ПАНЕЛЬ
         * ---------------------------------------------
         *
         * Никакой furnace.png целиком.
         */
        context.fill(
                x,
                y,
                x + WIDTH,
                y + HEIGHT,
                0xFFC6C6C6
        );

        /*
         * Верхняя внутренняя область.
         */
        context.fill(
                x + 4,
                y + 4,
                x + WIDTH - 4,
                y + 70,
                0xFF8B8B8B
        );

        /*
         * Внутренняя светлая поверхность.
         */
        context.fill(
                x + 6,
                y + 6,
                x + WIDTH - 6,
                y + 68,
                0xFFC6C6C6
        );

        /*
         * ---------------------------------------------
         * Топливный слот.
         * ---------------------------------------------
         *
         * Единственный слот.
         *
         * Координаты должны совпадать
         * с ScreenHandler:
         *
         * 79 / 37
         */
        drawVanillaSlot(
                context,
                x + 79,
                y + 37
        );

        /*
         * ---------------------------------------------
         * Огонь.
         * ---------------------------------------------
         *
         * Берём только ванильный flame sprite
         * из furnace texture.
         */
        int flameHeight =
                handler.getFuelProgress();

        if (flameHeight > 0) {

            context.drawTexture(
                    FURNACE_TEXTURE,
                    x + 103,
                    y + 39
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
         * ---------------------------------------------
         * Разделитель перед инвентарём.
         * ---------------------------------------------
         */
        context.fill(
                x + 4,
                y + 76,
                x + WIDTH - 4,
                y + 77,
                0xFF8B8B8B
        );

        /*
         * Сама область инвентаря.
         */
        context.fill(
                x + 4,
                y + 78,
                x + WIDTH - 4,
                y + HEIGHT - 4,
                0xFFC6C6C6
        );
    }

    private void drawVanillaSlot(
            DrawContext context,
            int x,
            int y
    ) {
        /*
         * Только 18x18 область настоящего
         * топливного слота из ванильной печи.
         *
         * Никаких других furnace GUI элементов.
         */
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
                6,
                0x404040,
                false
        );

        /*
         * Подпись над единственным слотом.
         */
        context.drawText(
                textRenderer,
                Text.translatable(
                        "container.minecartmagic.fuel"
                ),
                68,
                20,
                0x404040,
                false
        );

        /*
         * Инвентарь игрока.
         */
        context.drawText(
                textRenderer,
                playerInventoryTitle,
                8,
                78,
                0x404040,
                false
        );
    }
}
