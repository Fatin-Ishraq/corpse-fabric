package dev.fatin.corpse.history;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.fatin.corpse.entity.CorpseEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
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

    private static final Codec<NonNullList<ItemStack>> INVENTORY_CODEC =
            ItemStackWithSlot.CODEC.listOf()
                    .fieldOf("Items")
                    .codec()
                    .xmap(DeathRecord::fromSlots, DeathRecord::toSlots);

    public static final Codec<DeathRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("Id").forGetter(DeathRecord::id),
            UUIDUtil.CODEC.fieldOf("PlayerId").forGetter(DeathRecord::playerId),
            Codec.STRING.fieldOf("PlayerName").forGetter(DeathRecord::playerName),
            Codec.LONG.fieldOf("Timestamp").forGetter(DeathRecord::timestamp),
            Codec.STRING.fieldOf("Dimension").forGetter(DeathRecord::dimension),
            Codec.DOUBLE.fieldOf("X").forGetter(DeathRecord::x),
            Codec.DOUBLE.fieldOf("Y").forGetter(DeathRecord::y),
            Codec.DOUBLE.fieldOf("Z").forGetter(DeathRecord::z),
            Codec.STRING.fieldOf("Cause").forGetter(DeathRecord::cause),
            INVENTORY_CODEC.fieldOf("Inventory").forGetter(DeathRecord::items)
    ).apply(instance, DeathRecord::new));

    public DeathRecord copy() {
        NonNullList<ItemStack> copiedItems = NonNullList.withSize(items.size(), ItemStack.EMPTY);
        for (int index = 0; index < items.size(); index++) {
            copiedItems.set(index, items.get(index).copy());
        }
        return new DeathRecord(id, playerId, playerName, timestamp, dimension, x, y, z, cause, copiedItems);
    }

    private static NonNullList<ItemStack> fromSlots(List<ItemStackWithSlot> slots) {
        NonNullList<ItemStack> items = NonNullList.withSize(CorpseEntity.INVENTORY_SIZE, ItemStack.EMPTY);
        for (ItemStackWithSlot entry : slots) {
            if (entry.isValidInContainer(items.size())) {
                items.set(entry.slot(), entry.stack().copy());
            }
        }
        return items;
    }

    private static List<ItemStackWithSlot> toSlots(NonNullList<ItemStack> items) {
        List<ItemStackWithSlot> slots = new ArrayList<>();
        for (int index = 0; index < items.size(); index++) {
            ItemStack stack = items.get(index);
            if (!stack.isEmpty()) {
                slots.add(new ItemStackWithSlot(index, stack.copy()));
            }
        }
        return slots;
    }
}