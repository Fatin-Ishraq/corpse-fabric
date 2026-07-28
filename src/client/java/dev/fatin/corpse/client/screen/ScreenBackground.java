package dev.fatin.corpse.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

final class ScreenBackground {

    static void draw(GuiGraphics graphics, AbstractContainerMenu menu,
                     int left, int top, int width, int height) {
        graphics.fill(left - 1, top - 1, left + width + 1, top + height + 1, 0xFF090B0E);
        graphics.fill(left, top, left + width, top + height, 0xFF20252B);
        graphics.fill(left + 4, top + 4, left + width - 4, top + height - 4, 0xFF15191E);
        for (Slot slot : menu.slots) {
            int x = left + slot.x;
            int y = top + slot.y;
            graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF080A0D);
            graphics.fill(x, y, x + 16, y + 16, 0xFF2D343C);
        }
    }

    private ScreenBackground() {
    }
}
