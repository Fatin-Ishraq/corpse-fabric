package dev.fatin.corpse.client.render;

import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;

public final class CorpseRenderState extends SkeletonRenderState {

    public boolean skeleton;
    public boolean faceDown;
    public Identifier texture = DefaultPlayerSkin.getDefaultTexture();
}
