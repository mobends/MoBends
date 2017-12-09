package net.gobbob.mobends.client.model.entity.armor;

import java.util.ArrayList;
import java.util.List;

import net.gobbob.mobends.client.model.IModelPart;
import net.gobbob.mobends.client.model.ModelPartContainer;
import net.gobbob.mobends.data.DataBiped;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.EntityDatabase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

public class ModelBipedArmorCustom extends ModelBiped
{
	protected ModelBiped original;
	
	protected List<IModelPart> body;
	protected List<IModelPart> head;
	protected List<IModelPart> leftArm;
	protected List<IModelPart> rightArm;
	protected List<IModelPart> leftLeg;
	protected List<IModelPart> rightLeg;
	
	public ModelBipedArmorCustom(ModelBiped original) {
		this.original = original;
		this.body = new ArrayList<IModelPart>();
		this.head = new ArrayList<IModelPart>();
		this.leftArm = new ArrayList<IModelPart>();
		this.rightArm = new ArrayList<IModelPart>();
		this.leftLeg = new ArrayList<IModelPart>();
		this.rightLeg = new ArrayList<IModelPart>();
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
			part.syncUp(dataBiped.head);
		}
		
		for(IModelPart part : this.body) {
			part.syncUp(dataBiped.body);
		}
		
		for(IModelPart part : this.leftArm) {
			part.syncUp(dataBiped.leftArm);
			dataBiped.position.y = 2F;
		}
		
		for(IModelPart part : this.rightArm) {
			part.syncUp(dataBiped.rightArm);
			dataBiped.position.y = 2F;
		}
		
		for(IModelPart part : this.leftLeg) {
			part.syncUp(dataBiped.leftLeg);
		}
		
		for(IModelPart part : this.rightLeg) {
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
		
		this.bipedHead = original.bipedHead = mutatePart(original.bipedHead);
		this.bipedBody = original.bipedBody = mutatePart(original.bipedBody);
		this.bipedLeftArm = original.bipedLeftArm = mutatePart(original.bipedLeftArm);
		this.bipedRightArm = original.bipedRightArm = mutatePart(original.bipedRightArm);
		this.bipedLeftLeg = original.bipedLeftLeg = mutatePart(original.bipedLeftLeg);
		this.bipedRightLeg = original.bipedRightLeg = mutatePart(original.bipedRightLeg);
		
		((ModelPartContainer) this.bipedHead).setParent((IModelPart) this.bipedBody);
		
		((ModelPartContainer) this.bipedBody).setInnerOffset(0, -12F, 0);
		
		((ModelPartContainer) this.bipedLeftArm).setInnerOffset(-5.0F, -2.0F, 0.0F);
		((ModelPartContainer) this.bipedLeftArm).setParent((IModelPart) this.bipedBody);
		
		((ModelPartContainer) this.bipedRightArm).setInnerOffset(5.0F, -2.0F, 0.0F);
		((ModelPartContainer) this.bipedRightArm).setParent((IModelPart) this.bipedBody);
		
		((ModelPartContainer) this.bipedLeftLeg).setInnerOffset(-1.9F, -12.0F, 0.0F);
		((ModelPartContainer) this.bipedRightLeg).setInnerOffset(1.9F, -12.0F, 0.0F);
		
		this.head.add((IModelPart) this.bipedHead);
		this.body.add((IModelPart) this.bipedBody);
		this.leftArm.add((IModelPart) this.bipedLeftArm);
		this.rightArm.add((IModelPart) this.bipedRightArm);
		this.leftLeg.add((IModelPart) this.bipedLeftLeg);
		this.rightLeg.add((IModelPart) this.bipedRightLeg);
	}
	
	protected ModelPartContainer mutatePart(ModelRenderer modelRenderer) {
		if(modelRenderer.rotationPointX <= -5) {
			// Right arm/forearm
			return new ModelPartContainer(this, modelRenderer);
		}else if(modelRenderer.rotationPointX >= 5) {
			// Left arm/forearm
			return new ModelPartContainer(this, modelRenderer);
		}else{
			return new ModelPartContainer(this, modelRenderer);
		}
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
