package dev.fatin.corpse.client.render;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

public final class CorpseRenderState extends HumanoidRenderState {

    public boolean skeleton;
    public boolean faceDown;
    public ResourceLocation texture = DefaultPlayerSkin.getDefaultTexture();
}