package goblinbob.mobends.standard.client.renderer.entity.layers;

import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.standard.client.model.armor.ArmorModelFactory;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.main.ModConfig;
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
    	this.modelLeggings = new ModelBiped(0.5F);
        this.modelArmor = new ModelBiped(1.0F);
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    protected void setModelSlotVisible(ModelBiped model, EntityEquipmentSlot slotIn)
    {
        this.hideModelParts(model);

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
                break;
            default:
                break;
        }
    }

    protected void hideModelParts(ModelBiped model)
    {
        model.setVisible(false);
    }

    @Override
    protected ModelBiped getArmorModelHook(net.minecraft.entity.EntityLivingBase entity, net.minecraft.item.ItemStack itemStack, EntityEquipmentSlot slot, ModelBiped model)
    {
    	EntityData<?> entityData = EntityDatabase.instance.get(entity);
    	
    	final ModelBiped suggestedModel = net.minecraftforge.client.ForgeHooksClient.getArmorModel(entity, itemStack, slot, model);

        boolean shouldBeMutated = !ModConfig.shouldKeepArmorAsVanilla(itemStack.getItem()) && entityData instanceof BipedEntityData;

        return ArmorModelFactory.getArmorModel(suggestedModel, shouldBeMutated);
    }
}