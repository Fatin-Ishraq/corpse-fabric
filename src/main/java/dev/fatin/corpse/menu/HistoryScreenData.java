package dev.fatin.corpse.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record HistoryScreenData(UUID deathId, String playerName, boolean editable) {

    public static final StreamCodec<RegistryFriendlyByteBuf, HistoryScreenData> STREAM_CODEC =
            StreamCodec.ofMember(HistoryScreenData::write, HistoryScreenData::read);

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(deathId);
        buf.writeUtf(playerName);
        buf.writeBoolean(editable);
    }

    private static HistoryScreenData read(RegistryFriendlyByteBuf buf) {
        return new HistoryScreenData(buf.readUUID(), buf.readUtf(), buf.readBoolean());
    }
}