package net.gobbob.mobends.standard.client.mutators;

import java.util.function.Function;

import net.gobbob.mobends.core.EntityDatabase;
import net.gobbob.mobends.core.animation.controller.Controller;
import net.gobbob.mobends.core.client.model.IModelPart;
import net.gobbob.mobends.core.client.model.ModelBox;
import net.gobbob.mobends.core.client.model.ModelPart;
import net.gobbob.mobends.core.client.model.ModelPartChild;
import net.gobbob.mobends.core.client.model.ModelPartChildExtended;
import net.gobbob.mobends.core.client.model.ModelPartChildPostOffset;
import net.gobbob.mobends.standard.data.PlayerData;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;

/*
 * Instantiated one per RenderPlayer
 */
public class PlayerMutator extends BipedMutator<PlayerData, AbstractClientPlayer, ModelPlayer>
{

	protected ModelPartChild bodywear;
	protected ModelPartChild leftArmwear;
	protected ModelPartChild rightArmwear;
	protected ModelPart leftForeArmwear;
	protected ModelPart rightForeArmwear;
	protected ModelPartChild leftLegwear;
	protected ModelPartChild rightLegwear;
	protected ModelPart leftForeLegwear;
	protected ModelPart rightForeLegwear;

	protected boolean smallArms;

	public PlayerMutator()
	{
		super(PlayerData::new);
	}
	
	public boolean hasSmallArms()
	{
		return this.smallArms;
	}
	
	@Override
	public void fetchFields(RenderLivingBase<? extends AbstractClientPlayer> renderer)
	{
		super.fetchFields(renderer);

		// Does the renderer have Small Arms?
		this.smallArms = ((RenderPlayer) renderer).smallArms;
	}

	@Override
	public void storeVanillaModel(ModelPlayer model)
	{
		ModelPlayer vanillaModel = new ModelPlayer(0.0F, this.smallArms);
		this.vanillaModel = vanillaModel;
		
		// Calling the super method here, since it
		// requires the vanillaModel property to be
		// set.
		super.storeVanillaModel(model);
		
		vanillaModel.bipedBodyWear = model.bipedBodyWear;
		vanillaModel.bipedLeftArmwear = model.bipedLeftArmwear;
		vanillaModel.bipedLeftLegwear = model.bipedLeftLegwear;
		vanillaModel.bipedRightArmwear = model.bipedRightArmwear;
		vanillaModel.bipedRightLegwear = model.bipedRightLegwear;
	}
	
	@Override
	public void applyVanillaModel(ModelPlayer model)
	{
		super.applyVanillaModel(model);
		
		model.bipedBodyWear = vanillaModel.bipedBodyWear;
		model.bipedLeftArmwear = vanillaModel.bipedLeftArmwear;
		model.bipedLeftLegwear = vanillaModel.bipedLeftLegwear;
		model.bipedRightArmwear = vanillaModel.bipedRightArmwear;
		model.bipedRightLegwear = vanillaModel.bipedRightLegwear;
	}
	
	@Override
	public boolean createParts(ModelPlayer original, float scaleFactor)
	{
		super.createParts(original, scaleFactor);
		
		// Arms
		int armWidth = this.smallArms ? 3 : 4;
		float armY = this.smallArms ? -9.5F : -10F;
		
		original.bipedLeftArm = leftArm = (ModelPartChildExtended) new ModelPartChildExtended(original, 32, 48)
				.setParent(body)
				.setHideLikeParent(false)
				.setPosition(5.0F, armY, 0.0F)
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
		
		leftForeArm = (ModelPartChildPostOffset) new ModelPartChildPostOffset(original, 32, 48 + 6)
				.setPostOffset(0, -4F, -2F)
				.setParent(leftArm)
				.setPosition(0.0F, 4.0F, 2.0F)
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
		
		// Wear
		original.bipedBodyWear = bodywear = new ModelPartChild(original, 16, 32).setParent(body);
		bodywear.addBox(-4F, -12F, -2F, 8, 12, 4, scaleFactor + 0.25F);
		
		original.bipedLeftArmwear = leftArmwear = (ModelPartChild) new ModelPartChild(original, 48, 48)
				.setParent(leftArm)
				.setBox(-1.0F, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor + 0.25F)
				.setHeight(5.75F)
				.offsetBoxBy(-0.0025F, 0F, -0.0025F)
				.resizeBox(armWidth + 0.005F, 5.75F, 4.005F)
				.updateVertices()
				.setVisibility(ModelBox.BOTTOM, false);
		original.bipedRightArmwear = rightArmwear = (ModelPartChild) new ModelPartChild(original, 40, 32)
				.setParent(rightArm)
				.setBox(-armWidth + 1, -2F, -2F, armWidth, 6, 4, scaleFactor + 0.25F)
				.setHeight(5.75F)
				.offsetBoxBy(-0.0025F, 0F, -0.0025F)
				.resizeBox(armWidth + 0.005F, 5.75F, 4.005F)
				.updateVertices()
				.setVisibility(ModelBox.BOTTOM, false);

		leftForeArmwear = new ModelPart(original, 48, 48 + 6)
				.setBox(-1F, 0F, -4F, armWidth, 6, 4, scaleFactor + 0.25F)
				.setHeight(5.75F)
				.offsetBoxBy(-0.005F, 0.25F, -0.005F)
				.resizeBox(armWidth + 0.01F, 5.75F, 4.01F)
				.updateVertices()
				.setVisibility(ModelBox.TOP, false);
		leftForeArmwear.getBox().offsetTextureQuad(leftForeArmwear, ModelBox.BOTTOM, 0, -6F);
		leftForeArm.addChild(leftForeArmwear);
		
		rightForeArmwear = new ModelPart(original, 40, 32 + 6)
				.setBox(-armWidth + 1, 0F, -4F, armWidth, 6, 4, scaleFactor + 0.25F)
				.setHeight(5.75F)
				.offsetBoxBy(-0.005F, 0.25F, -0.005F)
				.resizeBox(armWidth + 0.01F, 5.75F, 4.01F)
				.updateVertices()
				.setVisibility(ModelBox.TOP, false);
		rightForeArmwear.getBox().offsetTextureQuad(rightForeArmwear, ModelBox.BOTTOM, 0, -6F);
		rightForeArm.addChild(rightForeArmwear);

		original.bipedLeftLegwear = leftLegwear = (ModelPartChild) new ModelPartChild(original, 0, 48)
				.setParent(leftLeg)
				.setBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, scaleFactor + 0.25F)
				.setHeight(5.75F)
				.updateVertices()
				.setVisibility(ModelBox.BOTTOM, false);
		original.bipedRightLegwear = rightLegwear = (ModelPartChild) new ModelPartChild(original, 0, 32)
				.setParent(rightLeg)
				.setBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, scaleFactor + 0.25F)
				.setHeight(5.75F)
				.updateVertices()
				.setVisibility(ModelBox.BOTTOM, false);

		leftForeLegwear = new ModelPart(original, 0, 48 + 6)
				.setBox(-2F, 0F, 0F, 4, 6, 4, scaleFactor + 0.25F)
				.setHeight(5.75F)
				.offsetBoxBy(-0.005F, 0.25F, -0.005F)
				.resizeBox(4.01F, 5.75F, 4.01F)
				.updateVertices()
				.setVisibility(ModelBox.TOP, false);
		leftForeLegwear.getBox().offsetTextureQuad(leftForeLegwear, ModelBox.BOTTOM, 0, -6F);
		leftForeLeg.addChild(leftForeLegwear);
		
		rightForeLegwear = new ModelPart(original, 0, 32 + 6)
				.setBox(-2F, 0F, 0F, 4, 6, 4, scaleFactor + 0.25F)
				.setHeight(5.75F)
				.offsetBoxBy(-0.005F, 0.25F, -0.005F)
				.resizeBox(4.01F, 5.75F, 4.01F)
				.updateVertices()
				.setVisibility(ModelBox.TOP, false);
		rightForeLegwear.getBox().offsetTextureQuad(rightForeLegwear, ModelBox.BOTTOM, 0, -6F);
		rightForeLeg.addChild(rightForeLegwear);
		
		return true;
	}

	@Override
	protected void performAnimations(PlayerData data, RenderLivingBase<? extends AbstractClientPlayer> renderer, float partialTicks)
	{
		leftForeArmwear.setVisible(leftArmwear.isShowing());
		rightForeArmwear.setVisible(rightArmwear.isShowing());
		leftForeLegwear.setVisible(leftLegwear.isShowing());
		rightForeLegwear.setVisible(rightLegwear.isShowing());
		
		super.performAnimations(data, renderer, partialTicks);
	}
	
	@Override
	public void postRefresh()
	{
		if (this.layerArmor != null)
			this.layerArmor.initArmor();
	}
	
	/*
	 * Called before the first person hand is rendered, so the mutator can pose it
	 * in any way.
	 */
	public void poseForFirstPersonView()
	{
		this.body.rotation.identity();
		this.rightArm.rotation.identity();
		this.rightForeArm.rotation.identity();
		this.leftArm.rotation.identity();
		this.leftForeArm.rotation.identity();
	}

	@Override
	public boolean isModelVanilla(ModelPlayer model)
	{
		return !(model.bipedBody instanceof IModelPart);
	}

	@Override
	public boolean isModelEligible(ModelBase model)
	{
		return model instanceof ModelPlayer;
	}
	
}
