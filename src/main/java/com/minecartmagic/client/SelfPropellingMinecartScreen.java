package com.minecartmagic.client;

import com.minecartmagic.screen.SelfPropellingMinecartScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SelfPropellingMinecartScreen
        extends HandledScreen<SelfPropellingMinecartScreenHandler> {

    private static final Identifier DISPENSER_TEXTURE =
            Identifier.ofVanilla("textures/gui/container/dispenser.png");

    private static final Identifier FURNACE_TEXTURE =
            Identifier.ofVanilla("textures/gui/container/furnace.png");

    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;
    private static final int TOP_PANEL_HEIGHT = 76;

    public SelfPropellingMinecartScreen(
            SelfPropellingMinecartScreenHandler handler,
            PlayerInventory inventory,
            Text title
    ) {
        super(handler, inventory, title);

        backgroundWidth = WIDTH;
        backgroundHeight = HEIGHT;
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
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

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

        context.fill(
                x,
                y,
                x + WIDTH,
                y + TOP_PANEL_HEIGHT,
                0xFF373737
        );

        context.fill(
                x + 1,
                y + 1,
                x + WIDTH - 1,
                y + TOP_PANEL_HEIGHT - 1,
                0xFFC6C6C6
        );

        context.fill(
                x + 2,
                y + 2,
                x + WIDTH - 2,
                y + 3,
                0xFFE8E8E8
        );

        context.fill(
                x + 2,
                y + TOP_PANEL_HEIGHT - 3,
                x + WIDTH - 2,
                y + TOP_PANEL_HEIGHT - 2,
                0xFF8A8A8A
        );

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

        int flameHeight = handler.getFuelProgress();

        if (flameHeight > 0) {
            context.drawTexture(
                    FURNACE_TEXTURE,
                    x + 103,
                    y + 36 + (14 - flameHeight),
                    176,
                    14 - flameHeight,
                    14,
                    flameHeight,
                    256,
                    256
            );
        }

        int gaugeX = x + 126;
        int gaugeY = y + 12;
        int gaugeWidth = 12;
        int gaugeHeight = 42;

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

        int percent = handler.getFuelPercent();
        int filledHeight = Math.round(
                (gaugeHeight - 4) * percent / 100.0F
        );

        if (filledHeight > 0) {
            int top =
                    gaugeY + gaugeHeight - 2 - filledHeight;

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
                Text.translatable("container.minecartmagic.fuel"),
                69,
                17,
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
                43,
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
                54,
                0x404040,
                false
        );
    }
}
