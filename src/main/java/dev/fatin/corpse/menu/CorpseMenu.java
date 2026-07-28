package dev.fatin.corpse.menu;

import dev.fatin.corpse.entity.CorpseEntity;
import dev.fatin.corpse.registry.CorpseRegistry;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class CorpseMenu extends AbstractContainerMenu {

    public static final int TRANSFER_BUTTON_ID = 0;
    private static final int CORPSE_SLOT_COUNT = CorpseEntity.INVENTORY_SIZE;

    private final Container corpseInventory;
    private final CorpseEntity corpse;
    private final Inventory playerInventory;

    public static CorpseMenu fromNetwork(int containerId, Inventory inventory, Integer entityId) {
        Entity entity = inventory.player.level().getEntity(entityId);
        if (entity instanceof CorpseEntity corpseEntity) {
            return new CorpseMenu(containerId, inventory, corpseEntity);
        }
        return new CorpseMenu(containerId, inventory, new SimpleContainer(CORPSE_SLOT_COUNT), null);
    }

    public CorpseMenu(int containerId, Inventory playerInventory, CorpseEntity corpse) {
        this(containerId, playerInventory, corpse, corpse);
    }

    private CorpseMenu(int containerId, Inventory playerInventory, Container corpseInventory, CorpseEntity corpse) {
        super(CorpseRegistry.CORPSE_MENU, containerId);
        this.corpseInventory = corpseInventory;
        this.corpse = corpse;
        this.playerInventory = playerInventory;
        corpseInventory.startOpen(playerInventory.player);

        for (int slot = 0; slot < CORPSE_SLOT_COUNT; slot++) {
            int column = slot % 9;
            int row = slot / 9;
            addSlot(new CorpseSlot(corpseInventory, slot, 8 + column * 18, 18 + row * 18));
        }

        int playerInventoryY = 140;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9,
                        8 + column * 18, playerInventoryY + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, playerInventoryY + 58));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return corpse == null || corpse.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < CORPSE_SLOT_COUNT) {
            if (!moveItemStackTo(stack, CORPSE_SLOT_COUNT, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, CORPSE_SLOT_COUNT, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != TRANSFER_BUTTON_ID || corpse == null || !corpse.canPlayerAccess(player)) {
            return false;
        }

        for (int index = 0; index < CORPSE_SLOT_COUNT; index++) {
            ItemStack corpseStack = corpseInventory.getItem(index);
            if (corpseStack.isEmpty()) {
                continue;
            }

            ItemStack existing = playerInventory.getItem(index);
            if (existing.isEmpty()) {
                playerInventory.setItem(index, corpseStack.copy());
                corpseInventory.setItem(index, ItemStack.EMPTY);
                continue;
            }

            ItemStack remainder = corpseStack.copy();
            if (playerInventory.add(remainder)) {
                corpseInventory.setItem(index, ItemStack.EMPTY);
            } else {
                corpseInventory.setItem(index, remainder);
            }
        }

        corpseInventory.setChanged();
        playerInventory.setChanged();
        broadcastChanges();
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        corpseInventory.stopOpen(player);
    }

    private static final class CorpseSlot extends Slot {
        private CorpseSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
