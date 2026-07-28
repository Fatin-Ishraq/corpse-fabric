package dev.fatin.corpse.client.screen;

import dev.fatin.corpse.menu.HistoryMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class HistoryInventoryScreen extends AbstractContainerScreen<HistoryMenu> {

    public HistoryInventoryScreen(HistoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 222);
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
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        ScreenBackground.draw(graphics, menu, leftPos, topPos, imageWidth, imageHeight);
        super.extractContents(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, 0xFFE7EDF4, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFFAEB8C4, false);
        if (!menu.isEditable()) {
            graphics.centeredText(font, Component.translatable("gui.corpse.read_only"), imageWidth / 2, 113, 0xFFD8A657);
        }
    }
}