package dev.fatin.corpse.gametest;

import dev.fatin.corpse.CorpseFabric;
import dev.fatin.corpse.death.CorpseDeathHandler;
import dev.fatin.corpse.entity.CorpseEntity;
import dev.fatin.corpse.menu.CorpseMenu;
import dev.fatin.corpse.registry.CorpseRegistry;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class CorpseGameTests implements FabricGameTest {

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void capturesInventoryWithoutDrops(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 3));
        player.getInventory().setItem(36, new ItemStack(Items.IRON_BOOTS));
        player.getInventory().selected = 2;
        player.getInventory().setItem(2, new ItemStack(Items.DIAMOND_SWORD));
        player.getInventory().setItem(39, new ItemStack(Items.IRON_HELMET));
        player.getInventory().setItem(40, new ItemStack(Items.SHIELD));

        CorpseDeathHandler.onDeath(player, player.damageSources().generic());

        helper.assertTrue(player.getInventory().isEmpty(), "player inventory should be cleared");
        List<CorpseEntity> corpses = nearbyCorpses(helper, player);
        helper.assertTrue(corpses.size() == 1, "exactly one corpse should spawn");
        CorpseEntity corpse = corpses.get(0);
        helper.assertTrue(corpse.getItem(0).is(Items.DIAMOND), "corpse should contain diamonds in slot 0");
        helper.assertTrue(corpse.getItem(0).getCount() == 3, "corpse should preserve item count");
        helper.assertTrue(corpse.getItem(36).is(Items.IRON_BOOTS), "corpse should preserve armor slot");
        helper.assertTrue(corpse.getItemBySlot(EquipmentSlot.MAINHAND).is(Items.DIAMOND_SWORD),
                "death capture should preserve selected main-hand equipment");
        helper.assertTrue(corpse.getItemBySlot(EquipmentSlot.HEAD).is(Items.IRON_HELMET),
                "death capture should expose worn armor");
        helper.assertTrue(corpse.getItemBySlot(EquipmentSlot.OFFHAND).is(Items.SHIELD),
                "death capture should expose offhand equipment");
        helper.assertItemEntityCountIs(Items.DIAMOND, player.blockPosition(), 4.0D, 0);
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void lethalDamageUsesDeathMixin(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND_SWORD));
        player.die(player.damageSources().generic());
        helper.runAfterDelay(1, () -> {
            List<CorpseEntity> corpses = nearbyCorpses(helper, player);
            helper.assertTrue(corpses.size() == 1, "death mixin should create exactly one corpse");
            helper.assertTrue(corpses.get(0).getItem(0).is(Items.DIAMOND_SWORD),
                    "death mixin should move the inventory into the corpse");
            helper.assertItemEntityCountIs(Items.DIAMOND_SWORD, player.blockPosition(), 4.0D, 0);
            helper.succeed();
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void excludesCurseOfVanishing(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        ItemStack cursedSword = new ItemStack(Items.DIAMOND_SWORD);
        cursedSword.enchant(
                helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.VANISHING_CURSE),
                1
        );
        player.getInventory().setItem(0, cursedSword);

        CorpseDeathHandler.onDeath(player, player.damageSources().generic());

        List<CorpseEntity> corpses = nearbyCorpses(helper, player);
        helper.assertTrue(corpses.size() == 1, "a corpse should still spawn for cursed inventory");
        helper.assertTrue(corpses.get(0).getItem(0).isEmpty(),
                "Curse of Vanishing items must not be stored in the corpse");
        helper.assertItemEntityCountIs(Items.DIAMOND_SWORD, player.blockPosition(), 4.0D, 0);
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void respectsKeepInventory(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        player.getInventory().setItem(0, new ItemStack(Items.EMERALD, 2));
        GameRules.BooleanValue keepInventory = helper.getLevel().getGameRules().getRule(GameRules.RULE_KEEPINVENTORY);
        boolean previous = keepInventory.get();
        keepInventory.set(true, helper.getLevel().getServer());
        try {
            CorpseDeathHandler.onDeath(player, player.damageSources().generic());
            helper.assertTrue(player.getInventory().getItem(0).is(Items.EMERALD),
                    "keepInventory should retain the player inventory");
            helper.assertTrue(nearbyCorpses(helper, player).isEmpty(),
                    "keepInventory should prevent corpse creation");
        } finally {
            keepInventory.set(previous, helper.getLevel().getServer());
        }
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void transfersBackToOriginalSlots(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        CorpseEntity corpse = CorpseRegistry.CORPSE_ENTITY.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        helper.assertTrue(corpse != null, "corpse entity should be creatable");
        corpse.setOwner(player.getUUID(), player.getGameProfile().getName());
        corpse.setItem(0, new ItemStack(Items.GOLD_INGOT, 5));
        corpse.setItem(36, new ItemStack(Items.DIAMOND_BOOTS));
        Vec3 position = player.position();
        corpse.moveTo(position.x, position.y, position.z, 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(corpse);

        CorpseMenu menu = new CorpseMenu(1, player.getInventory(), corpse);
        helper.assertTrue(menu.clickMenuButton(player, CorpseMenu.TRANSFER_BUTTON_ID),
                "transfer button should be handled");
        helper.assertTrue(player.getInventory().getItem(0).is(Items.GOLD_INGOT),
                "main inventory should return to its original slot");
        helper.assertTrue(player.getInventory().getItem(36).is(Items.DIAMOND_BOOTS),
                "armor should return to its original slot");
        helper.assertTrue(corpse.isEmpty(), "corpse should be empty after transfer");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void equipmentSlotsMirrorInventory(GameTestHelper helper) {
        CorpseEntity corpse = CorpseRegistry.CORPSE_ENTITY.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        helper.assertTrue(corpse != null, "corpse entity should be creatable");
        corpse.setSelectedSlot(2);
        corpse.setItem(2, new ItemStack(Items.DIAMOND_SWORD));
        corpse.setItem(36, new ItemStack(Items.IRON_BOOTS));
        corpse.setItem(37, new ItemStack(Items.IRON_LEGGINGS));
        corpse.setItem(38, new ItemStack(Items.IRON_CHESTPLATE));
        corpse.setItem(39, new ItemStack(Items.IRON_HELMET));
        corpse.setItem(40, new ItemStack(Items.SHIELD));

        helper.assertTrue(corpse.getItemBySlot(EquipmentSlot.MAINHAND).is(Items.DIAMOND_SWORD),
                "selected hotbar item should be exposed as main-hand equipment");
        helper.assertTrue(corpse.getItemBySlot(EquipmentSlot.OFFHAND).is(Items.SHIELD),
                "offhand inventory should be exposed as offhand equipment");
        helper.assertTrue(corpse.getItemBySlot(EquipmentSlot.FEET).is(Items.IRON_BOOTS),
                "boots should be exposed as feet equipment");
        helper.assertTrue(corpse.getItemBySlot(EquipmentSlot.LEGS).is(Items.IRON_LEGGINGS),
                "leggings should be exposed as leg equipment");
        helper.assertTrue(corpse.getItemBySlot(EquipmentSlot.CHEST).is(Items.IRON_CHESTPLATE),
                "chestplate should be exposed as chest equipment");
        helper.assertTrue(corpse.getItemBySlot(EquipmentSlot.HEAD).is(Items.IRON_HELMET),
                "helmet should be exposed as head equipment");

        corpse.removeItemNoUpdate(39);
        helper.assertTrue(corpse.getItemBySlot(EquipmentSlot.HEAD).isEmpty(),
                "removing a helmet should immediately clear visible head equipment");
        corpse.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        helper.assertTrue(corpse.getItem(38).isEmpty(),
                "equipment packet updates should map back to the corpse inventory");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void persistsInventoryAndOwner(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        CorpseEntity source = CorpseRegistry.CORPSE_ENTITY.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        CorpseEntity restored = CorpseRegistry.CORPSE_ENTITY.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        helper.assertTrue(source != null && restored != null, "corpse entities should be creatable");
        source.setOwner(player.getUUID(), player.getGameProfile().getName());
        source.setItem(4, new ItemStack(Items.NETHERITE_SCRAP, 7));
        source.setSelectedSlot(4);
        source.setItem(39, new ItemStack(Items.DIAMOND_HELMET));
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        source.addAdditionalSaveData(tag);
        restored.readAdditionalSaveData(tag);

        helper.assertTrue(restored.getOwnerId().orElseThrow().equals(player.getUUID()),
                "owner UUID should survive serialization");
        helper.assertTrue(restored.getItem(4).is(Items.NETHERITE_SCRAP),
                "inventory item should survive serialization");
        helper.assertTrue(restored.getItem(4).getCount() == 7,
                "inventory count should survive serialization");
        helper.assertTrue(restored.getSelectedSlot() == 4,
                "selected hotbar slot should survive serialization");
        helper.assertTrue(restored.getItemBySlot(EquipmentSlot.HEAD).is(Items.DIAMOND_HELMET),
                "visible armor mapping should survive serialization");
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void entersSkeletonStage(GameTestHelper helper) {
        CorpseEntity corpse = CorpseRegistry.CORPSE_ENTITY.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        helper.assertTrue(corpse != null, "corpse entity should be creatable");
        int previousTicks = CorpseFabric.CONFIG.skeletonTicks;
        CorpseFabric.CONFIG.skeletonTicks = 0;
        try {
            corpse.tick();
            helper.assertTrue(corpse.isSkeleton(), "corpse should enter skeleton stage at configured age");
        } finally {
            CorpseFabric.CONFIG.skeletonTicks = previousTicks;
        }
        helper.succeed();
    }

    private static ServerPlayer createPlayer(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos position = helper.absolutePos(new BlockPos(2, 1, 2));
        player.setPos(position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D);
        return player;
    }

    private static List<CorpseEntity> nearbyCorpses(GameTestHelper helper, ServerPlayer player) {
        return helper.getLevel().getEntities(CorpseRegistry.CORPSE_ENTITY,
                player.getBoundingBox().inflate(4.0D), Entity::isAlive);
    }
}
