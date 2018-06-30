package net.gobbob.mobends.client.mutators;

import java.util.HashMap;
import java.util.Map;

import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.animation.controller.Controller;
import net.gobbob.mobends.client.model.IModelPart;
import net.gobbob.mobends.client.model.ModelBox;
import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.model.ModelPartChild;
import net.gobbob.mobends.client.model.ModelPartChildExtended;
import net.gobbob.mobends.client.model.ModelPartChildPostOffset;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.EntityDatabase;
import net.gobbob.mobends.data.PlayerData;
import net.gobbob.mobends.util.FieldMiner;
import net.gobbob.mobends.util.GUtil;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

/*
 * Instantiated one per RenderPlayer
 */
public class PlayerMutator extends BipedMutator<AbstractClientPlayer, ModelPlayer>
{
	public static HashMap<RenderPlayer, PlayerMutator> mutatorMap = new HashMap<RenderPlayer, PlayerMutator>();

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

	public boolean hasSmallArms()
	{
		return this.smallArms;
	}
	
	@Override
	public void fetchFields(RenderLivingBase<AbstractClientPlayer> renderer)
	{
		super.fetchFields(renderer);

		// Does the renderer have Small Arms?
		Boolean smallArms = FieldMiner.getObfuscatedValue(renderer, "smallArms", "field_177140_a");
		this.smallArms = smallArms != null ? smallArms : false;
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
	
	public void mutate(AbstractClientPlayer entityPlayer, RenderPlayer renderer)
	{
		fetchFields(renderer);

		ModelPlayer model = renderer.getMainModel();
		float scaleFactor = 0F;
		
		// True, if this renderer wasn't mutated before.
		boolean isModelVanilla = ! (model.bipedBody instanceof IModelPart);
		
		if (isModelVanilla)
		{
			// If this model wasn't mutated before, save it
			// as the vanilla model.
			this.storeVanillaModel(model);
		}
		
		// Swapping layers
		if (this.layerRenderers != null)
		{
			for (int i = 0; i < layerRenderers.size(); ++i)
			{
				swapLayer(renderer, i, isModelVanilla);
			}
		}
		
		this.createParts(model, scaleFactor);
	}

	public void updateModel(AbstractClientPlayer player, RenderPlayer renderer, float partialTicks)
	{
		boolean shouldSit = player.isRiding()
				&& (player.getRidingEntity() != null && player.getRidingEntity().shouldRiderSit());
		float f = GUtil.interpolateRotation(player.prevRenderYawOffset, player.renderYawOffset, partialTicks);
		float f1 = GUtil.interpolateRotation(player.prevRotationYawHead, player.rotationYawHead, partialTicks);
		float yaw = f1 - f;

		if (shouldSit && player.getRidingEntity() instanceof EntityLivingBase)
		{
			EntityLivingBase entitylivingbase = (EntityLivingBase) player.getRidingEntity();
			f = GUtil.interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset,
					partialTicks);
			yaw = f1 - f;
			float f3 = MathHelper.wrapDegrees(yaw);

			if (f3 < -85.0F)
				f3 = -85.0F;
			if (f3 >= 85.0F)
				f3 = 85.0F;

			f = f1 - f3;

			if (f3 * f3 > 2500.0F)
				f += f3 * 0.2F;

			yaw = f1 - f;
		}

		float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
		float f5 = 0.0F;
		float f6 = 0.0F;

		if (!player.isRiding())
		{
			f5 = player.prevLimbSwingAmount + (player.limbSwingAmount - player.prevLimbSwingAmount) * partialTicks;
			f6 = player.limbSwing - player.limbSwingAmount * (1.0F - partialTicks);

			if (player.isChild())
				f6 *= 3.0F;
			if (f5 > 1.0F)
				f5 = 1.0F;
			yaw = f1 - f;
		}

		this.headYaw = yaw;
		this.headPitch = pitch;
		this.limbSwing = f6;
		this.limbSwingAmount = f5;
		performAnimations(player, renderer, partialTicks);
	}

	public void performAnimations(AbstractClientPlayer player, RenderPlayer renderer, float partialTicks)
	{
		EntityData entityData = EntityDatabase.instance.getAndMake(PlayerData.class, player);
		if (!(entityData instanceof PlayerData))
			return;
		PlayerData data = (PlayerData) entityData;
		AnimatedEntity animatedEntity = AnimatedEntity.getForEntity(player);
		float ticks = player.ticksExisted + partialTicks;

		leftForeArmwear.setVisible(leftArmwear.isShowing());
		rightForeArmwear.setVisible(rightArmwear.isShowing());
		leftForeLegwear.setVisible(leftLegwear.isShowing());
		rightForeLegwear.setVisible(rightLegwear.isShowing());

		data.setHeadYaw(this.headYaw);
		data.setHeadPitch(this.headPitch);
		data.setLimbSwing(this.limbSwing);
		data.setLimbSwingAmount(this.limbSwingAmount);

		Controller controller = data.getController();
		if (controller != null && data.canBeUpdated())
		{
			controller.perform(data);
		}

		// Sync up with the EntityData
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
	 * Used to apply the effect of the mutation, or just to update the model
	 * if it was already mutated.
	 * Called from PlayerRenderHandler.beforePlayerRender().
	 */
	public static void apply(RenderLivingBase renderer, EntityLivingBase entity, float partialTicks)
	{
		if (!(renderer instanceof RenderPlayer))
			return;
		if (!(entity instanceof AbstractClientPlayer))
			return;
		
		PlayerMutator mutator = mutatorMap.get(renderer);
		if (!mutatorMap.containsKey(renderer))
		{
			mutator = new PlayerMutator();
			mutator.mutate((AbstractClientPlayer) entity, (RenderPlayer) renderer);
			mutatorMap.put((RenderPlayer) renderer, mutator);
		}

		mutator.updateModel((AbstractClientPlayer) entity, (RenderPlayer) renderer, partialTicks);
	}
	
	/*
	 * Used to reverse the effect of the mutation.
	 * Called from PlayerRenderHandler.beforePlayerRender().
	 */
	public static void deapply(RenderLivingBase renderer, EntityLivingBase entity)
	{
		if (!(renderer instanceof RenderPlayer))
			return;
		if (!(entity instanceof AbstractClientPlayer))
			return;
		
		if (mutatorMap.containsKey(renderer))
		{
			PlayerMutator mutator = mutatorMap.get(renderer);
			mutator.demutate((AbstractClientPlayer) entity, renderer);
			mutatorMap.remove(renderer);
		}
	}

	/*
	 * Used to refresh the mutators in case of real-time changes during development.
	 */
	public static void refresh()
	{
		for (Map.Entry<RenderPlayer, PlayerMutator> mutator : mutatorMap.entrySet())
		{
			mutator.getValue().mutate(null, mutator.getKey());
			if (mutator.getValue().layerArmor != null)
				mutator.getValue().layerArmor.initArmor();
		}
	}

	public static PlayerMutator getMutatorForRenderer(Render render)
	{
		return mutatorMap.get(render);
	}
}
