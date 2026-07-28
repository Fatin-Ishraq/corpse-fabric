package dev.fatin.corpse.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.fatin.corpse.CorpseFabric;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CorpseConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean ownerOnlyAccess = true;
    public boolean skeletonAccessibleByEveryone = true;
    public int emptyDespawnTicks = 600;
    public int fullDespawnTicks = -1;
    public int skeletonTicks = 72_000;
    public int maxHistoryEntriesPerPlayer = 50;
    public boolean spawnFaceDown = false;

    public static CorpseConfig load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("corpse.json");
        CorpseConfig config = new CorpseConfig();

        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                CorpseConfig loaded = GSON.fromJson(reader, CorpseConfig.class);
                if (loaded != null) {
                    config = loaded;
                }
            } catch (IOException | RuntimeException exception) {
                CorpseFabric.LOGGER.error("Could not read {}; defaults will be used", path, exception);
            }
        }

        config.validate();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException exception) {
            CorpseFabric.LOGGER.error("Could not write {}", path, exception);
        }
        return config;
    }

    private void validate() {
        emptyDespawnTicks = Math.max(-1, emptyDespawnTicks);
        fullDespawnTicks = Math.max(-1, fullDespawnTicks);
        skeletonTicks = Math.max(0, skeletonTicks);
        maxHistoryEntriesPerPlayer = Math.max(1, maxHistoryEntriesPerPlayer);
    }
}
