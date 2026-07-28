package dev.fatin.corpse.client;

import dev.fatin.corpse.client.render.CorpseEntityRenderer;
import dev.fatin.corpse.client.screen.CorpseInventoryScreen;
import dev.fatin.corpse.client.screen.DeathHistoryScreen;
import dev.fatin.corpse.client.screen.HistoryInventoryScreen;
import dev.fatin.corpse.history.DeathSummary;
import dev.fatin.corpse.network.CorpseNetworking;
import dev.fatin.corpse.registry.CorpseRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class CorpseFabricClient implements ClientModInitializer {

    private static KeyMapping historyKey;

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(CorpseRegistry.CORPSE_ENTITY, CorpseEntityRenderer::new);
        MenuScreens.register(CorpseRegistry.CORPSE_MENU, CorpseInventoryScreen::new);
        MenuScreens.register(CorpseRegistry.HISTORY_MENU, HistoryInventoryScreen::new);

        historyKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.corpse.death_history", GLFW.GLFW_KEY_U, "key.categories.misc"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (historyKey.consumeClick()) {
                if (client.player != null) {
                    ClientPlayNetworking.send(CorpseNetworking.OPEN_HISTORY, PacketByteBufs.create());
                }
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(CorpseNetworking.HISTORY_RESPONSE,
                (client, handler, buf, responseSender) -> {
                    String playerName = buf.readUtf();
                    int count = buf.readVarInt();
                    List<DeathSummary> deaths = new ArrayList<>(count);
                    for (int index = 0; index < count; index++) {
                        deaths.add(DeathSummary.read(buf));
                    }
                    client.execute(() -> client.setScreen(new DeathHistoryScreen(playerName, deaths)));
                });
    }
}
