package dev.fatin.corpse.client.screen;

import dev.fatin.corpse.menu.HistoryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class HistoryInventoryScreen extends AbstractContainerScreen<HistoryMenu> {

    public HistoryInventoryScreen(HistoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 222;
        inventoryLabelY = 130;
    }

    @Override
    protected void init() {
        super.init();
        if (menu.isEditable()) {
            addRenderableWidget(Button.builder(Component.translatable("gui.corpse.recover_copy"), button -> {
                if (minecraft != null && minecraft.gameMode != null) {
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, HistoryMenu.RECOVER_BUTTON_ID);
                }
            }).bounds(leftPos + 43, topPos + 111, 90, 18).build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        ScreenBackground.draw(graphics, menu, leftPos, topPos, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0xE7EDF4, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xAEB8C4, false);
        if (!menu.isEditable()) {
            graphics.drawCenteredString(font, Component.translatable("gui.corpse.read_only"), imageWidth / 2, 113, 0xD8A657);
        }
    }
}
