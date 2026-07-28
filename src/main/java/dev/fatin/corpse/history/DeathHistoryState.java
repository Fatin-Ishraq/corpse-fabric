package dev.fatin.corpse.history;

import com.mojang.serialization.Codec;
import dev.fatin.corpse.CorpseFabric;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class DeathHistoryState extends SavedData {

    private static final String DATA_NAME = "corpse_death_history";
    private static final Codec<DeathHistoryState> CODEC = DeathRecord.CODEC.listOf()
            .fieldOf("Deaths")
            .codec()
            .xmap(DeathHistoryState::new, DeathHistoryState::recordsForSave);
    private static final SavedDataType<DeathHistoryState> TYPE = new SavedDataType<>(
            Identifier.withDefaultNamespace(DATA_NAME), DeathHistoryState::new, CODEC, DataFixTypes.LEVEL
    );

    private final List<DeathRecord> records = new ArrayList<>();

    public DeathHistoryState() {
    }

    private DeathHistoryState(List<DeathRecord> records) {
        records.stream().map(DeathRecord::copy).forEach(this.records::add);
    }

    public static DeathHistoryState get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public synchronized void add(DeathRecord record) {
        records.add(record.copy());
        trim(record.playerId());
        setDirty();
    }

    public synchronized List<DeathRecord> getForPlayer(UUID playerId) {
        return records.stream()
                .filter(record -> record.playerId().equals(playerId))
                .sorted(Comparator.comparingLong(DeathRecord::timestamp).reversed())
                .map(DeathRecord::copy)
                .toList();
    }

    public synchronized Optional<DeathRecord> find(UUID id) {
        return records.stream()
                .filter(record -> record.id().equals(id))
                .findFirst()
                .map(DeathRecord::copy);
    }

    private void trim(UUID playerId) {
        List<DeathRecord> playerRecords = records.stream()
                .filter(record -> record.playerId().equals(playerId))
                .sorted(Comparator.comparingLong(DeathRecord::timestamp).reversed())
                .toList();
        int limit = CorpseFabric.CONFIG.maxHistoryEntriesPerPlayer;
        if (playerRecords.size() <= limit) {
            return;
        }
        records.removeAll(playerRecords.subList(limit, playerRecords.size()));
    }

    private synchronized List<DeathRecord> recordsForSave() {
        return records.stream().map(DeathRecord::copy).toList();
    }
}