package dev.fatin.corpse.entity;

import dev.fatin.corpse.CorpseFabric;
import dev.fatin.corpse.menu.CorpseMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.world.ContainerHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public final class CorpseEntity extends PathfinderMob implements Container, ExtendedScreenHandlerFactory {

    public static final int INVENTORY_SIZE = 41;

    private static final EntityDataAccessor<Optional<UUID>> OWNER_ID = SynchedEntityData.defineId(
            CorpseEntity.class, EntityDataSerializers.OPTIONAL_UUID
    );
    private static final EntityDataAccessor<String> OWNER_NAME = SynchedEntityData.defineId(
            CorpseEntity.class, EntityDataSerializers.STRING
    );
    private static final EntityDataAccessor<Boolean> SKELETON = SynchedEntityData.defineId(
            CorpseEntity.class, EntityDataSerializers.BOOLEAN
    );

    private static final EntityDataAccessor<Boolean> FACE_DOWN = SynchedEntityData.defineId(
            CorpseEntity.class, EntityDataSerializers.BOOLEAN
    );

    private NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private long createdGameTime;
    private int emptyTicks;

    public CorpseEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        setNoAi(true);
        setNoGravity(true);
        setInvulnerable(true);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(OWNER_ID, Optional.empty());
        entityData.define(OWNER_NAME, "");
        entityData.define(SKELETON, false);
        entityData.define(FACE_DOWN, false);
    }

    public void initialize(ServerPlayer owner, NonNullList<ItemStack> capturedItems) {
        setOwner(owner.getUUID(), owner.getGameProfile().getName());
        createdGameTime = level().getGameTime();
        entityData.set(FACE_DOWN, CorpseFabric.CONFIG.spawnFaceDown);
        for (int index = 0; index < Math.min(items.size(), capturedItems.size()); index++) {
            items.set(index, capturedItems.get(index).copy());
        }
        setYRot(owner.getYRot());
        setYBodyRot(owner.getYRot());
        setYHeadRot(owner.getYRot());
    }

    public void setOwner(UUID ownerId, String ownerName) {
        entityData.set(OWNER_ID, Optional.of(ownerId));
        entityData.set(OWNER_NAME, ownerName);
    }

    public Optional<UUID> getOwnerId() {
        return entityData.get(OWNER_ID);
    }

    public String getOwnerName() {
        return entityData.get(OWNER_NAME);
    }

    public boolean isSkeleton() {
        return entityData.get(SKELETON);
    }

    public boolean isFaceDown() {
        return entityData.get(FACE_DOWN);
    }

    @Override
    public void tick() {
        super.tick();
        setNoGravity(true);
        setDeltaMovement(0.0D, 0.0D, 0.0D);

        if (!level().isClientSide) {
            long age = Math.max(0L, level().getGameTime() - createdGameTime);
            if (!isSkeleton() && age >= CorpseFabric.CONFIG.skeletonTicks) {
                entityData.set(SKELETON, true);
            }

            if (isEmpty()) {
                emptyTicks++;
                if (CorpseFabric.CONFIG.emptyDespawnTicks >= 0
                        && emptyTicks >= CorpseFabric.CONFIG.emptyDespawnTicks) {
                    discard();
                    return;
                }
            } else {
                emptyTicks = 0;
                if (CorpseFabric.CONFIG.fullDespawnTicks >= 0
                        && age >= CorpseFabric.CONFIG.fullDespawnTicks) {
                    discard();
                    return;
                }
            }

            int minimumY = level().getMinBuildHeight() + 1;
            if (getY() < minimumY) {
                setPos(getX(), minimumY, getZ());
            }
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!canPlayerAccess(player)) {
            if (!level().isClientSide) {
                player.displayClientMessage(Component.translatable("message.corpse.not_owner"), true);
            }
            return InteractionResult.FAIL;
        }
        if (!level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(this);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    public boolean canPlayerAccess(Player player) {
        if (!CorpseFabric.CONFIG.ownerOnlyAccess) {
            return true;
        }
        if (isSkeleton() && CorpseFabric.CONFIG.skeletonAccessibleByEveryone) {
            return true;
        }
        return getOwnerId().map(id -> id.equals(player.getUUID())).orElse(false)
                || player.hasPermissions(2);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        buf.writeVarInt(getId());
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.corpse.inventory", getOwnerName());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        if (!canPlayerAccess(player)) {
            return null;
        }
        return new CorpseMenu(containerId, playerInventory, this);
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < items.size() ? items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack result = ContainerHelper.takeItem(items, slot);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= items.size()) {
            return;
        }
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return isAlive() && distanceToSqr(player) <= 64.0D
                && (level().isClientSide || canPlayerAccess(player));
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        getOwnerId().ifPresent(id -> tag.putUUID("Owner", id));
        tag.putString("OwnerName", getOwnerName());
        tag.putLong("CreatedGameTime", createdGameTime);
        tag.putInt("EmptyTicks", emptyTicks);
        tag.putBoolean("Skeleton", isSkeleton());
        tag.putBoolean("FaceDown", isFaceDown());
        ContainerHelper.saveAllItems(tag, items);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) {
            setOwner(tag.getUUID("Owner"), tag.getString("OwnerName"));
        }
        createdGameTime = tag.getLong("CreatedGameTime");
        emptyTicks = tag.getInt("EmptyTicks");
        entityData.set(SKELETON, tag.getBoolean("Skeleton"));
        entityData.set(FACE_DOWN, tag.getBoolean("FaceDown"));
        items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
        setNoAi(true);
        setNoGravity(true);
        setInvulnerable(true);
        setPersistenceRequired();
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}
