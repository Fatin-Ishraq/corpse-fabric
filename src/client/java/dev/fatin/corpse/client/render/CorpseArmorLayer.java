package dev.fatin.corpse.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;

final class CorpseArmorLayer extends HumanoidArmorLayer<
        CorpseRenderState,
        HumanoidModel<CorpseRenderState>,
        HumanoidModel<CorpseRenderState>> {

    private final boolean skeletonLayer;

    CorpseArmorLayer(
            RenderLayerParent<CorpseRenderState, HumanoidModel<CorpseRenderState>> parent,
            HumanoidModel<CorpseRenderState> innerModel,
            HumanoidModel<CorpseRenderState> outerModel,
            EquipmentLayerRenderer equipmentRenderer,
            boolean skeletonLayer) {
        super(parent, innerModel, outerModel, equipmentRenderer);
        this.skeletonLayer = skeletonLayer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight,
                       CorpseRenderState state, float yRot, float xRot) {
        if (state.skeleton == skeletonLayer) {
            super.render(poseStack, buffers, packedLight, state, yRot, xRot);
        }
    }
}