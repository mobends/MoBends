package net.gobbob.mobends.client.renderer.entity;

import net.gobbob.mobends.client.model.entity.ModelBendsSpider;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.util.ResourceLocation;

public class RenderBendsCaveSpider extends RenderBendsSpider {
    private static final ResourceLocation CAVE_SPIDER_TEXTURES = new ResourceLocation("textures/entity/spider/cave_spider.png");

    public RenderBendsCaveSpider(RenderManager renderManagerIn)
    {
        super(renderManagerIn);
        this.shadowSize *= 0.7F;
    }

    /**
     * Allows the render to do state modifications necessary before the model is rendered.
     */
    protected void preRenderCallback(EntityLivingBase entitylivingbaseIn, float partialTickTime)
    {
        GlStateManager.scale(0.7F, 0.7F, 0.7F);
        
        ((ModelBendsSpider)this.mainModel).updateWithEntityData((EntitySpider) entitylivingbaseIn);
        ((ModelBendsSpider)this.mainModel).postRenderTranslate(0.0625f);
        ((ModelBendsSpider)this.mainModel).postRenderRotate(0.0625f);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    @Override
    protected ResourceLocation getEntityTexture(EntitySpider entity)
    {
        return CAVE_SPIDER_TEXTURES;
    }
}
