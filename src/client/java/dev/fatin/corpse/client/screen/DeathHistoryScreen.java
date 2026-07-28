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
        int buttonY = height / 2 + 55;
        previousButton = addRenderableWidget(Button.builder(Component.translatable("gui.corpse.previous"),
                button -> changePage(-1)).bounds(center - 154, buttonY, 72, 20).build());
        itemsButton = addRenderableWidget(Button.builder(Component.translatable("gui.corpse.items"),
                button -> openItems()).bounds(center - 76, buttonY, 72, 20).build());
        locationButton = addRenderableWidget(Button.builder(Component.translatable("gui.corpse.location"),
                button -> copyTeleportCommand()).bounds(center + 2, buttonY, 72, 20).build());
        nextButton = addRenderableWidget(Button.builder(Component.translatable("gui.corpse.next"),
                button -> changePage(1)).bounds(center + 80, buttonY, 72, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"),
                button -> onClose()).bounds(center - 50, buttonY + 26, 100, 20).build());
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
        int panelTop = height / 2 - 75;
        graphics.fill(center - 165, panelTop, center + 165, panelTop + 150, 0xE615191E);
        graphics.fill(center - 164, panelTop + 1, center + 164, panelTop + 149, 0xE6252B32);
        graphics.drawCenteredString(font, title, center, panelTop + 12, 0xF1F5F9);

        if (deaths.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("gui.corpse.no_deaths"),
                    center, panelTop + 65, 0xAEB8C4);
        } else {
            DeathSummary death = deaths.get(index);
            String date = DATE_FORMAT.format(Instant.ofEpochMilli(death.timestamp()).atZone(ZoneId.systemDefault()));
            graphics.drawCenteredString(font, Component.literal(date), center, panelTop + 35, 0xDDE5ED);
            graphics.drawCenteredString(font,
                    Component.translatable("gui.corpse.dimension", death.dimension()),
                    center, panelTop + 51, 0xAEB8C4);
            graphics.drawCenteredString(font,
                    Component.translatable("gui.corpse.coordinates",
                            Math.round(death.x()), Math.round(death.y()), Math.round(death.z())),
                    center, panelTop + 67, 0xAEB8C4);
            graphics.drawCenteredString(font, Component.literal(death.cause()),
                    center, panelTop + 83, 0xD7A7A7);
            graphics.drawCenteredString(font,
                    Component.translatable("gui.corpse.page", index + 1, deaths.size()),
                    center, panelTop + 101, 0x8794A3);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
