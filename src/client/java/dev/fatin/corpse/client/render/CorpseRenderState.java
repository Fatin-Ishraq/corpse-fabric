package dev.fatin.corpse.client.render;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;

public final class CorpseRenderState extends HumanoidRenderState {

    public boolean skeleton;
    public boolean faceDown;
    public Identifier texture = DefaultPlayerSkin.getDefaultTexture();
}