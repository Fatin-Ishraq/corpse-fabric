package dev.fatin.corpse.client.render;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.fatin.corpse.entity.CorpseEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public final class CorpseEntityRenderer extends HumanoidMobRenderer<
        CorpseEntity,
        CorpseRenderState,
        HumanoidModel<CorpseRenderState>> {

    private static final ResourceLocation SKELETON_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/skeleton/skeleton.png");

    private final HumanoidModel<CorpseRenderState> playerModel;
    private final HumanoidModel<CorpseRenderState> skeletonModel;

    public CorpseEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new CorpsePlayerModel(context.bakeLayer(ModelLayers.PLAYER)), 0.35F);
        playerModel = model;
        skeletonModel = new HumanoidModel<>(context.bakeLayer(ModelLayers.SKELETON));
        addLayer(new CorpseArmorLayer(
                this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getEquipmentRenderer(),
                false
        ));
        addLayer(new CorpseArmorLayer(
                this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.SKELETON_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.SKELETON_OUTER_ARMOR)),
                context.getEquipmentRenderer(),
                true
        ));
    }

    @Override
    public CorpseRenderState createRenderState() {
        return new CorpseRenderState();
    }

    @Override
    public void extractRenderState(CorpseEntity corpse, CorpseRenderState state, float partialTick) {
        super.extractRenderState(corpse, state, partialTick);
        state.skeleton = corpse.isSkeleton();
        state.faceDown = corpse.isFaceDown();
        state.texture = resolveTexture(corpse);
    }

    @Override
    public void render(CorpseRenderState state, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        model = state.skeleton ? skeletonModel : playerModel;
        super.render(state, poseStack, buffers, packedLight);
    }

    @Override
    protected void setupRotations(CorpseRenderState state, PoseStack poseStack,
                                  float bodyYaw, float scale) {
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
        if (state.faceDown) {
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.translate(0.0F, -0.78F, 2.01D / 16.0D);
        } else {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.translate(0.0F, -0.78F, -2.01D / 16.0D);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(CorpseRenderState state) {
        return state.skeleton ? SKELETON_TEXTURE : state.texture;
    }

    @Override
    protected boolean shouldShowName(CorpseEntity corpse, double distanceToCameraSq) {
        return false;
    }

    private static ResourceLocation resolveTexture(CorpseEntity corpse) {
        UUID ownerId = corpse.getOwnerId().orElse(null);
        if (ownerId == null) {
            return DefaultPlayerSkin.getDefaultTexture();
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() != null) {
            PlayerInfo info = minecraft.getConnection().getPlayerInfo(ownerId);
            if (info != null) {
                return info.getSkin().texture();
            }
        }
        String name = corpse.getOwnerName().isBlank() ? ownerId.toString() : corpse.getOwnerName();
        return minecraft.getSkinManager().getInsecureSkin(new GameProfile(ownerId, name)).texture();
    }
}