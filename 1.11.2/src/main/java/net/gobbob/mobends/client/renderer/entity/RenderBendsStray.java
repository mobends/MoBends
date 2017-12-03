package net.gobbob.mobends.client.renderer.entity;

import net.gobbob.mobends.client.renderer.entity.layers.LayerBendsStrayClothing;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerStrayClothing;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.util.ResourceLocation;

public class RenderBendsStray extends RenderBendsSkeleton{
    private static final ResourceLocation STRAY_SKELETON_TEXTURES = new ResourceLocation("textures/entity/skeleton/stray.png");

    public RenderBendsStray(RenderManager p_i47191_1_)
    {
        super(p_i47191_1_);
        this.addLayer(new LayerBendsStrayClothing(this));
    }
    
    protected ResourceLocation getEntityTexture(AbstractSkeleton entity)
    {
        return STRAY_SKELETON_TEXTURES;
    }
}
