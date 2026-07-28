package dev.fatin.corpse.death;

import dev.fatin.corpse.entity.CorpseEntity;
import dev.fatin.corpse.history.DeathHistoryState;
import dev.fatin.corpse.history.DeathRecord;
import dev.fatin.corpse.registry.CorpseRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CorpseDeathHandler {

    private static final Map<UUID, Long> LAST_CAPTURE_TICK = new ConcurrentHashMap<>();

    public static void onDeath(ServerPlayer player, DamageSource source) {
        if (player.isSpectator()
                || player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            return;
        }

        long gameTime = player.level().getGameTime();
        Long previousTick = LAST_CAPTURE_TICK.put(player.getUUID(), gameTime);
        if (previousTick != null && previousTick == gameTime) {
            return;
        }

        Inventory inventory = player.getInventory();
        NonNullList<ItemStack> captured = NonNullList.withSize(
                CorpseEntity.INVENTORY_SIZE, ItemStack.EMPTY
        );
        for (int slot = 0; slot < Math.min(inventory.getContainerSize(), captured.size()); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(stack)) {
                captured.set(slot, stack.copy());
            }
        }

        String cause = player.getCombatTracker().getDeathMessage().getString();
        DeathRecord record = new DeathRecord(
                UUID.randomUUID(),
                player.getUUID(),
                player.getGameProfile().getName(),
                System.currentTimeMillis(),
                player.level().dimension().location().toString(),
                player.getX(),
                player.getY(),
                player.getZ(),
                cause,
                captured
        );
        DeathHistoryState.get(player.getServer()).add(record);

        CorpseEntity corpse = CorpseRegistry.CORPSE_ENTITY.create(player.level());
        if (corpse != null) {
            corpse.initialize(player, captured);
            corpse.moveTo(player.getX(), player.getY() + 0.1D, player.getZ(), player.getYRot(), 0.0F);
            ((ServerLevel) player.level()).addFreshEntity(corpse);
        }

        inventory.clearContent();
        inventory.setChanged();
        player.containerMenu.broadcastChanges();
    }

    public static void clearCaptureGuard(UUID playerId) {
        LAST_CAPTURE_TICK.remove(playerId);
    }

    private CorpseDeathHandler() {
    }
}
