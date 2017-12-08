package net.gobbob.mobends.client.model.entity.armor;

import java.util.ArrayList;
import java.util.List;

import net.gobbob.mobends.client.model.IModelPart;
import net.gobbob.mobends.client.model.ModelPart;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;

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
    }
	
	protected void addPart(ModelRenderer modelRenderer) {
		if(modelRenderer.rotationPointX <= -5) {
			// Right arm/forearm
			//ModelPart.createFrom(modelRenderer);
		}else if(modelRenderer.rotationPointX >= 5) {
			// Left arm/forearm
		}
	}
	
	public static ModelBipedArmorCustom createFrom(ModelBiped src) {
		ModelBipedArmorCustom customModel = new ModelBipedArmorCustom(src);
		
		for(ModelRenderer modelRenderer : src.boxList) {
			customModel.addPart(modelRenderer);
		}
		
		return customModel;
	}
}
