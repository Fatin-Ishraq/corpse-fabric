package dev.fatin.corpse.menu;

import dev.fatin.corpse.entity.CorpseEntity;
import dev.fatin.corpse.registry.CorpseRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public final class HistoryMenu extends AbstractContainerMenu {

    public static final int RECOVER_BUTTON_ID = 0;
    private static final int HISTORY_SLOT_COUNT = CorpseEntity.INVENTORY_SIZE;

    private final Container historyInventory;
    private final Inventory playerInventory;
    private final UUID deathId;
    private final boolean editable;

    public static HistoryMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buf) {
        UUID deathId = buf.readUUID();
        buf.readUtf();
        boolean editable = buf.readBoolean();
        return new HistoryMenu(containerId, inventory, new SimpleContainer(HISTORY_SLOT_COUNT), deathId, editable);
    }

    public HistoryMenu(int containerId, Inventory playerInventory, Container historyInventory,
                       UUID deathId, boolean editable) {
        super(CorpseRegistry.HISTORY_MENU, containerId);
        this.historyInventory = historyInventory;
        this.playerInventory = playerInventory;
        this.deathId = deathId;
        this.editable = editable;
        historyInventory.startOpen(playerInventory.player);

        for (int slot = 0; slot < HISTORY_SLOT_COUNT; slot++) {
            int column = slot % 9;
            int row = slot / 9;
            addSlot(new HistorySlot(historyInventory, slot, 8 + column * 18, 18 + row * 18, editable));
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

    public boolean isEditable() {
        return editable;
    }

    public UUID getDeathId() {
        return deathId;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (!editable) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < HISTORY_SLOT_COUNT) {
            if (!moveItemStackTo(stack, HISTORY_SLOT_COUNT, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
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
        if (id != RECOVER_BUTTON_ID || !editable || !player.getAbilities().instabuild) {
            return false;
        }
        for (int index = 0; index < HISTORY_SLOT_COUNT; index++) {
            ItemStack stack = historyInventory.getItem(index);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack copy = stack.copy();
            if (!playerInventory.add(copy) && !copy.isEmpty()) {
                player.drop(copy, false);
            }
        }
        playerInventory.setChanged();
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        historyInventory.stopOpen(player);
    }

    private static final class HistorySlot extends Slot {
        private final boolean editable;

        private HistorySlot(Container container, int slot, int x, int y, boolean editable) {
            super(container, slot, x, y);
            this.editable = editable;
        }

        @Override
        public boolean mayPickup(Player player) {
            return editable && player.getAbilities().instabuild;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
