package dev.fatin.corpse.network;

import dev.fatin.corpse.history.DeathHistoryState;
import dev.fatin.corpse.history.DeathRecord;
import dev.fatin.corpse.history.DeathSummary;
import dev.fatin.corpse.menu.HistoryInventoryFactory;
import dev.fatin.corpse.registry.CorpseRegistry;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CorpseNetworking {

    public static void registerPayloads() {
        PayloadTypeRegistry.playC2S().register(OpenHistoryPayload.TYPE, OpenHistoryPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(OpenHistoryItemsPayload.TYPE, OpenHistoryItemsPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(HistoryResponsePayload.TYPE, HistoryResponsePayload.CODEC);
    }

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(OpenHistoryPayload.TYPE,
                (payload, context) -> sendHistory(context.player(), context.player()));

        ServerPlayNetworking.registerGlobalReceiver(OpenHistoryItemsPayload.TYPE,
                (payload, context) -> openHistoryItems(context.player(), payload.deathId()));
    }

    public static void sendHistory(ServerPlayer viewer, ServerPlayer target) {
        List<DeathSummary> deaths = DeathHistoryState.get(viewer.getServer()).getForPlayer(target.getUUID())
                .stream()
                .map(DeathSummary::from)
                .toList();
        ServerPlayNetworking.send(viewer,
                new HistoryResponsePayload(target.getGameProfile().getName(), deaths));
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

    public record OpenHistoryPayload() implements CustomPacketPayload {
        public static final OpenHistoryPayload INSTANCE = new OpenHistoryPayload();
        public static final Type<OpenHistoryPayload> TYPE =
                new Type<>(CorpseRegistry.id("open_history"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenHistoryPayload> CODEC =
                StreamCodec.unit(INSTANCE);

        @Override
        public Type<OpenHistoryPayload> type() {
            return TYPE;
        }
    }

    public record OpenHistoryItemsPayload(UUID deathId) implements CustomPacketPayload {
        public static final Type<OpenHistoryItemsPayload> TYPE =
                new Type<>(CorpseRegistry.id("open_history_items"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenHistoryItemsPayload> CODEC =
                CustomPacketPayload.codec(OpenHistoryItemsPayload::write, OpenHistoryItemsPayload::read);

        private void write(RegistryFriendlyByteBuf buf) {
            buf.writeUUID(deathId);
        }

        private static OpenHistoryItemsPayload read(RegistryFriendlyByteBuf buf) {
            return new OpenHistoryItemsPayload(buf.readUUID());
        }

        @Override
        public Type<OpenHistoryItemsPayload> type() {
            return TYPE;
        }
    }

    public record HistoryResponsePayload(String playerName, List<DeathSummary> deaths)
            implements CustomPacketPayload {
        public static final Type<HistoryResponsePayload> TYPE =
                new Type<>(CorpseRegistry.id("history_response"));
        public static final StreamCodec<RegistryFriendlyByteBuf, HistoryResponsePayload> CODEC =
                CustomPacketPayload.codec(HistoryResponsePayload::write, HistoryResponsePayload::read);

        public HistoryResponsePayload {
            deaths = List.copyOf(deaths);
        }

        private void write(RegistryFriendlyByteBuf buf) {
            buf.writeUtf(playerName);
            buf.writeVarInt(deaths.size());
            for (DeathSummary death : deaths) {
                death.write(buf);
            }
        }

        private static HistoryResponsePayload read(RegistryFriendlyByteBuf buf) {
            String playerName = buf.readUtf();
            int count = buf.readVarInt();
            List<DeathSummary> deaths = new ArrayList<>(count);
            for (int index = 0; index < count; index++) {
                deaths.add(DeathSummary.read(buf));
            }
            return new HistoryResponsePayload(playerName, deaths);
        }

        @Override
        public Type<HistoryResponsePayload> type() {
            return TYPE;
        }
    }

    private CorpseNetworking() {
    }
}