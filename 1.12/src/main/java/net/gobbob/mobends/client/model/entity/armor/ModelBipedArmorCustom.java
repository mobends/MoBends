package net.gobbob.mobends.client.model.entity.armor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.gobbob.mobends.client.model.IModelPart;
import net.gobbob.mobends.client.model.ModelBox;
import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.model.ModelPartContainer;
import net.gobbob.mobends.client.mutators.BoxMutator;
import net.gobbob.mobends.data.DataBiped;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.EntityDatabase;
import net.gobbob.mobends.util.ModelUtils;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;

public class ModelBipedArmorCustom extends ModelBiped
{
	protected ModelBiped original;
	protected List<Field> gatheredFields;
	
	protected List<ModelPartContainer> bodyParts;
	protected List<ModelPartContainer> headParts;
	protected List<ModelPartContainer> leftArmParts;
	protected List<ModelPartContainer> rightArmParts;
	protected List<ModelPartContainer> leftLegParts;
	protected List<ModelPartContainer> rightLegParts;
	protected List<ModelPartContainer> leftForeArmParts;
	protected List<ModelPartContainer> rightForeArmParts;
	protected List<ModelPartContainer> leftForeLegParts;
	protected List<ModelPartContainer> rightForeLegParts;
	
	public ModelBipedArmorCustom(ModelBiped original)
	{
		this.original = original;
		this.gatheredFields = new ArrayList<Field>();
		this.bodyParts = new ArrayList<ModelPartContainer>();
		this.headParts = new ArrayList<ModelPartContainer>();
		this.leftArmParts = new ArrayList<ModelPartContainer>();
		this.rightArmParts = new ArrayList<ModelPartContainer>();
		this.leftLegParts = new ArrayList<ModelPartContainer>();
		this.rightLegParts = new ArrayList<ModelPartContainer>();
		this.leftForeArmParts = new ArrayList<ModelPartContainer>();
		this.rightForeArmParts = new ArrayList<ModelPartContainer>();
		this.leftForeLegParts = new ArrayList<ModelPartContainer>();
		this.rightForeLegParts = new ArrayList<ModelPartContainer>();
	}
	
	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        
        EntityData entityData = EntityDatabase.instance.get(entityIn.getEntityId());
		if(entityData == null || !(entityData instanceof DataBiped))
			return;
		
		DataBiped dataBiped = (DataBiped) entityData;
        
        GlStateManager.pushMatrix();
        original.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        
        if (entityIn.isSneaking())
        {
        	// This value was fine-tuned to counteract the vanilla
        	// translation done to the character.
        	GlStateManager.translate(0, 0.2D, 0);
        }
        
        GlStateManager.pushMatrix();
	        for(IModelPart part : this.leftForeArmParts)
			{
				part.renderPart(scale);
			}
        GlStateManager.popMatrix();
		
        GlStateManager.pushMatrix();
			for(IModelPart part : this.rightForeArmParts)
			{
				part.renderPart(scale);
			}
		GlStateManager.popMatrix();
		
		GlStateManager.pushMatrix();
			dataBiped.leftLeg.applyOwnTransform(scale);
			for(IModelPart part : this.leftForeLegParts)
			{
				part.renderPart(scale);
			}
		GlStateManager.popMatrix();
		
		GlStateManager.pushMatrix();
			dataBiped.rightLeg.applyOwnTransform(scale);
			for(IModelPart part : this.rightForeLegParts)
			{
				part.renderPart(scale);
			}
		GlStateManager.popMatrix();
        
        GlStateManager.popMatrix();
    }
	
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
    {
		original.setModelAttributes(this);
		
		EntityData entityData = EntityDatabase.instance.get(entityIn.getEntityId());
		if(entityData == null || !(entityData instanceof DataBiped))
			return;
		
		DataBiped dataBiped = (DataBiped) entityData;
		
		for(IModelPart part : this.headParts)
		{
			part.setVisible(this.bipedHead.showModel);
			part.syncUp(dataBiped.head);
		}
		
		for(IModelPart part : this.bodyParts)
		{
			part.setVisible(this.bipedBody.showModel);
			part.syncUp(dataBiped.body);
		}
		
		for(IModelPart part : this.leftArmParts)
		{
			part.setVisible(this.bipedLeftArm.showModel);
			part.syncUp(dataBiped.leftArm);
			dataBiped.position.y = 2F;
		}
		
		for(IModelPart part : this.rightArmParts)
		{
			part.setVisible(this.bipedRightArm.showModel);
			part.syncUp(dataBiped.rightArm);
			dataBiped.position.y = 2F;
		}
		
		for(IModelPart part : this.leftLegParts)
		{
			part.setVisible(this.bipedLeftLeg.showModel);
			part.syncUp(dataBiped.leftLeg);
		}
		
		for(IModelPart part : this.rightLegParts)
		{
			part.setVisible(this.bipedRightLeg.showModel);
			part.syncUp(dataBiped.rightLeg);
		}
		
		for(IModelPart part : this.leftForeArmParts)
		{
			part.setVisible(this.bipedLeftArm.showModel);
			part.syncUp(dataBiped.leftForeArm);
		}
		
		for(IModelPart part : this.rightForeArmParts)
		{
			part.setVisible(this.bipedRightArm.showModel);
			part.syncUp(dataBiped.rightForeArm);
		}
		
		for(IModelPart part : this.leftForeLegParts)
		{
			part.setVisible(this.bipedLeftLeg.showModel);
			part.syncUp(dataBiped.leftForeLeg);
		}
		
		for(IModelPart part : this.rightForeLegParts)
		{
			part.setVisible(this.bipedRightLeg.showModel);
			part.syncUp(dataBiped.rightForeLeg);
		}
    }
	
	protected void mutateOriginal()
	{
		this.headParts.clear();
		this.bodyParts.clear();
		this.leftArmParts.clear();
		this.rightArmParts.clear();
		this.leftLegParts.clear();
		this.rightLegParts.clear();
		
		gatherFields(original.getClass());
		
		List<ModelRenderer> children = new ArrayList<ModelRenderer>();
		for(ModelRenderer part : original.boxList)
		{
			if(part.childModels != null)
				children.addAll(part.childModels);
		}
		
		for(Field f : this.gatheredFields)
		{
			System.out.println("ArmorField: " + f);
			
			try
			{
				ModelRenderer modelRenderer = (ModelRenderer) f.get(original);
				
				ModelPartContainer container = createAndAssignPart(modelRenderer);
				f.set(original, container);
			}
			catch (IllegalArgumentException | IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
		
		mutateParts();
		sliceParts();
	}
	
	protected void gatherFields(Class modelClass)
	{
		Field[] fields = modelClass.getDeclaredFields();
		for(Field f : fields)
		{
			if(!ModelRenderer.class.isAssignableFrom(f.getType()))
				continue;
			f.setAccessible(true);
			this.gatheredFields.add(f);
		}
		
		if(modelClass.getSuperclass() != null)
			gatherFields(modelClass.getSuperclass());
	}
	
	protected ModelPartContainer mutatePart(ModelRenderer modelRenderer)
	{
		return new ModelPartContainer(this, modelRenderer);
	}
	
	protected ModelPartContainer createAndAssignPart(ModelRenderer part)
	{
		ModelPartContainer container = new ModelPartContainer(this, part);
		container.mirror = part.mirror;
		
		AxisAlignedBB bounds = ModelUtils.getBounds(part);
		System.out.println("Bounds: " + bounds);
		
		if(part.rotationPointY >= 11F)
		{
			if(part.rotationPointX < 0F)
			{
				// Right leg/foreleg
				if(!(this.bipedRightLeg instanceof ModelPartContainer))
					this.bipedRightLeg = container;
				this.rightLegParts.add(container);
			}
			else
			{
				// Left leg/foreleg
				if(!(this.bipedLeftLeg instanceof ModelPartContainer))
					this.bipedLeftLeg = container;
				this.leftLegParts.add(container);
			}
		}
		else if(part.rotationPointX <= -5F || (part.cubeList != null &&
				part.cubeList.size() > 0 && part.rotationPointX+part.cubeList.get(0).posX1 <= -6F))
		{
			// Right arm/forearm
			if(!(this.bipedRightArm instanceof ModelPartContainer))
				this.bipedRightArm = container;
			this.rightArmParts.add(container);
		}
		else if(part.rotationPointX >= 5F || (part.cubeList != null &&
				part.cubeList.size() > 0 && part.rotationPointX+part.cubeList.get(0).posX2 >= 6F))
		{
			// Left arm/forearm
			if(!(this.bipedLeftArm instanceof ModelPartContainer))
				this.bipedLeftArm = container;
			this.leftArmParts.add(container);
		}
		else if(part.cubeList != null && part.cubeList.size() > 0 && bounds.maxY >= 4F)
		{
			// Body
			if(!(this.bipedBody instanceof ModelPartContainer))
				this.bipedBody = container;
			this.bodyParts.add(container);
		}
		else
		{
			// Head
			if(!(this.bipedHead instanceof ModelPartContainer))
				this.bipedHead = container;
			this.headParts.add(container);
		}

		return container;
	}
	
	protected void mutateParts()
	{
		for(ModelPartContainer part : headParts)
		{
			part.setParent((IModelPart) this.bipedBody);
		}
		
		for(ModelPartContainer part : bodyParts)
		{
			part.setInnerOffset(0, -12F, 0);
		}
		
		for(ModelPartContainer part : leftArmParts)
		{
			part.setInnerOffset(-5F, -2F, 0F);
			part.setParent((IModelPart) this.bipedBody);
		}
		
		for(ModelPartContainer part : rightArmParts)
		{
			part.setInnerOffset(5F, -2F, 0F);
			part.setParent((IModelPart) this.bipedBody);
		}
		
		for(ModelPartContainer part : leftLegParts)
			part.setInnerOffset(-1.9F, -12F, 0F);
		for(ModelPartContainer part : rightLegParts)
			part.setInnerOffset(1.9F, -12F, 0F);
	}
	
	/* 
	 * This function takes groups of models, and divides them up into sub-groups, like the upper arm and lower arm.
	 */
	protected void sliceParts()
	{
		for(ModelPartContainer part : leftLegParts)
		{
			sliceLeg(part, leftForeLegParts);
		}
		
		for(ModelPartContainer part : rightLegParts)
		{
			sliceLeg(part, rightForeLegParts);
		}
		
		/*for(ModelPartContainer part : leftArmParts)
		{
			sliceArm(part, leftForeArmParts);
		}
		
		for(ModelPartContainer part : rightArmParts)
		{
			sliceArm(part, rightForeArmParts);
		}*/
	}
	
	protected void sliceLeg(ModelPartContainer part, List<ModelPartContainer> listToAddTo)
	{
		for(net.minecraft.client.model.ModelBox box : part.getModel().cubeList)
		{
			BoxMutator mutator = BoxMutator.createFrom(this, part, box);
			if(mutator != null)
			{
				ModelBox lowerPart = mutator.sliceFromBottom(6, false);
				ModelBox topPart = mutator.getTargetBox();
				part.getModel().cubeList.remove(box);
				part.getModel().cubeList.add(topPart);
				
				if(lowerPart != null)
				{
					ModelPart modelPart = new ModelPart(this, mutator.getTextureOffsetX(), mutator.getTextureOffsetY());
					modelPart.cubeList.add(lowerPart);
					ModelPartContainer partContainer = new ModelPartContainer(this, modelPart);
					partContainer.setInnerOffset(0, 0F, 2F);
					listToAddTo.add(partContainer);
				}
			}
		}
	}
	
	protected void sliceArm(ModelPartContainer part, List<ModelPartContainer> listToAddTo)
	{
		for(net.minecraft.client.model.ModelBox box : part.getModel().cubeList)
		{
			BoxMutator mutator = BoxMutator.createFrom(this, part, box);
			if(mutator != null)
			{
				ModelBox lowerPart = mutator.sliceFromBottom(6, false);
				ModelBox topPart = mutator.getTargetBox();
				part.getModel().cubeList.remove(box);
				part.getModel().cubeList.add(topPart);
				
				if(lowerPart != null)
				{
					ModelPart modelPart = new ModelPart(this, mutator.getTextureOffsetX(), mutator.getTextureOffsetY());
					modelPart.cubeList.add(lowerPart);
					ModelPartContainer partContainer = new ModelPartContainer(this, modelPart);
					partContainer.setInnerOffset(0, 0F, -2F);
					listToAddTo.add(partContainer);
				}
			}
		}
	}
	
	public static ModelBipedArmorCustom createFrom(ModelBiped src)
	{
		ModelBipedArmorCustom customModel = new ModelBipedArmorCustom(src);
		customModel.mutateOriginal();
		
		return customModel;
	}
}
