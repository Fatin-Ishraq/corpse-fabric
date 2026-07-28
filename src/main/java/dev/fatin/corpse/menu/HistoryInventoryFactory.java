package dev.fatin.corpse.menu;

import dev.fatin.corpse.history.DeathRecord;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class HistoryInventoryFactory implements ExtendedMenuProvider<HistoryScreenData> {

    private final DeathRecord record;
    private final SimpleContainer inventory;
    private final boolean editable;

    public HistoryInventoryFactory(DeathRecord record, boolean editable) {
        this.record = record;
        this.editable = editable;
        this.inventory = new SimpleContainer(record.items().size());
        for (int index = 0; index < record.items().size(); index++) {
            ItemStack stack = record.items().get(index);
            inventory.setItem(index, stack.copy());
        }
    }

    @Override
    public HistoryScreenData getScreenOpeningData(ServerPlayer player) {
        return new HistoryScreenData(record.id(), record.playerName(), editable);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.corpse.history_items", record.playerName());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new HistoryMenu(containerId, playerInventory, inventory, record.id(), editable);
    }
}
