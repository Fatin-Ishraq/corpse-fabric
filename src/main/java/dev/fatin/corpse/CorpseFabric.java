package dev.fatin.corpse;

import dev.fatin.corpse.command.CorpseCommands;
import dev.fatin.corpse.config.CorpseConfig;
import dev.fatin.corpse.death.CorpseDeathHandler;
import dev.fatin.corpse.network.CorpseNetworking;
import dev.fatin.corpse.registry.CorpseRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CorpseFabric implements ModInitializer {

    public static final String MOD_ID = "corpse";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static CorpseConfig CONFIG;

    @Override
    public void onInitialize() {
        CONFIG = CorpseConfig.load();
        CorpseRegistry.register();
        CorpseNetworking.registerServer();
        CorpseCommands.register();
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
                CorpseDeathHandler.clearCaptureGuard(newPlayer.getUUID()));
        LOGGER.info("Initializing Corpse for Fabric");
    }
}
