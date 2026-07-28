package dev.fatin.corpse.history;

import dev.fatin.corpse.CorpseFabric;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class DeathHistoryState extends SavedData {

    private static final String DATA_NAME = "corpse_death_history";
    private static final SavedData.Factory<DeathHistoryState> FACTORY = new SavedData.Factory<>(
            DeathHistoryState::new, DeathHistoryState::load, DataFixTypes.LEVEL
    );
    private final List<DeathRecord> records = new ArrayList<>();

    public static DeathHistoryState get(MinecraftServer server) {
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        if (level == null) {
            throw new IllegalStateException("Overworld is not available");
        }
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public static DeathHistoryState load(CompoundTag tag, HolderLookup.Provider registries) {
        DeathHistoryState state = new DeathHistoryState();
        ListTag list = tag.getList("Deaths", CompoundTag.TAG_COMPOUND);
        for (int index = 0; index < list.size(); index++) {
            state.records.add(DeathRecord.load(list.getCompound(index), registries));
        }
        return state;
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

    @Override
    public synchronized CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (DeathRecord record : records) {
            list.add(record.save(registries));
        }
        tag.put("Deaths", list);
        return tag;
    }
}
