package com.minecartmagic.client;

import com.minecartmagic.screen.SelfPropellingBoatScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SelfPropellingBoatScreen
        extends HandledScreen<
                SelfPropellingBoatScreenHandler
                > {

    private static final Identifier FURNACE_TEXTURE =
            Identifier.ofVanilla(
                    "textures/gui/container/furnace.png"
            );

    private static final int BACKGROUND_WIDTH = 176;
    private static final int BACKGROUND_HEIGHT = 166;

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
                BACKGROUND_WIDTH;

        backgroundHeight =
                BACKGROUND_HEIGHT;
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
         * Full vanilla furnace GUI.
         */
        context.drawTexture(
                FURNACE_TEXTURE,
                x,
                y,
                0,
                0,
                BACKGROUND_WIDTH,
                BACKGROUND_HEIGHT,
                256,
                256
        );

        /*
         * Vanilla-style flame.
         *
         * Texture region:
         * U = 176
         * V = 0
         * W = 14
         * H = 14
         */
        int flameHeight =
                handler.getFuelProgress();

        if (flameHeight > 0) {

            context.drawTexture(
                    FURNACE_TEXTURE,
                    x + 56,
                    y + 36 + 14 - flameHeight,
                    176,
                    14 - flameHeight,
                    14,
                    flameHeight,
                    256,
                    256
            );
        }
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
