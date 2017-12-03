package net.gobbob.mobends.client.renderer.entity.layers;

import net.gobbob.mobends.client.model.entity.ModelBendsBipedArmor;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerBendsBipedArmor extends LayerArmorBase<ModelBendsBipedArmor>
{
	public LayerBendsBipedArmor(RenderLivingBase<?> rendererIn)
    {
        super(rendererIn);
    }

	@Override
	protected void initArmor() {
		this.modelLeggings = new ModelBendsBipedArmor(0.5F, false, false);
        this.modelArmor = new ModelBendsBipedArmor(1.0F, false, false);
	}
	
	protected void setModelVisible(ModelBendsBipedArmor model)
    {
        model.setInvisible(false);
    }

	@Override
	protected void setModelSlotVisible(ModelBendsBipedArmor model, EntityEquipmentSlot p_188359_2_) {
		this.setModelVisible(model);
		
		switch (p_188359_2_)
	    {
	    	case HEAD:
	    		model.bipedHead.showModel = true;
	    		model.bipedHeadwear.showModel = true;
	            break;
	        case CHEST:
	        	model.bipedBody.showModel = true;
            	model.bipedRightArm.showModel = true;
            	model.bipedLeftArm.showModel = true;
            	model.bipedRightForeArm.showModel = true;
                model.bipedLeftForeArm.showModel = true;
	            break;
	        case LEGS:
	        	model.bipedBody.showModel = true;
                model.bipedRightLeg.showModel = true;
                model.bipedLeftLeg.showModel = true;
                model.bipedRightForeLeg.showModel = true;
                model.bipedLeftForeLeg.showModel = true;
	            break;
	        case FEET:
	        	model.bipedRightLeg.showModel = true;
	        	model.bipedLeftLeg.showModel = true;
                model.bipedRightForeLeg.showModel = true;
                model.bipedLeftForeLeg.showModel = true;
	    }
	}
}