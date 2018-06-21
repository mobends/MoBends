package net.gobbob.mobends.client.renderer.entity;

import net.gobbob.mobends.client.model.entity.ModelBendsSpider;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSpider;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBendsSpider extends RenderSpider
{
    public RenderBendsSpider(RenderManager p_i46139_1_)
    {
        super(p_i46139_1_);
        this.mainModel = new ModelBendsSpider();
    }
    
    @Override
    protected void preRenderCallback(EntityLivingBase argEntity, float p_77041_2_)
    {
        float f1 = 0.9375F;
        GlStateManager.scale(f1, f1, f1);
        
        //((ModelBendsSpider)this.mainModel).updateWithEntityData((EntitySpider)argEntity);
        ((ModelBendsSpider)this.mainModel).postRenderTranslate(0.0625f);
        ((ModelBendsSpider)this.mainModel).postRenderRotate(0.0625f);
    }
}