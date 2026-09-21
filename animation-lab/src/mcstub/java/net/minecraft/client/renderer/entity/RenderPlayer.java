package net.minecraft.client.renderer.entity;

import net.minecraft.client.entity.AbstractClientPlayer;

public class RenderPlayer extends Render<AbstractClientPlayer>
{
    public boolean smallArms;
    public RenderPlayer(boolean smallArms) { this.smallArms = smallArms; }
}
