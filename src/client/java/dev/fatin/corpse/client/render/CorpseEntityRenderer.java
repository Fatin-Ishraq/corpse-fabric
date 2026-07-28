package dev.fatin.corpse.client.render;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.fatin.corpse.entity.CorpseEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public final class CorpseEntityRenderer extends HumanoidMobRenderer<
        CorpseEntity,
        CorpseRenderState,
        HumanoidModel<CorpseRenderState>> {

    private final HumanoidModel<CorpseRenderState> playerModel;
    private final SkeletonRenderer skeletonRenderer;

    public CorpseEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new CorpsePlayerModel(context.bakeLayer(ModelLayers.PLAYER)), 0.35F);
        playerModel = model;
        skeletonRenderer = new CorpseSkeletonRenderer(context);
        addLayer(new CorpseArmorLayer(
                this,
                ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel::new),
                context.getEquipmentRenderer(),
                false
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
    public void submit(CorpseRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (state.skeleton) {
            skeletonRenderer.submit(state, poseStack, collector, cameraState);
            return;
        }
        model = playerModel;
        super.submit(state, poseStack, collector, cameraState);
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
    public Identifier getTextureLocation(CorpseRenderState state) {
        return state.texture;
    }

    @Override
    protected boolean shouldShowName(CorpseEntity corpse, double distanceToCameraSq) {
        return false;
    }

    private static final class CorpseSkeletonRenderer extends SkeletonRenderer {

        private CorpseSkeletonRenderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        protected void setupRotations(SkeletonRenderState state, PoseStack poseStack,
                                      float bodyYaw, float scale) {
            if (!(state instanceof CorpseRenderState corpseState)) {
                super.setupRotations(state, poseStack, bodyYaw, scale);
                return;
            }
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
            if (corpseState.faceDown) {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                poseStack.translate(0.0F, -0.78F, 2.01D / 16.0D);
            } else {
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                poseStack.translate(0.0F, -0.78F, -2.01D / 16.0D);
            }
        }
    }

    private static Identifier resolveTexture(CorpseEntity corpse) {
        UUID ownerId = corpse.getOwnerId().orElse(null);
        if (ownerId == null) {
            return DefaultPlayerSkin.getDefaultTexture();
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() != null) {
            PlayerInfo info = minecraft.getConnection().getPlayerInfo(ownerId);
            if (info != null) {
                return info.getSkin().body().texturePath();
            }
        }
        String name = corpse.getOwnerName().isBlank() ? ownerId.toString() : corpse.getOwnerName();
        return minecraft.getSkinManager()
                .createLookup(new GameProfile(ownerId, name), false)
                .get().body().texturePath();
    }
}