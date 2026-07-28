package dev.fatin.corpse.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
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
            ArmorModelSet<HumanoidModel<CorpseRenderState>> modelSet,
            EquipmentLayerRenderer equipmentRenderer,
            boolean skeletonLayer) {
        super(parent, modelSet, equipmentRenderer);
        this.skeletonLayer = skeletonLayer;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                       CorpseRenderState state, float yRot, float xRot) {
        if (state.skeleton == skeletonLayer) {
            super.submit(poseStack, collector, packedLight, state, yRot, xRot);
        }
    }
}