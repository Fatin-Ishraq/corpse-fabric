package dev.fatin.corpse.history;

import dev.fatin.corpse.entity.CorpseEntity;
import net.minecraft.world.ContainerHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public record DeathRecord(
        UUID id,
        UUID playerId,
        String playerName,
        long timestamp,
        String dimension,
        double x,
        double y,
        double z,
        String cause,
        NonNullList<ItemStack> items
) {

    public DeathRecord copy() {
        NonNullList<ItemStack> copiedItems = NonNullList.withSize(items.size(), ItemStack.EMPTY);
        for (int index = 0; index < items.size(); index++) {
            copiedItems.set(index, items.get(index).copy());
        }
        return new DeathRecord(id, playerId, playerName, timestamp, dimension, x, y, z, cause, copiedItems);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Id", id);
        tag.putUUID("PlayerId", playerId);
        tag.putString("PlayerName", playerName);
        tag.putLong("Timestamp", timestamp);
        tag.putString("Dimension", dimension);
        tag.putDouble("X", x);
        tag.putDouble("Y", y);
        tag.putDouble("Z", z);
        tag.putString("Cause", cause);
        CompoundTag inventoryTag = new CompoundTag();
        ContainerHelper.saveAllItems(inventoryTag, items);
        tag.put("Inventory", inventoryTag);
        return tag;
    }

    public static DeathRecord load(CompoundTag tag) {
        NonNullList<ItemStack> items = NonNullList.withSize(CorpseEntity.INVENTORY_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag.getCompound("Inventory"), items);
        return new DeathRecord(
                tag.getUUID("Id"),
                tag.getUUID("PlayerId"),
                tag.getString("PlayerName"),
                tag.getLong("Timestamp"),
                tag.getString("Dimension"),
                tag.getDouble("X"),
                tag.getDouble("Y"),
                tag.getDouble("Z"),
                tag.getString("Cause"),
                items
        );
    }
}
