package net.gobbob.mobends.standard.mutators;

import net.gobbob.mobends.core.client.model.*;
import net.gobbob.mobends.core.data.IEntityDataFactory;
import net.gobbob.mobends.core.mutators.Mutator;
import net.gobbob.mobends.standard.client.renderer.entity.layers.LayerCustomBipedArmor;
import net.gobbob.mobends.standard.client.renderer.entity.layers.LayerCustomHeldItem;
import net.gobbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;

public abstract class BipedMutator<D extends BipedEntityData<E>,
								   E extends EntityLivingBase,
								   M extends ModelBiped>
								  extends Mutator<D, E, M>
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

	protected LayerCustomBipedArmor 	layerArmor;
	protected LayerBipedArmor 			layerArmorVanilla;
	protected LayerCustomHeldItem 		layerHeldItem;
	protected LayerHeldItem 			layerHeldItemVanilla;
	protected LayerCustomHead 			layerCustomHead;
	protected LayerCustomHead 			layerCustomHeadVanilla;
	

	public BipedMutator(IEntityDataFactory<E> dataFactory)
	{
		super(dataFactory);
	}

	/*
	 * Used to store the model parameter as the
	 * vanilla model, so then the mutation can be
	 * reversed.
	 */
	@Override
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
	@Override
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
	@Override
	public void swapLayer(RenderLivingBase<? extends E> renderer, int index, boolean isModelVanilla)
	{
		LayerRenderer<EntityLivingBase> layer = layerRenderers.get(index);
		if (layer instanceof LayerBipedArmor)
		{
			this.layerArmor = new LayerCustomBipedArmor(renderer);
			if (isModelVanilla)
				this.layerArmorVanilla = (LayerBipedArmor) layer;
			layerRenderers.set(index, this.layerArmor);
		}
		else if (layer instanceof LayerHeldItem)
		{
			this.layerHeldItem = new LayerCustomHeldItem(renderer);
			if (isModelVanilla)
				this.layerHeldItemVanilla = (LayerHeldItem) layer;
			layerRenderers.set(index, this.layerHeldItem);
		}
		else if (layer instanceof LayerCustomHead)
		{
			this.layerCustomHead = new LayerCustomHead(this.head);
			if (isModelVanilla)
				this.layerCustomHeadVanilla = (LayerCustomHead) layer;
			layerRenderers.set(index, this.layerCustomHead);
		}
	}
	
	/*
	 * Swaps the custom layers back with the vanilla layers.
	 * Used to demutate the model.
	 */
	@Override
	public void deswapLayer(RenderLivingBase<? extends E> renderer, int index)
	{
		LayerRenderer<EntityLivingBase> layer = layerRenderers.get(index);
		if (layer instanceof LayerCustomBipedArmor)
		{
			layerRenderers.set(index, this.layerArmorVanilla);
		}
		else if (layer instanceof LayerCustomHeldItem)
		{
			layerRenderers.set(index, this.layerHeldItemVanilla);
		}
		else if (layer instanceof LayerCustomHead)
		{
			layerRenderers.set(index, this.layerCustomHeadVanilla);
		}
	}
	
	/*
	 * Creates all the custom parts you need! It swaps all the
	 * original parts with newly created custom parts.
	 */
	@Override
	public boolean createParts(M original, float scaleFactor)
	{
		// Body
		original.bipedBody = this.body = (ModelPartPostOffset) new ModelPartPostOffset(original, 16, 16)
				.setPostOffset(0.0F, -12.0F, 0.0F)
				.setPosition(0.0F, 12.0F, 0.0F);
		this.body.addBox(-4.0F, -12.0F, -2.0F, 8, 12, 4, scaleFactor);
		
		// Head
		original.bipedHead = this.head = (ModelPartChild) new ModelPartChild(original, 0, 0)
				.setHideLikeParent(false)
				.setParent(body)
				.setPosition(0.0F, -12.0F, 0.0F);
		this.head.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, scaleFactor);

		// Arms
		int armWidth = 4;
		float armY = -10F;

		original.bipedLeftArm = leftArm = (ModelPartChildExtended) new ModelPartChildExtended(original, 40, 16)
				.setHideLikeParent(false)
				.setParent(body)
				.setPosition(5.0F, armY, 0.0F)
				.setMirror(true);
		this.leftArm.developBox(-1.0F, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor)
				.inflate(0.01F, 0, 0.01F)
				.hideFace(BoxSide.BOTTOM)
				.create();
		
		original.bipedRightArm = rightArm = (ModelPartChildExtended) new ModelPartChildExtended(original, 40, 16)
				.setHideLikeParent(false)
				.setParent(body)
				.setPosition(-5.0F, armY, 0.0F);
		this.rightArm.developBox(-armWidth + 1, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor)
				.inflate(0.01F, 0, 0.1F)
				.hideFace(BoxSide.BOTTOM)
				.create();

		this.leftForeArm = (ModelPartChildPostOffset) new ModelPartChildPostOffset(original, 40, 16 + 6)
				.setPostOffset(0, -4F, -2F)
				.setParent(leftArm)
				.setPosition(0.0F, 4.0F, 2.0F)
				.setMirror(true);
		this.leftForeArm.developBox(-1.0F, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor)
				.hideFace(BoxSide.TOP)
				.offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
				.create();
		
		this.leftArm.setExtension(this.leftForeArm);
		this.rightForeArm = (ModelPartChildPostOffset) new ModelPartChildPostOffset(original, 40, 16 + 6)
				.setPostOffset(0, -4F, -2F)
				.setParent(rightArm)
				.setPosition(0.0F, 4.0F, 2.0F);
		this.rightForeArm.developBox(-armWidth + 1, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor)
				.hideFace(BoxSide.TOP)
				.offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
				.create();
		this.rightArm.setExtension(this.rightForeArm);

		// Legs
		original.bipedRightLeg = rightLeg = (ModelPartExtended) new ModelPartExtended(original, 0, 16)
				.setPosition(0.0F, 12F, 0F);
		this.rightLeg.addBox(-3.9F, 0.0F, -2.0F, 4, 6, 4, scaleFactor);
		original.bipedLeftLeg = leftLeg = (ModelPartExtended) new ModelPartExtended(original, 0, 16)
				.setPosition(0.0F, 12.0F, 0.0F)
				.setMirror(true);
		this.leftLeg.addBox(-0.1F, 0.0F, -2.0F, 4, 6, 4, scaleFactor);
		leftForeLeg = (ModelPartChild) new ModelPartChild(original, 0, 16 + 6)
				.setParent(leftLeg)
				.setPosition(0, 6.0F, -2.0F)
				.setMirror(true);
		
		this.leftForeLeg.developBox(-0.1F, 0.0F, 0.0F, 4, 6, 4, scaleFactor)
				.inflate(0.01F, 0, 0.01F)
				.offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
				.create();
		this.leftLeg.setExtension(this.leftForeLeg);
		rightForeLeg = (ModelPartChild) new ModelPartChild(original, 0, 16 + 6)
				.setParent(rightLeg)
				.setPosition(0, 6.0F, -2.0F);
		
		rightForeLeg.developBox(-3.9F, 0.0F, 0.0F, 4, 6, 4, scaleFactor)
				.inflate(0.01F, 0, 0.01F)
				.offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
				.create();
		rightLeg.setExtension(rightForeLeg);

		// Wear
		original.bipedHeadwear = headwear = (ModelPartChild) new ModelPartChild(original, 32, 0).setParent(head);
		headwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, scaleFactor + 0.5F);
		
		return true;
	}
	
	@Override
	public void syncUpWithData(D data)
	{
		head.syncUp(data.head);
		body.syncUp(data.body);
		leftArm.syncUp(data.leftArm);
		rightArm.syncUp(data.rightArm);
		leftLeg.syncUp(data.leftLeg);
		rightLeg.syncUp(data.rightLeg);
		leftForeArm.syncUp(data.leftForeArm);
		rightForeArm.syncUp(data.rightForeArm);
		leftForeLeg.syncUp(data.leftForeLeg);
		rightForeLeg.syncUp(data.rightForeLeg);
	}
	
	/*
	 * True, if this renderer wasn't mutated before.
	 */
	@Override
	public boolean isModelVanilla(ModelBiped model)
	{
		return !(model.bipedBody instanceof IModelPart);
	}

}
