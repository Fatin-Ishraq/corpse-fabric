package dev.fatin.corpse.registry;

import dev.fatin.corpse.CorpseFabric;
import dev.fatin.corpse.entity.CorpseEntity;
import dev.fatin.corpse.menu.CorpseMenu;
import dev.fatin.corpse.menu.HistoryMenu;
import dev.fatin.corpse.menu.HistoryScreenData;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;

public final class CorpseRegistry {

    public static final ResourceKey<EntityType<?>> CORPSE_ENTITY_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, id("corpse"));

    public static final EntityType<CorpseEntity> CORPSE_ENTITY = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            CORPSE_ENTITY_KEY,
            FabricEntityTypeBuilder.create(MobCategory.MISC, CorpseEntity::new)
                    .dimensions(EntityDimensions.scalable(2.0F, 0.5F))
                    .fireImmune()
                    .trackRangeChunks(10)
                    .trackedUpdateRate(1)
                    .build(CORPSE_ENTITY_KEY)
    );

    public static final MenuType<CorpseMenu> CORPSE_MENU = Registry.register(
            BuiltInRegistries.MENU,
            id("corpse_inventory"),
            new ExtendedScreenHandlerType<>(CorpseMenu::fromNetwork, ByteBufCodecs.VAR_INT)
    );

    public static final MenuType<HistoryMenu> HISTORY_MENU = Registry.register(
            BuiltInRegistries.MENU,
            id("death_history_items"),
            new ExtendedScreenHandlerType<>(HistoryMenu::fromNetwork, HistoryScreenData.STREAM_CODEC)
    );
    public static void register() {
        FabricDefaultAttributeRegistry.register(CORPSE_ENTITY, CorpseEntity.createAttributes());
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(CorpseFabric.MOD_ID, path);
    }

    private CorpseRegistry() {
    }
}
