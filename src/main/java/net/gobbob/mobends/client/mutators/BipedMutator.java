package net.gobbob.mobends.client.mutators;

import java.util.HashMap;
import java.util.List;

import net.gobbob.mobends.client.model.IBendsModel;
import net.gobbob.mobends.client.model.IModelPart;
import net.gobbob.mobends.client.model.ModelBox;
import net.gobbob.mobends.client.model.ModelPartChild;
import net.gobbob.mobends.client.model.ModelPartChildExtended;
import net.gobbob.mobends.client.model.ModelPartChildPostOffset;
import net.gobbob.mobends.client.model.ModelPartExtended;
import net.gobbob.mobends.client.model.ModelPartPostOffset;
import net.gobbob.mobends.client.model.ModelPartTransform;
import net.gobbob.mobends.client.renderer.entity.layers.LayerCustomBipedArmor;
import net.gobbob.mobends.client.renderer.entity.layers.LayerCustomHeldItem;
import net.gobbob.mobends.util.FieldMiner;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;

public abstract class BipedMutator<T extends EntityLivingBase, M extends ModelBiped> extends Mutator<M>
{
	protected ModelPartPostOffset body;
	protected ModelPartChild head;
	protected ModelPartChild headwear;
	protected ModelPartChildExtended leftArm;
	protected ModelPartChildExtended rightArm;
	protected ModelPartChildPostOffset leftForeArm;
	protected ModelPartChildPostOffset rightForeArm;
	protected ModelPartExtended leftLeg;
	protected ModelPartExtended rightLeg;
	protected ModelPartChild leftForeLeg;
	protected ModelPartChild rightForeLeg;

	protected LayerBipedArmor vanillaLayerArmor;
	protected LayerHeldItem vanillaLayerHeldItem;
	protected LayerCustomBipedArmor layerArmor;
	protected LayerCustomHeldItem layerHeldItem;

	public BipedMutator() {}

	/*
	 * Used to fetch private data from the original
	 * renderer.
	 */
	public void fetchFields(RenderLivingBase<T> renderer)
	{
		// Getting the layer renderers
		this.layerRenderers = FieldMiner.getObfuscatedValue(renderer, "layerRenderers", "field_177097_h");
	}

	/*
	 * Used to store the model parameter as the
	 * vanilla model, so then the mutation can be
	 * reversed.
	 */
	public void storeVanillaModel(M model)
	{
		this.vanillaModel.bipedBody = model.bipedBody;
		this.vanillaModel.bipedHead = model.bipedHead;
		this.vanillaModel.bipedHeadwear = model.bipedHeadwear;
		this.vanillaModel.bipedLeftArm = model.bipedLeftArm;
		this.vanillaModel.bipedLeftLeg = model.bipedLeftLeg;
		this.vanillaModel.bipedRightArm = model.bipedRightArm;
		this.vanillaModel.bipedRightLeg = model.bipedRightLeg;
	}
	
	/*
	 * Sets the model parameter back to it's vanilla
	 * state. Used to demutate the model.
	 */
	public void applyVanillaModel(M model)
	{
		model.bipedBody = this.vanillaModel.bipedBody;
		model.bipedHead = this.vanillaModel.bipedHead;
		model.bipedHeadwear = this.vanillaModel.bipedHeadwear;
		model.bipedLeftArm = this.vanillaModel.bipedLeftArm;
		model.bipedLeftLeg = this.vanillaModel.bipedLeftLeg;
		model.bipedRightArm = this.vanillaModel.bipedRightArm;
		model.bipedRightLeg = this.vanillaModel.bipedRightLeg;
	}
	
	/*
	 * Swaps out the vanilla layers for their custom counterparts,
	 * and if it's a vanilla model, it stores the vanilla layers
	 * for future mutation reversal.
	 */
	public void swapLayer(RenderLivingBase<T> renderer, int index, boolean isModelVanilla)
	{
		LayerRenderer<EntityLivingBase> layer = layerRenderers.get(index);
		if (layer instanceof LayerBipedArmor)
		{
			this.layerArmor = new LayerCustomBipedArmor(renderer);
			if (isModelVanilla)
				this.vanillaLayerArmor = (LayerBipedArmor) layer;
			layerRenderers.set(index, this.layerArmor);
		}
		else if (layer instanceof LayerHeldItem)
		{
			this.layerHeldItem = new LayerCustomHeldItem(renderer);
			if (isModelVanilla)
				this.vanillaLayerHeldItem = (LayerHeldItem) layer;
			layerRenderers.set(index, this.layerHeldItem);
		}
	}
	
	/*
	 * Swaps the custom layers back with the vanilla layers.
	 * Used to demutate the model.
	 */
	public void deswapLayer(RenderLivingBase<T> renderer, int index)
	{
		LayerRenderer<EntityLivingBase> layer = layerRenderers.get(index);
		if (layer instanceof LayerCustomBipedArmor)
		{
			layerRenderers.set(index, this.vanillaLayerArmor);
		}
		else if (layer instanceof LayerCustomHeldItem)
		{
			layerRenderers.set(index, this.vanillaLayerHeldItem);
		}
	}
	
	/*
	 * Creates all the custom parts you need! It swaps all the
	 * original parts with newly created custom parts.
	 */
	public boolean createParts(M original, float scaleFactor)
	{
		// Body
		original.bipedBody = body = (ModelPartPostOffset) new ModelPartPostOffset(original, 16, 16)
				.setPostOffset(0.0F, -12.0F, 0.0F)
				.setPosition(0.0F, 12.0F, 0.0F)
				.setBox(-4.0F, -12.0F, -2.0F, 8, 12, 4, scaleFactor);

		// Head
		original.bipedHead = head = (ModelPartChild) new ModelPartChild(original, 0, 0)
				.setParent(body)
				.setHideLikeParent(false)
				.setPosition(0.0F, -12.0F, 0.0F)
				.setBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, scaleFactor);

		// Arms
		int armWidth = 4;
		float armY = -10F;

		original.bipedLeftArm = leftArm = (ModelPartChildExtended) new ModelPartChildExtended(original, 40, 16)
				.setParent(body)
				.setHideLikeParent(false)
				.setPosition(5.0F, armY, 0.0F)
				.setMirror(true)
				.setBox(-1.0F, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor)
				.offsetBoxBy(-0.01f, 0, -0.01f)
				.resizeBox(armWidth + 0.02f, 6.0f, 4.02f)
				.updateVertices()
				.setVisibility(ModelBox.BOTTOM, false);
		original.bipedRightArm = rightArm = (ModelPartChildExtended) new ModelPartChildExtended(original, 40, 16)
				.setParent(body)
				.setHideLikeParent(false)
				.setPosition(-5.0F, armY, 0.0F)
				.setBox(-armWidth + 1, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor)
				.offsetBoxBy(-0.01f, 0, -0.01f)
				.resizeBox(armWidth + 0.02f, 6.0f, 4.02f)
				.updateVertices()
				.setVisibility(ModelBox.BOTTOM, false);

		leftForeArm = (ModelPartChildPostOffset) new ModelPartChildPostOffset(original, 40, 16 + 6)
				.setPostOffset(0, -4F, -2F)
				.setParent(leftArm)
				.setPosition(0.0F, 4.0F, 2.0F)
				.setMirror(true)
				.setBox(-1.0F, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor)
				.setVisibility(ModelBox.TOP, false);
		leftForeArm.getBox().offsetTextureQuad(leftForeArm, ModelBox.BOTTOM, 0, -6.0f);
		leftArm.setExtension(leftForeArm);
		rightForeArm = (ModelPartChildPostOffset) new ModelPartChildPostOffset(original, 40, 16 + 6)
				.setPostOffset(0, -4F, -2F)
				.setParent(rightArm)
				.setPosition(0.0F, 4.0F, 2.0F)
				.setBox(-armWidth + 1, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor)
				.setVisibility(ModelBox.TOP, false);
		rightForeArm.getBox().offsetTextureQuad(rightForeArm, ModelBox.BOTTOM, 0, -6.0f);
		rightArm.setExtension(rightForeArm);

		// Legs
		original.bipedRightLeg = rightLeg = (ModelPartExtended) new ModelPartExtended(original, 0, 16)
				.setPosition(-1.9F, 12F, 0F)
				.setBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, scaleFactor);
		original.bipedLeftLeg = leftLeg = (ModelPartExtended) new ModelPartExtended(original, 0, 16)
				.setPosition(1.9F, 12.0F, 0.0F)
				.setMirror(true)
				.setBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, scaleFactor);
		leftForeLeg = (ModelPartChild) new ModelPartChild(original, 0, 16 + 6)
				.setParent(leftLeg)
				.setPosition(0, 6.0F, -2.0F)
				.setMirror(true)
				.setBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, scaleFactor)
				.offsetBoxBy(-0.005F, 0F, -0.005F)
				.resizeBox(4.01F, 6.0F, 4.01F)
				.updateVertices();
		leftForeLeg.getBox().offsetTextureQuad(leftForeLeg, ModelBox.BOTTOM, 0, -6.0f);
		leftLeg.setExtension(leftForeLeg);
		rightForeLeg = (ModelPartChild) new ModelPartChild(original, 0, 16 + 6)
				.setParent(rightLeg)
				.setPosition(0, 6.0F, -2.0F)
				.setBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, scaleFactor)
				.offsetBoxBy(-0.005F, 0F, -0.005F)
				.resizeBox(4.01F, 6.0F, 4.01F)
				.updateVertices();
		rightForeLeg.getBox().offsetTextureQuad(rightForeLeg, ModelBox.BOTTOM, 0, -6.0f);
		rightLeg.setExtension(rightForeLeg);

		// Wear
		original.bipedHeadwear = headwear = new ModelPartChild(original, 32, 0).setParent(head);
		headwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, scaleFactor + 0.5F);
		
		return true;
	}
	
	/*
	 * Performs the steps needed to demutate the model.
	 */
	public void demutate(T entityPlayer, RenderLivingBase<T> renderer)
	{
		if (!(renderer.getMainModel() instanceof ModelBiped))
			return;
		M model = (M) renderer.getMainModel();
		
		this.applyVanillaModel(model);
		
		if (this.layerRenderers != null)
		{
			for (int i = 0; i < layerRenderers.size(); ++i)
			{
				this.deswapLayer(renderer, i);
			}
		}
	}
}
