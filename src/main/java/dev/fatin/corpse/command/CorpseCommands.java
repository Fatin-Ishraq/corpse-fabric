package dev.fatin.corpse.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.fatin.corpse.network.CorpseNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public final class CorpseCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("deathhistory")
                .requires(source -> source.getEntity() instanceof ServerPlayer)
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    CorpseNetworking.sendHistory(player, player);
                    return 1;
                })
                .then(Commands.argument("player", EntityArgument.player())
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> {
                            ServerPlayer viewer = context.getSource().getPlayerOrException();
                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                            CorpseNetworking.sendHistory(viewer, target);
                            return 1;
                        })));
    }

    private CorpseCommands() {
    }
}
