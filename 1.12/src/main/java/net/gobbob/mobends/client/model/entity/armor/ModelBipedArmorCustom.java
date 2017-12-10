package net.gobbob.mobends.client.model.entity.armor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.gobbob.mobends.client.model.IModelPart;
import net.gobbob.mobends.client.model.ModelPartContainer;
import net.gobbob.mobends.data.DataBiped;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.EntityDatabase;
import net.gobbob.mobends.util.ModelUtils;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;

public class ModelBipedArmorCustom extends ModelBiped
{
	protected ModelBiped original;
	protected List<Field> gatheredFields;
	
	protected List<ModelPartContainer> body;
	protected List<ModelPartContainer> head;
	protected List<ModelPartContainer> leftArm;
	protected List<ModelPartContainer> rightArm;
	protected List<ModelPartContainer> leftLeg;
	protected List<ModelPartContainer> rightLeg;
	
	public ModelBipedArmorCustom(ModelBiped original) {
		this.original = original;
		this.gatheredFields = new ArrayList<Field>();
		this.body = new ArrayList<ModelPartContainer>();
		this.head = new ArrayList<ModelPartContainer>();
		this.leftArm = new ArrayList<ModelPartContainer>();
		this.rightArm = new ArrayList<ModelPartContainer>();
		this.leftLeg = new ArrayList<ModelPartContainer>();
		this.rightLeg = new ArrayList<ModelPartContainer>();
	}
	
	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        
        GlStateManager.pushMatrix();
        original.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        GlStateManager.popMatrix();
    }
	
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
    {
		original.setModelAttributes(this);
		
		EntityData entityData = EntityDatabase.instance.get(entityIn.getEntityId());
		if(entityData == null || !(entityData instanceof DataBiped)) return;
		
		DataBiped dataBiped = (DataBiped) entityData;
		
		for(IModelPart part : this.head) {
			part.setVisible(this.bipedHead.showModel);
			part.syncUp(dataBiped.head);
		}
		
		for(IModelPart part : this.body) {
			part.setVisible(this.bipedBody.showModel);
			part.syncUp(dataBiped.body);
		}
		
		for(IModelPart part : this.leftArm) {
			part.setVisible(this.bipedLeftArm.showModel);
			part.syncUp(dataBiped.leftArm);
			dataBiped.position.y = 2F;
		}
		
		for(IModelPart part : this.rightArm) {
			part.setVisible(this.bipedRightArm.showModel);
			part.syncUp(dataBiped.rightArm);
			dataBiped.position.y = 2F;
		}
		
		for(IModelPart part : this.leftLeg) {
			part.setVisible(this.bipedLeftLeg.showModel);
			part.syncUp(dataBiped.leftLeg);
		}
		
		for(IModelPart part : this.rightLeg) {
			part.setVisible(this.bipedRightLeg.showModel);
			part.syncUp(dataBiped.rightLeg);
		}
    }
	
	protected void mutateOriginal() {
		this.head.clear();
		this.body.clear();
		this.leftArm.clear();
		this.rightArm.clear();
		this.leftLeg.clear();
		this.rightLeg.clear();
		
		gatherFields(original.getClass());
		
		List<ModelRenderer> children = new ArrayList<ModelRenderer>();
		for(ModelRenderer part : original.boxList) {
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
		
		AxisAlignedBB bounds = ModelUtils.getBounds(part);
		System.out.println("Bounds: " + bounds);
		
		if(part.rotationPointY >= 11F)
		{
			if(part.rotationPointX < 0F)
			{
				// Right leg/foreleg
				if(!(this.bipedRightLeg instanceof ModelPartContainer))
					this.bipedRightLeg = container;
				this.rightLeg.add(container);
			}
			else
			{
				// Left leg/foreleg
				if(!(this.bipedLeftLeg instanceof ModelPartContainer))
					this.bipedLeftLeg = container;
				this.leftLeg.add(container);
			}
		}
		else if(part.rotationPointX <= -5F || (part.cubeList != null &&
				part.cubeList.size() > 0 && part.rotationPointX+part.cubeList.get(0).posX1 <= -6F))
		{
			// Right arm/forearm
			if(!(this.bipedRightArm instanceof ModelPartContainer))
				this.bipedRightArm = container;
			this.rightArm.add(container);
		}
		else if(part.rotationPointX >= 5F || (part.cubeList != null &&
				part.cubeList.size() > 0 && part.rotationPointX+part.cubeList.get(0).posX2 >= 6F))
		{
			// Left arm/forearm
			if(!(this.bipedLeftArm instanceof ModelPartContainer))
				this.bipedLeftArm = container;
			this.leftArm.add(container);
		}
		else if(part.cubeList != null && part.cubeList.size() > 0 && bounds.maxY >= 4F)
		{
			// Body
			if(!(this.bipedBody instanceof ModelPartContainer))
				this.bipedBody = container;
			this.body.add(container);
		}
		else
		{
			// Head
			if(!(this.bipedHead instanceof ModelPartContainer))
				this.bipedHead = container;
			this.head.add(container);
		}

		return container;
	}
	
	protected void mutateParts()
	{
		for(ModelPartContainer part : head)
		{
			part.setParent((IModelPart) this.bipedBody);
		}
		
		for(ModelPartContainer part : body)
		{
			part.setInnerOffset(0, -12F, 0);
		}
		
		for(ModelPartContainer part : leftArm)
		{
			part.setInnerOffset(-5F, -2F, 0F);
			part.setParent((IModelPart) this.bipedBody);
		}
		
		for(ModelPartContainer part : rightArm)
		{
			part.setInnerOffset(5F, -2F, 0F);
			part.setParent((IModelPart) this.bipedBody);
		}
		
		for(ModelPartContainer part : leftLeg)
			part.setInnerOffset(-1.9F, -12F, 0F);
		for(ModelPartContainer part : rightLeg)
			part.setInnerOffset(1.9F, -12F, 0F);
	}
	
	public static ModelBipedArmorCustom createFrom(ModelBiped src) {
		ModelBipedArmorCustom customModel = new ModelBipedArmorCustom(src);
		customModel.mutateOriginal();
		
		/* 
		 * Gathering all children to later only process
		 * models that are not children of other models.
		 * */
		List<ModelRenderer> children = new ArrayList<ModelRenderer>();
		for(ModelRenderer modelRenderer : src.boxList) {
			if(modelRenderer.childModels != null)
				children.addAll(modelRenderer.childModels);
		}
		
		/*for(int i = 0; i < src.boxList.size(); ++i) {
			// Process only if it's not a child
			if(!children.contains(src.boxList.get(i)))
				customModel.addPart(src.boxList, i);
		}*/
		
		return customModel;
	}
}
