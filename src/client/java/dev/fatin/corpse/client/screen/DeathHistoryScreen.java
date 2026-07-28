package dev.fatin.corpse.client.screen;

import dev.fatin.corpse.history.DeathSummary;
import dev.fatin.corpse.network.CorpseNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class DeathHistoryScreen extends Screen {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int PANEL_WIDTH = 330;
    private static final int PANEL_HEIGHT = 166;
    private static final int PANEL_BOTTOM_PADDING = 7;
    private static final int BUTTON_HEIGHT = 20;
    private static final int PANEL_BACKGROUND = 0xFFC6C6C6;
    private static final int PANEL_BORDER_DARK = 0xFF000000;
    private static final int PANEL_BORDER_LIGHT = 0xFFFFFFFF;
    private static final int PANEL_BORDER_SHADOW = 0xFF555555;
    private static final int TEXT_PRIMARY = 0xFF000000;
    private static final int TEXT_SECONDARY = 0xFF555555;
    private static final int TEXT_CAUSE = 0xFFAA0000;

    private final String playerName;
    private final List<DeathSummary> deaths;
    private int index;
    private Button previousButton;
    private Button nextButton;
    private Button itemsButton;
    private Button locationButton;

    public DeathHistoryScreen(String playerName, List<DeathSummary> deaths) {
        super(Component.translatable("gui.corpse.history", playerName));
        this.playerName = playerName;
        this.deaths = List.copyOf(deaths);
    }

    @Override
    protected void init() {
        int center = width / 2;
        int panelTop = (height - PANEL_HEIGHT) / 2;
        int buttonY = panelTop + PANEL_HEIGHT - PANEL_BOTTOM_PADDING - BUTTON_HEIGHT;
        previousButton = addRenderableWidget(Button.builder(Component.translatable("gui.corpse.previous"),
                button -> changePage(-1)).bounds(center - 154, buttonY, 72, 20).build());
        itemsButton = addRenderableWidget(Button.builder(Component.translatable("gui.corpse.items"),
                button -> openItems()).bounds(center - 76, buttonY, 72, 20).build());
        locationButton = addRenderableWidget(Button.builder(Component.translatable("gui.corpse.location"),
                button -> copyTeleportCommand()).bounds(center + 2, buttonY, 72, 20).build());
        nextButton = addRenderableWidget(Button.builder(Component.translatable("gui.corpse.next"),
                button -> changePage(1)).bounds(center + 80, buttonY, 72, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"),
                button -> onClose()).bounds(center - 50, panelTop + PANEL_HEIGHT + 7, 100, 20).build());
        updateButtons();
    }

    private void changePage(int amount) {
        if (deaths.isEmpty()) {
            return;
        }
        index = Math.max(0, Math.min(deaths.size() - 1, index + amount));
        updateButtons();
    }

    private void updateButtons() {
        boolean hasDeaths = !deaths.isEmpty();
        previousButton.active = hasDeaths && index > 0;
        nextButton.active = hasDeaths && index < deaths.size() - 1;
        itemsButton.active = hasDeaths;
        locationButton.active = hasDeaths;
    }

    private void openItems() {
        if (deaths.isEmpty()) {
            return;
        }
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUUID(deaths.get(index).id());
        ClientPlayNetworking.send(CorpseNetworking.OPEN_HISTORY_ITEMS, buf);
    }

    private void copyTeleportCommand() {
        if (deaths.isEmpty() || minecraft == null) {
            return;
        }
        DeathSummary death = deaths.get(index);
        String command = String.format("/execute in %s run tp @s %.2f %.2f %.2f",
                death.dimension(), death.x(), death.y(), death.z());
        minecraft.keyboardHandler.setClipboard(command);
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(Component.translatable("message.corpse.teleport_copied"), false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int center = width / 2;
        int panelTop = (height - PANEL_HEIGHT) / 2;
        int panelLeft = center - PANEL_WIDTH / 2;
        int panelRight = panelLeft + PANEL_WIDTH;
        int panelBottom = panelTop + PANEL_HEIGHT;
        graphics.fill(panelLeft, panelTop, panelRight, panelBottom, PANEL_BORDER_DARK);
        graphics.fill(panelLeft + 1, panelTop + 1, panelRight - 1, panelBottom - 1, PANEL_BORDER_LIGHT);
        graphics.fill(panelLeft + 2, panelTop + 2, panelRight - 2, panelBottom - 2, PANEL_BACKGROUND);
        graphics.fill(panelRight - 3, panelTop + 2, panelRight - 2, panelBottom - 2, PANEL_BORDER_SHADOW);
        graphics.fill(panelLeft + 2, panelBottom - 3, panelRight - 2, panelBottom - 2, PANEL_BORDER_SHADOW);
        drawCenteredNoShadow(graphics, title, center, panelTop + 12, TEXT_PRIMARY);

        if (deaths.isEmpty()) {
            drawCenteredNoShadow(graphics, Component.translatable("gui.corpse.no_deaths"),
                    center, panelTop + 65, TEXT_SECONDARY);
        } else {
            DeathSummary death = deaths.get(index);
            String date = DATE_FORMAT.format(Instant.ofEpochMilli(death.timestamp()).atZone(ZoneId.systemDefault()));
            drawCenteredNoShadow(graphics, Component.literal(date), center, panelTop + 35, TEXT_SECONDARY);
            drawCenteredNoShadow(graphics,
                    Component.translatable("gui.corpse.dimension", death.dimension()),
                    center, panelTop + 51, TEXT_SECONDARY);
            drawCenteredNoShadow(graphics,
                    Component.translatable("gui.corpse.coordinates",
                            Math.round(death.x()), Math.round(death.y()), Math.round(death.z())),
                    center, panelTop + 67, TEXT_SECONDARY);
            drawCenteredNoShadow(graphics, Component.literal(death.cause()),
                    center, panelTop + 83, TEXT_CAUSE);
            drawCenteredNoShadow(graphics,
                    Component.translatable("gui.corpse.page", index + 1, deaths.size()),
                    center, panelTop + 101, TEXT_SECONDARY);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawCenteredNoShadow(GuiGraphics graphics, Component text, int center, int y, int color) {
        graphics.drawString(font, text, center - font.width(text) / 2, y, color, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
