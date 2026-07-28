package dev.fatin.corpse.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.fatin.corpse.entity.CorpseEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.resources.model.ModelManager;

final class CorpseArmorLayer extends HumanoidArmorLayer<CorpseEntity, HumanoidModel<CorpseEntity>, HumanoidModel<CorpseEntity>> {

    private final boolean skeletonLayer;

    CorpseArmorLayer(RenderLayerParent<CorpseEntity, HumanoidModel<CorpseEntity>> parent,
                       HumanoidModel<CorpseEntity> innerModel,
                       HumanoidModel<CorpseEntity> outerModel,
                       ModelManager modelManager,
                       boolean skeletonLayer) {
        super(parent, innerModel, outerModel, modelManager);
        this.skeletonLayer = skeletonLayer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight,
                       CorpseEntity corpse, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (corpse.isSkeleton() == skeletonLayer) {
            super.render(poseStack, buffers, packedLight, corpse, limbSwing, limbSwingAmount,
                    partialTick, ageInTicks, netHeadYaw, headPitch);
        }
    }
}
