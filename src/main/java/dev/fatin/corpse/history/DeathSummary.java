package dev.fatin.corpse.history;

import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record DeathSummary(
        UUID id,
        long timestamp,
        String dimension,
        double x,
        double y,
        double z,
        String cause
) {

    public static DeathSummary from(DeathRecord record) {
        return new DeathSummary(record.id(), record.timestamp(), record.dimension(),
                record.x(), record.y(), record.z(), record.cause());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(id);
        buf.writeLong(timestamp);
        buf.writeUtf(dimension);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeUtf(cause);
    }

    public static DeathSummary read(FriendlyByteBuf buf) {
        return new DeathSummary(
                buf.readUUID(),
                buf.readLong(),
                buf.readUtf(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readUtf()
        );
    }
}
