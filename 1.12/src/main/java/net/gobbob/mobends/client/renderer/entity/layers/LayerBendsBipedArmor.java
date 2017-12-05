package net.gobbob.mobends.client.renderer.entity.layers;

import net.gobbob.mobends.client.model.entity.ModelBipedArmorM;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerBendsBipedArmor extends LayerArmorBase<ModelBipedArmorM>
{
	public LayerBendsBipedArmor(RenderLivingBase<?> rendererIn)
    {
        super(rendererIn);
    }

	@Override
	protected void initArmor() {
		this.modelLeggings = new ModelBipedArmorM(0.5F);
        this.modelArmor = new ModelBipedArmorM(1.0F);
	}
	
	protected void setModelVisible(ModelBipedArmorM model)
    {
        model.setVisible(false);
    }

	@Override
	protected void setModelSlotVisible(ModelBipedArmorM model, EntityEquipmentSlot p_188359_2_) {
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
		default:
			break;
	    }
	}
}