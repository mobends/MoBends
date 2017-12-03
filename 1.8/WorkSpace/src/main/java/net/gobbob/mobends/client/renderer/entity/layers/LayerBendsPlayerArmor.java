package net.gobbob.mobends.client.renderer.entity.layers;

import net.gobbob.mobends.client.model.entity.ModelBendsPlayerArmor;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerBendsPlayerArmor extends LayerArmorBase
{
    public LayerBendsPlayerArmor(RendererLivingEntity p_i46116_1_)
    {
        super(p_i46116_1_);
    }

    protected void func_177177_a()
    {
        this.field_177189_c = new ModelBendsPlayerArmor(0.5F);
        this.field_177186_d = new ModelBendsPlayerArmor(1.0F);
    }
    
    @Override
    public void doRenderLayer(EntityLivingBase p_177141_1_, float p_177141_2_, float p_177141_3_, float p_177141_4_, float p_177141_5_, float p_177141_6_, float p_177141_7_, float p_177141_8_)
    {
    	super.doRenderLayer(p_177141_1_, p_177141_2_, p_177141_3_, p_177141_4_, p_177141_5_, p_177141_6_, p_177141_7_, p_177141_8_);
    }
    
    protected void func_177195_a(ModelBiped p_177195_1_, int p_177195_2_)
    {
        this.func_177194_a(p_177195_1_);
        
        ModelBendsPlayerArmor model = (ModelBendsPlayerArmor) p_177195_1_;
        
        switch (p_177195_2_)
        {
            case 1:
            	model.bipedRightLeg.showModel = true;
            	model.bipedLeftLeg.showModel = true;
                model.bipedRightForeLeg.showModel = true;
                model.bipedLeftForeLeg.showModel = true;
                break;
            case 2:
            	model.bipedBody.showModel = true;
                model.bipedRightLeg.showModel = true;
                model.bipedLeftLeg.showModel = true;
                model.bipedRightForeLeg.showModel = true;
                model.bipedLeftForeLeg.showModel = true;
                break;
            case 3:
            	model.bipedBody.showModel = true;
            	model.bipedRightArm.showModel = true;
            	model.bipedLeftArm.showModel = true;
            	model.bipedRightForeArm.showModel = true;
                model.bipedLeftForeArm.showModel = true;
                break;
            case 4:
            	model.bipedHead.showModel = true;
            	model.bipedHeadwear.showModel = true;
        }
    }

    protected void func_177194_a(ModelBiped p_177194_1_)
    {
        p_177194_1_.setInvisible(false);
    }

    protected void func_177179_a(ModelBase p_177179_1_, int p_177179_2_)
    {
        this.func_177195_a((ModelBiped)p_177179_1_, p_177179_2_);
    }
}