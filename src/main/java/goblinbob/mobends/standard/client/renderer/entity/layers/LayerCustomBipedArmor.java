package goblinbob.mobends.standard.client.renderer.entity.layers;

import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.standard.client.model.armor.ArmorModelFactory;
import goblinbob.mobends.standard.client.model.armor.ArmorWrapper;
import goblinbob.mobends.standard.client.model.armor.MalformedArmorModelException;
import goblinbob.mobends.standard.main.ModConfig;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import goblinbob.mobends.core.compat.EntityEquipmentSlot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerCustomBipedArmor extends LayerArmorBase<ModelBiped>
{
	
    public LayerCustomBipedArmor(RendererLivingEntity<?> rendererIn)
    {
        super(rendererIn);
    }
    
    @Override
    public void initArmor()
    {
    	this.modelLeggings = new ModelBiped(0.5F);
        this.modelArmor = new ModelBiped(1.0F);
    }

    /** {@code armorSlot}: as LayerArmorBase numbers them, 1 the feet up to 4 the head. */
    @Override
    protected void setModelPartVisible(ModelBiped model, int armorSlot)
    {
        this.hideModelParts(model);

        switch (EntityEquipmentSlot.fromArmorIndex(armorSlot - 1))
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
        model.setInvisible(false);
    }

    @Override
    protected ModelBiped getArmorModelHook(net.minecraft.entity.EntityLivingBase entity, net.minecraft.item.ItemStack itemStack, int armorSlot, ModelBiped model)
    {
    	EntityData<?> entityData = EntityDatabase.instance.get(entity);
    	
    	final ModelBiped suggestedModel = net.minecraftforge.client.ForgeHooksClient.getArmorModel(entity, itemStack, armorSlot, model);

        boolean shouldBeMutated = !ModConfig.shouldKeepArmorAsVanilla(itemStack.getItem()) && ArmorWrapper.fits(entityData);

        try
        {
            final ModelBiped armorModel = ArmorModelFactory.getArmorModel(suggestedModel, shouldBeMutated);
            // LayerArmorBase gave the model it made the renderer's attributes (swing, riding, child), not this
            // one; a model is a child until told otherwise in 1.8.9, and drawn half its size if left so.
            armorModel.setModelAttributes(model);
            if (armorModel instanceof ArmorWrapper)
            {
                ((ArmorWrapper) armorModel).setWorn(itemStack, EntityEquipmentSlot.fromArmorIndex(armorSlot - 1));
            }
            return armorModel;
        }
        catch(MalformedArmorModelException ex)
        {
            ex.printStackTrace();
            return suggestedModel;
        }
    }
}
