package net.gobbob.mobends.client.model.entity.armor;

import java.util.ArrayList;
import java.util.List;

import net.gobbob.mobends.client.model.IModelPart;
import net.gobbob.mobends.client.model.ModelPart;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;

public class ModelBipedArmorCustom extends ModelBiped
{
	protected List<IModelPart> body;
	protected List<IModelPart> head;
	protected List<IModelPart> leftArm;
	protected List<IModelPart> rightArm;
	protected List<IModelPart> leftLeg;
	protected List<IModelPart> rightLeg;
	
	public ModelBipedArmorCustom() {
		this.body = new ArrayList<IModelPart>();
		this.head = new ArrayList<IModelPart>();
		this.leftArm = new ArrayList<IModelPart>();
		this.rightArm = new ArrayList<IModelPart>();
		this.leftLeg = new ArrayList<IModelPart>();
		this.rightLeg = new ArrayList<IModelPart>();
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
		ModelBipedArmorCustom customModel = new ModelBipedArmorCustom();
		
		for(ModelRenderer modelRenderer : src.boxList) {
			customModel.addPart(modelRenderer);
		}
		
		return customModel;
	}
}
