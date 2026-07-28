package dev.fatin.corpse.network;

import dev.fatin.corpse.history.DeathHistoryState;
import dev.fatin.corpse.history.DeathRecord;
import dev.fatin.corpse.history.DeathSummary;
import dev.fatin.corpse.menu.HistoryInventoryFactory;
import dev.fatin.corpse.registry.CorpseRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;

public final class CorpseNetworking {

    public static final ResourceLocation OPEN_HISTORY = CorpseRegistry.id("open_history");
    public static final ResourceLocation HISTORY_RESPONSE = CorpseRegistry.id("history_response");
    public static final ResourceLocation OPEN_HISTORY_ITEMS = CorpseRegistry.id("open_history_items");

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(OPEN_HISTORY,
                (server, player, handler, buf, responseSender) ->
                        server.execute(() -> sendHistory(player, player)));

        ServerPlayNetworking.registerGlobalReceiver(OPEN_HISTORY_ITEMS,
                (server, player, handler, buf, responseSender) -> {
                    UUID deathId = buf.readUUID();
                    server.execute(() -> openHistoryItems(player, deathId));
                });
    }

    public static void sendHistory(ServerPlayer viewer, ServerPlayer target) {
        List<DeathRecord> records = DeathHistoryState.get(viewer.getServer()).getForPlayer(target.getUUID());
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUtf(target.getGameProfile().getName());
        buf.writeVarInt(records.size());
        for (DeathRecord record : records) {
            DeathSummary.from(record).write(buf);
        }
        ServerPlayNetworking.send(viewer, HISTORY_RESPONSE, buf);
    }

    private static void openHistoryItems(ServerPlayer player, UUID deathId) {
        DeathHistoryState.get(player.getServer()).find(deathId).ifPresent(record -> {
            boolean allowed = record.playerId().equals(player.getUUID()) || player.hasPermissions(2);
            if (!allowed) {
                return;
            }
            player.openMenu(new HistoryInventoryFactory(record, player.getAbilities().instabuild));
        });
    }

    private CorpseNetworking() {
    }
}
