package net.gobbob.mobends.standard.client.renderer.entity.layers;

import net.gobbob.mobends.core.client.model.entity.armor.ArmorModelFactory;
import net.gobbob.mobends.core.data.EntityData;
import net.gobbob.mobends.core.data.EntityDatabase;
import net.gobbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerCustomBipedArmor extends LayerArmorBase<ModelBiped>
{
    public LayerCustomBipedArmor(RenderLivingBase<?> rendererIn)
    {
        super(rendererIn);
    }
    
    @Override
    public void initArmor()
    {
        //this.modelLeggings = new ModelBipedArmorM(0.5F);
        //this.modelArmor = new ModelBipedArmorM(1.0F);
    	this.modelLeggings = new ModelBiped(0.5F);
        this.modelArmor = new ModelBiped(1.0F);
    }

    @SuppressWarnings("incomplete-switch")
    protected void setModelSlotVisible(ModelBiped model, EntityEquipmentSlot slotIn)
    {
        this.setModelVisible(model);

        switch (slotIn)
        {
            case HEAD:
                model.bipedHead.showModel = true;
                model.bipedHeadwear.showModel = true;
                break;
            case CHEST:
                model.bipedBody.showModel = true;
                model.bipedRightArm.showModel = true;
                model.bipedLeftArm.showModel = true;
                break;
            case LEGS:
                model.bipedBody.showModel = true;
                model.bipedRightLeg.showModel = true;
                model.bipedLeftLeg.showModel = true;
                break;
            case FEET:
                model.bipedRightLeg.showModel = true;
                model.bipedLeftLeg.showModel = true;
        }
    }

    protected void setModelVisible(ModelBiped model)
    {
        model.setVisible(false);
    }

    @Override
    protected ModelBiped getArmorModelHook(net.minecraft.entity.EntityLivingBase entity, net.minecraft.item.ItemStack itemStack, EntityEquipmentSlot slot, ModelBiped model)
    {
    	EntityData<?> entityData = EntityDatabase.instance.get(entity);
    	
    	ModelBiped suggestedModel = net.minecraftforge.client.ForgeHooksClient.getArmorModel(entity, itemStack, slot, model);
    	if (entityData instanceof BipedEntityData)
    	{
    		return ArmorModelFactory.getArmorModel(suggestedModel);
    	}
    	else
    	{
    		return suggestedModel;
    	}
    }
}