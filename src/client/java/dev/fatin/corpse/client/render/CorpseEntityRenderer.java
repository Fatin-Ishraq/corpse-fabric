package dev.fatin.corpse.client.render;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.fatin.corpse.entity.CorpseEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public final class CorpseEntityRenderer extends MobRenderer<CorpseEntity, HumanoidModel<CorpseEntity>> {

    private static final ResourceLocation SKELETON_TEXTURE =
            new ResourceLocation("textures/entity/skeleton/skeleton.png");

    private final PlayerModel<CorpseEntity> playerModel;
    private final HumanoidModel<CorpseEntity> skeletonModel;

    public CorpseEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.35F);
        this.playerModel = (PlayerModel<CorpseEntity>) this.model;
        this.skeletonModel = new HumanoidModel<>(context.bakeLayer(ModelLayers.SKELETON));
        addLayer(new CorpseArmorLayer(
                this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager(),
                false
        ));
        addLayer(new CorpseArmorLayer(
                this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.SKELETON_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.SKELETON_OUTER_ARMOR)),
                context.getModelManager(),
                true
        ));
        addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
        addLayer(new ElytraLayer<>(this, context.getModelSet()));
    }

    @Override
    public void render(CorpseEntity corpse, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        model = corpse.isSkeleton() ? skeletonModel : playerModel;
        super.render(corpse, yaw, partialTick, poseStack, buffers, packedLight);
    }

    @Override
    protected void setupRotations(CorpseEntity corpse, PoseStack poseStack,
                                  float ageInTicks, float bodyYaw, float partialTick) {
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
        poseStack.mulPose(Axis.ZP.rotationDegrees(corpse.isFaceDown() ? -90.0F : 90.0F));
        poseStack.translate(0.0F, -0.78F, 0.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(CorpseEntity corpse) {
        if (corpse.isSkeleton()) {
            return SKELETON_TEXTURE;
        }
        UUID ownerId = corpse.getOwnerId().orElse(null);
        if (ownerId == null) {
            return DefaultPlayerSkin.getDefaultSkin();
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() != null) {
            PlayerInfo info = minecraft.getConnection().getPlayerInfo(ownerId);
            if (info != null) {
                return info.getSkinLocation();
            }
        }
        String name = corpse.getOwnerName().isBlank() ? ownerId.toString() : corpse.getOwnerName();
        return minecraft.getSkinManager().getInsecureSkinLocation(new GameProfile(ownerId, name));
    }

    @Override
    protected boolean shouldShowName(CorpseEntity corpse) {
        return false;
    }
}
