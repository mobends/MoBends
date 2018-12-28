package net.gobbob.mobends.core.client.model.entity.armor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.core.EntityData;
import net.gobbob.mobends.core.EntityDatabase;
import net.gobbob.mobends.core.animatedentity.AnimatedEntity;
import net.gobbob.mobends.core.client.model.IModelPart;
import net.gobbob.mobends.core.client.model.ModelBox;
import net.gobbob.mobends.core.client.model.ModelPart;
import net.gobbob.mobends.core.client.model.ModelPartContainer;
import net.gobbob.mobends.core.client.model.ModelPartTransform;
import net.gobbob.mobends.core.mutators.BoxMutator;
import net.gobbob.mobends.core.util.ModelUtils;
import net.gobbob.mobends.standard.data.BipedEntityData;
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
	/*
	 * Used to demutate the armor back into it's vanilla state.
	 */
	protected HashMap<Field, ModelRenderer> fieldToOriginalMap;
	/*
	 * Used to demutate the armor back into it's vanilla state.
	 * Both key and value are the of the original vanilla model.
	 */
	protected HashMap<ModelRenderer, net.minecraft.client.model.ModelBox> modelToBoxMap;
	protected HashMap<ModelRenderer, IModelPart> originalToCustomMap;

	/*
	 * Keeps track of whether the model is mutated or not.
	 */
	protected boolean mutated = false;
	
	/*
	 * The lastest AnimatedEntity that rendered this armor.
	 */
	protected AnimatedEntity lastAnimatedEntity;
	
	/*
	 * This is used as a parent for other parts, like the arms and head.
	 */
	protected ModelPartTransform mainBodyTransform;
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
		this.fieldToOriginalMap = new HashMap<Field, ModelRenderer>();
		this.modelToBoxMap = new HashMap<ModelRenderer, net.minecraft.client.model.ModelBox>();
		this.originalToCustomMap = new HashMap<ModelRenderer, IModelPart>();
		this.mainBodyTransform = new ModelPartTransform();
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
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch, float scale)
	{
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);

		AnimatedEntity animatedEntity = AnimatedEntity.getForEntity(entityIn);
		EntityData entityData = EntityDatabase.instance.get(entityIn.getEntityId());
		if (animatedEntity == null || entityData == null || !(entityData instanceof BipedEntityData))
			return;

		lastAnimatedEntity = animatedEntity;
		if (animatedEntity.isAnimated() && !this.mutated)
		{
			this.mutate();
		}
		else if(!animatedEntity.isAnimated() && this.mutated)
		{
			this.demutate();
		}
		
		BipedEntityData dataBiped = (BipedEntityData) entityData;
		
		GlStateManager.pushMatrix();
		original.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		// The visibility is based on whatever is visible in the original, so it has to be called
		// after rendering the original.
		this.updateVisibility();
		
		if (entityIn.isSneaking())
		{
			// This value was fine-tuned to counteract the vanilla
			// translation done to the character.
			GlStateManager.translate(0, 0.2D, 0);
		}

		if (this.isChild)
        {
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
        }
		
		
		GlStateManager.pushMatrix();
		dataBiped.body.applyOwnTransform(scale);
		dataBiped.leftArm.applyOwnTransform(scale);
		for (IModelPart part : this.leftForeArmParts)
		{
			part.renderPart(scale);
		}
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		dataBiped.body.applyOwnTransform(scale);
		dataBiped.rightArm.applyOwnTransform(scale);
		for (IModelPart part : this.rightForeArmParts)
		{
			part.renderPart(scale);
		}
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		dataBiped.leftLeg.applyOwnTransform(scale);
		for (IModelPart part : this.leftForeLegParts)
		{
			part.renderPart(scale);
		}
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		dataBiped.rightLeg.applyOwnTransform(scale);
		for (IModelPart part : this.rightForeLegParts)
		{
			part.renderPart(scale);
		}
		GlStateManager.popMatrix();

		GlStateManager.popMatrix();
	}

	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch, float scaleFactor, Entity entityIn)
	{
		original.setModelAttributes(this);

		EntityData entityData = EntityDatabase.instance.get(entityIn.getEntityId());
		if (entityData == null || !(entityData instanceof BipedEntityData))
			return;

		BipedEntityData dataBiped = (BipedEntityData) entityData;

		this.mainBodyTransform.syncUp(dataBiped.body);
		
		for (IModelPart part : this.headParts)
			part.syncUp(dataBiped.head);

		for (IModelPart part : this.bodyParts)
			part.syncUp(dataBiped.body);

		for (IModelPart part : this.leftArmParts)
			part.syncUp(dataBiped.leftArm);

		for (IModelPart part : this.rightArmParts)
			part.syncUp(dataBiped.rightArm);

		for (IModelPart part : this.leftLegParts)
			part.syncUp(dataBiped.leftLeg);

		for (IModelPart part : this.rightLegParts)
			part.syncUp(dataBiped.rightLeg);

		for (IModelPart part : this.leftForeArmParts)
			part.syncUp(dataBiped.leftForeArm);

		for (IModelPart part : this.rightForeArmParts)
			part.syncUp(dataBiped.rightForeArm);

		for (IModelPart part : this.leftForeLegParts)
			part.syncUp(dataBiped.leftForeLeg);
			
		for (IModelPart part : this.rightForeLegParts)
			part.syncUp(dataBiped.rightForeLeg);
	}
	
	protected void updateVisibility()
	{
		if(original.bipedHead != null)
			for (IModelPart part : this.headParts)
				part.setVisible(this.bipedHead.showModel);
		
		if(original.bipedBody != null)
			for (IModelPart part : this.bodyParts)
				part.setVisible(this.bipedBody.showModel);
		
		if(original.bipedLeftArm != null)
			for (IModelPart part : this.leftArmParts)
				part.setVisible(this.bipedLeftArm.showModel);

		if(original.bipedRightArm != null)
			for (IModelPart part : this.rightArmParts)
				part.setVisible(this.bipedRightArm.showModel);
		
		if(original.bipedLeftLeg != null)
			for (IModelPart part : this.leftLegParts)
				part.setVisible(this.bipedLeftLeg.showModel);
		
		if(original.bipedRightLeg != null)
			for (IModelPart part : this.rightLegParts)
				part.setVisible(this.bipedRightLeg.showModel);
		
		if(original.bipedLeftArm != null)
			for (IModelPart part : this.leftForeArmParts)
				part.setVisible(this.bipedLeftArm.showModel);
		
		if(original.bipedRightArm != null)
			for (IModelPart part : this.rightForeArmParts)
				part.setVisible(this.bipedRightArm.showModel);

		if(original.bipedLeftLeg != null)
			for (IModelPart part : this.leftForeLegParts)
				part.setVisible(this.bipedLeftLeg.showModel);

		if(original.bipedRightLeg != null)
			for (IModelPart part : this.rightForeLegParts)
				part.setVisible(this.bipedRightLeg.showModel);
		
		for (Map.Entry<ModelRenderer, IModelPart> entry : this.originalToCustomMap.entrySet())
			if (entry.getValue().isShowing())
				entry.getValue().setVisible(entry.getKey().showModel && !entry.getKey().isHidden);
		
	}

	protected void mutate()
	{
		if (this.mutated)
		{
			this.demutate();
		}
		
		this.headParts.clear();
		this.bodyParts.clear();
		this.leftArmParts.clear();
		this.rightArmParts.clear();
		this.leftLegParts.clear();
		this.rightLegParts.clear();
		this.leftForeArmParts.clear();
		this.rightForeArmParts.clear();
		this.leftForeLegParts.clear();
		this.rightForeLegParts.clear();

		this.gatheredFields.clear();
		this.fieldToOriginalMap.clear();
		this.modelToBoxMap.clear();
		this.originalToCustomMap.clear();
		gatherFields(original.getClass());
		
		for (Field f : this.gatheredFields)
		{
			System.out.println("ArmorField: " + f);

			try
			{
				ModelRenderer modelRenderer = (ModelRenderer) f.get(original);

				if (modelRenderer != null)
				{
					ModelPartContainer container;
					if (modelRenderer instanceof ModelPartContainer)
					{
						container = (ModelPartContainer) modelRenderer;
					}
					else
					{
						System.out.println("Added to fieldToOriginalMap " + modelRenderer);
						fieldToOriginalMap.put(f, modelRenderer);
						container = this.mutatePart(modelRenderer);
						container.mirror = modelRenderer.mirror;
					}
					this.assignPart(container);
					f.set(original, container);
				}
			}
			catch (IllegalArgumentException | IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}

		this.positionParts();
		this.sliceParts();
		
		this.mutated = true;
	}
	
	/*
	 * Brings the original model back to it's vanilla state.
	 */
	public void demutate()
	{
		for (Field f : this.gatheredFields)
		{
			if (fieldToOriginalMap.containsKey(f))
			{
				System.out.println("Retrieved from fieldToOriginalMap " + fieldToOriginalMap.get(f));
				try
				{
					f.set(original, fieldToOriginalMap.get(f));
				}
				catch (IllegalArgumentException | IllegalAccessException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		for (ModelRenderer renderer : this.modelToBoxMap.keySet())
		{
			renderer.cubeList.clear();
		}
		
		for (Map.Entry<ModelRenderer, net.minecraft.client.model.ModelBox> entry : this.modelToBoxMap.entrySet())
		{
			entry.getKey().cubeList.add(entry.getValue());
		}
		
		this.gatheredFields.clear();
		this.fieldToOriginalMap.clear();
		this.modelToBoxMap.clear();
		this.originalToCustomMap.clear();
		
		this.headParts.clear();
		this.bodyParts.clear();
		this.leftArmParts.clear();
		this.rightArmParts.clear();
		this.leftLegParts.clear();
		this.rightLegParts.clear();
		this.leftForeArmParts.clear();
		this.rightForeArmParts.clear();
		this.leftForeLegParts.clear();
		this.rightForeLegParts.clear();
		
		this.mutated = false;
	}
	
	/*
	 * Ensures that this armor's mutation state is in sync
	 * with it's AnimatedEntity counterpart.
	 * Called from ArmorModelFactory.updateMutation()
	 */
	public void updateMutation()
	{
		if (lastAnimatedEntity == null)
			return;

		if (lastAnimatedEntity.isAnimated() && !this.mutated)
		{
			this.mutate();
		}
		else if(!lastAnimatedEntity.isAnimated() && this.mutated)
		{
			this.demutate();
		}
	}

	/*
	 * Used to get all the Fields from the modelClass and it's
	 * superClasses that are an instance of ModelRenderer.
	 */
	protected void gatherFields(Class<?> modelClass)
	{
		Field[] fields = modelClass.getDeclaredFields();
		for (Field f : fields)
		{
			if (!ModelRenderer.class.isAssignableFrom(f.getType()))
				continue;
			f.setAccessible(true);
			this.gatheredFields.add(f);
		}

		if (modelClass.getSuperclass() != null)
			gatherFields(modelClass.getSuperclass());
	}

	protected ModelPartContainer mutatePart(ModelRenderer modelRenderer)
	{
		return new ModelPartContainer(this, modelRenderer);
	}

	protected void assignPart(ModelPartContainer container)
	{
		ModelRenderer part = container.getModel();
		ModelRenderer rootParent = ModelUtils.getRootParent(part, fieldToOriginalMap.values());
		if (rootParent == null)
			rootParent = part;
		Vector3f globalOrigin = ModelUtils.getGlobalOrigin(part, fieldToOriginalMap.values());
		
		AxisAlignedBB bounds = ModelUtils.getBounds(part);
		System.out.println("Bounds: " + bounds);

		if (globalOrigin.y >= 11F)
		{
			if (globalOrigin.x < 0F)
			{
				// Right leg/foreleg
				this.rightLegParts.add(container);
			}
			else
			{
				// Left leg/foreleg
				this.leftLegParts.add(container);
			}
		}
		else if (globalOrigin.x <= -5F || (part.cubeList != null && part.cubeList.size() > 0
				&& part.rotationPointX + part.cubeList.get(0).posX1 <= -6F))
		{
			// Right arm/forearm
			this.rightArmParts.add(container);
		}
		else if (globalOrigin.x >= 5F || (part.cubeList != null && part.cubeList.size() > 0
				&& part.rotationPointX + part.cubeList.get(0).posX2 >= 6F))
		{
			// Left arm/forearm
			this.leftArmParts.add(container);
		}
		else if (part.cubeList != null && part.cubeList.size() > 0 && bounds.maxY >= 4F)
		{
			// Body
			this.bodyParts.add(container);
		}
		else
		{
			// Head
			this.headParts.add(container);
		}
	}

	protected void positionParts()
	{
		for (ModelPartContainer part : headParts)
		{
			part.setParent(this.mainBodyTransform);
		}

		for (ModelPartContainer part : bodyParts)
		{
			part.setInnerOffset(0, -12F, 0);
		}

		for (ModelPartContainer part : leftArmParts)
		{
			part.setInnerOffset(-5F, -2F, 0F);
			part.setParent(this.mainBodyTransform);
		}

		for (ModelPartContainer part : rightArmParts)
		{
			part.setInnerOffset(5F, -2F, 0F);
			part.setParent(this.mainBodyTransform);
		}

		for (ModelPartContainer part : leftLegParts)
			part.setInnerOffset(-1.9F, -12F, 0F);
		for (ModelPartContainer part : rightLegParts)
			part.setInnerOffset(1.9F, -12F, 0F);
	}

	protected void sliceLeg(ModelPartContainer part, List<ModelPartContainer> listToAddTo)
	{
		float cutPlane = 18F;
		for (int i = part.getModel().cubeList.size() - 1; i >= 0; i--)
		{
			ModelRenderer originalPart = part.getModel();
			net.minecraft.client.model.ModelBox box = originalPart.cubeList.get(i);
			BoxMutator mutator = BoxMutator.createFrom(this, originalPart, box);
			mutator.includeParentTransform(ModelUtils.getParentsList(originalPart, fieldToOriginalMap.values()));
			if (mutator != null)
			{
				modelToBoxMap.put(originalPart, box);
				part.getModel().cubeList.remove(box);
				
				if (mutator.getGlobalBoxY() < cutPlane)
				{
					// Upper leg, try to cut the bottom
					ModelBox lowerPart = mutator.sliceFromBottom(cutPlane, true);
					ModelBox topPart = mutator.getTargetBox();
					part.getModel().cubeList.add(topPart);
	
					if (lowerPart != null)
					{
						ModelPart modelPart = new ModelPart(this, mutator.getTextureOffsetX(), mutator.getTextureOffsetY());
						modelPart.mirror = part.mirror;
						modelPart.cubeList.add(lowerPart);
						ModelPartContainer partContainer = new ModelPartContainer(this, modelPart);
						partContainer.setInnerOffset(0, -6F, 2F);
						listToAddTo.add(partContainer);
						this.originalToCustomMap.put(originalPart, partContainer);
					}
				}
				else
				{
					// Lower leg
					ModelBox lowerBox = mutator.getTargetBox();
					ModelPart modelPart = new ModelPart(this, mutator.getTextureOffsetX(), mutator.getTextureOffsetY());
					modelPart.mirror = part.mirror;
					modelPart.cubeList.add(lowerBox);
					ModelPartContainer partContainer = new ModelPartContainer(this, modelPart);
					partContainer.setInnerOffset(0F, -6.0F, 2F);
					listToAddTo.add(partContainer);
					this.originalToCustomMap.put(originalPart, partContainer);
				}
			}
		}
	}

	protected void sliceArm(ModelPartContainer part, List<ModelPartContainer> listToAddTo)
	{
		float cutPlane = 6F;
		for (int i = part.getModel().cubeList.size() - 1; i >= 0; i--)
		{
			ModelRenderer originalPart = part.getModel();
			net.minecraft.client.model.ModelBox box = originalPart.cubeList.get(i);
			BoxMutator mutator = BoxMutator.createFrom(this, originalPart, box);
			mutator.includeParentTransform(ModelUtils.getParentsList(originalPart, fieldToOriginalMap.values()));
			if (mutator != null)
			{
				modelToBoxMap.put(originalPart, box);
				part.getModel().cubeList.remove(box);
				
				if (mutator.getGlobalBoxY() < cutPlane)
				{
					// Upper arm, try to cut the bottom
					ModelBox lowerPart = mutator.sliceFromBottom(cutPlane, true);
					ModelBox topPart = mutator.getTargetBox();
					part.getModel().cubeList.add(topPart);

					if (lowerPart != null)
					{
						ModelPart modelPart = new ModelPart(this, mutator.getTextureOffsetX(), mutator.getTextureOffsetY());
						modelPart.mirror = part.mirror;
						modelPart.cubeList.add(lowerPart);
						ModelPartContainer partContainer = new ModelPartContainer(this, modelPart);
						partContainer.setInnerOffset(0F, -4.0F, -2F);
						listToAddTo.add(partContainer);
						this.originalToCustomMap.put(originalPart, partContainer);
					}
				}
				else
				{
					// Lower arm
					ModelBox lowerBox = mutator.getTargetBox();
					ModelPart modelPart = new ModelPart(this, mutator.getTextureOffsetX(), mutator.getTextureOffsetY());
					modelPart.mirror = part.mirror;
					modelPart.cubeList.add(lowerBox);
					ModelPartContainer partContainer = new ModelPartContainer(this, modelPart);
					partContainer.setInnerOffset(0F, -4.0F, -2F);
					listToAddTo.add(partContainer);
					this.originalToCustomMap.put(originalPart, partContainer);
				}
			}
		}
	}
	
	/*
	 * This function takes groups of models, and divides them up into sub-groups,
	 * like the upper arm and lower arm.
	 */
	protected void sliceParts()
	{
		for (ModelPartContainer part : leftLegParts)
		{
			sliceLeg(part, leftForeLegParts);
		}

		for (ModelPartContainer part : rightLegParts)
		{
			sliceLeg(part, rightForeLegParts);
		}

		for (ModelPartContainer part : leftArmParts)
		{
			sliceArm(part, leftForeArmParts);
		}

		for (ModelPartContainer part : rightArmParts)
		{
			sliceArm(part, rightForeArmParts);
		}
	}
	
	public static ModelBipedArmorCustom createFrom(ModelBiped src)
	{
		ModelBipedArmorCustom customModel = new ModelBipedArmorCustom(src);
		customModel.mutate();

		return customModel;
	}
}
