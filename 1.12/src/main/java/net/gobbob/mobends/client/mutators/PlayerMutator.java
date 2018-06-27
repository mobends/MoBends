package net.gobbob.mobends.client.mutators;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.animation.controller.Controller;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.client.model.IBendsModel;
import net.gobbob.mobends.client.model.IModelPart;
import net.gobbob.mobends.client.model.ModelBox;
import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.model.ModelPartChild;
import net.gobbob.mobends.client.model.ModelPartChildExtended;
import net.gobbob.mobends.client.model.ModelPartExtended;
import net.gobbob.mobends.client.model.ModelPartPostOffset;
import net.gobbob.mobends.client.model.ModelPartTransform;
import net.gobbob.mobends.client.renderer.entity.layers.LayerCustomHeldItem;
import net.gobbob.mobends.client.renderer.entity.layers.LayerCustomBipedArmor;
import net.gobbob.mobends.data.PlayerData;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.EntityDatabase;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.util.FieldMiner;
import net.gobbob.mobends.util.GUtil;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

/*
 * Instantiated one per RenderPlayer
 */
public class PlayerMutator extends BipedMutator<AbstractClientPlayer>
{
	public static HashMap<RenderPlayer, PlayerMutator> mutatorMap = new HashMap<RenderPlayer, PlayerMutator>();

	protected ModelPlayer vanillaModel;

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
			this.vanillaModel = new ModelPlayer(scaleFactor, this.smallArms);
			this.vanillaModel.bipedBody = model.bipedBody;
			this.vanillaModel.bipedBodyWear = model.bipedBodyWear;
			this.vanillaModel.bipedHead = model.bipedHead;
			this.vanillaModel.bipedHeadwear = model.bipedHeadwear;
			this.vanillaModel.bipedLeftArm = model.bipedLeftArm;
			this.vanillaModel.bipedLeftArmwear = model.bipedLeftArmwear;
			this.vanillaModel.bipedLeftLeg = model.bipedLeftLeg;
			this.vanillaModel.bipedLeftLegwear = model.bipedLeftLegwear;
			this.vanillaModel.bipedRightArm = model.bipedRightArm;
			this.vanillaModel.bipedRightArmwear = model.bipedRightArmwear;
			this.vanillaModel.bipedRightLeg = model.bipedRightLeg;
			this.vanillaModel.bipedRightLegwear = model.bipedRightLegwear;
		}
		
		if (this.layerRenderers != null)
		{
			for (int i = 0; i < layerRenderers.size(); ++i)
			{
				LayerRenderer<EntityLivingBase> layer = layerRenderers.get(i);
				if (layer instanceof LayerBipedArmor)
				{
					this.layerArmor = new LayerCustomBipedArmor(renderer);
					if (isModelVanilla)
						this.vanillaLayerArmor = (LayerBipedArmor) layerRenderers.get(i);
					layerRenderers.set(i, this.layerArmor);
				}
				else if (layer instanceof LayerHeldItem)
				{
					this.layerHeldItem = new LayerCustomHeldItem(renderer);
					if (isModelVanilla)
						this.vanillaLayerHeldItem = (LayerHeldItem) layerRenderers.get(i);
					layerRenderers.set(i, this.layerHeldItem);
				}
			}
		}
		
		// Body
		model.bipedBody = body = (ModelPartPostOffset) new ModelPartPostOffset(model, 16, 16)
				.setPostOffset(0.0F, -12.0F, 0.0F).setPosition(0.0F, 12.0F, 0.0F);
		body.addBox(-4.0F, -12.0F, -2.0F, 8, 12, 4, scaleFactor);

		// Head
		model.bipedHead = head = (ModelPartChild) new ModelPartChild(model, 0, 0).setParent(body)
				.setHideLikeParent(false).setPosition(0.0F, -12.0F, 0.0F);
		head.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, scaleFactor);

		// Arms
		int armWidth = this.smallArms ? 3 : 4;
		float armHeight = this.smallArms ? -9.5F : -10F;

		model.bipedLeftArm = leftArm = (ModelPartChildExtended) new ModelPartChildExtended(model, 32, 48)
				.setParent(body).setHideLikeParent(false).setPosition(5.0F, armHeight, 0.0F);
		leftArm.addModelBox(-1.0F, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor).setVisibility(ModelBox.BOTTOM, false);
		model.bipedRightArm = rightArm = (ModelPartChildExtended) new ModelPartChildExtended(model, 40, 16)
				.setParent(body).setHideLikeParent(false).setPosition(-5.0F, armHeight, 0.0F);
		rightArm.addModelBox(-armWidth + 1, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor).setVisibility(ModelBox.BOTTOM,
				false);

		rightArm.offsetBoxBy(-0.01f, 0, -0.01f).resizeBox(armWidth + 0.02f, 6.0f, 4.02f).updateVertices();
		leftArm.offsetBoxBy(-0.01f, 0, -0.01f).resizeBox(armWidth + 0.02f, 6.0f, 4.02f).updateVertices();

		leftForeArm = (ModelPartChildExtended) new ModelPartChildExtended(model, 32, 48 + 6).setParent(leftArm);
		leftForeArm.addModelBox(-1.0F, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor).setVisibility(ModelBox.TOP, false);
		leftForeArm.setRotationPoint(0.0F, 4.0F, 2.0F);
		leftForeArm.getBox().offsetTextureQuad(leftForeArm, ModelBox.BOTTOM, 0, -6.0f);
		leftArm.setExtension(leftForeArm);
		rightForeArm = (ModelPartChildExtended) new ModelPartChildExtended(model, 40, 16 + 6).setParent(rightArm);
		rightForeArm.addModelBox(-armWidth + 1, 0.0F, -4.0F, armWidth, 6, 4, scaleFactor).setVisibility(ModelBox.TOP,
				false);
		rightForeArm.setRotationPoint(0.0F, 4.0F, 2.0F);
		rightForeArm.getBox().offsetTextureQuad(rightForeArm, ModelBox.BOTTOM, 0, -6.0f);
		rightArm.setExtension(rightForeArm);

		// Items
		leftItemTransform = new ModelPartTransform();
		leftForeArm.setExtension(leftItemTransform);
		rightItemTransform = new ModelPartTransform();
		rightForeArm.setExtension(rightItemTransform);

		// Legs
		model.bipedRightLeg = rightLeg = (ModelPartExtended) new ModelPartExtended(model, 0, 16)
				.setPosition(-1.9F, 12F, 0F)
				.setBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, scaleFactor);
		model.bipedLeftLeg = leftLeg = (ModelPartExtended) new ModelPartExtended(model, 16, 48)
				.setPosition(1.9F, 12.0F, 0.0F)
				.setBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, scaleFactor);
		leftForeLeg = (ModelPartChild) new ModelPartChild(model, 16, 48 + 6)
				.setParent(leftLeg)
				.setPosition(0, 6.0F, -2.0F)
				.setBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, scaleFactor)
				.offsetBoxBy(-0.005F, 0F, -0.005F)
				.resizeBox(4.01F, 6.0F, 4.01F)
				.updateVertices();
		leftForeLeg.getBox().offsetTextureQuad(leftForeLeg, ModelBox.BOTTOM, 0, -6.0f);
		leftLeg.setExtension(leftForeLeg);
		rightForeLeg = (ModelPartChild) new ModelPartChild(model, 0, 16 + 6)
				.setParent(rightLeg)
				.setPosition(0, 6.0F, -2.0F)
				.setBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, scaleFactor)
				.offsetBoxBy(-0.005F, 0F, -0.005F)
				.resizeBox(4.01F, 6.0F, 4.01F)
				.updateVertices();
		rightForeLeg.getBox().offsetTextureQuad(rightForeLeg, ModelBox.BOTTOM, 0, -6.0f);
		rightLeg.setExtension(rightForeLeg);

		// Wear
		model.bipedHeadwear = headwear = new ModelPartChild(model, 32, 0).setParent(head);
		headwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, scaleFactor + 0.5F);
		model.bipedBodyWear = bodywear = new ModelPartChild(model, 16, 32).setParent(body);
		bodywear.addBox(-4F, -12F, -2F, 8, 12, 4, scaleFactor + 0.25F);
		
		model.bipedLeftArmwear = leftArmwear = (ModelPartChild) new ModelPartChild(model, 48, 48)
				.setParent(leftArm)
				.setBox(-1.0F, -2.0F, -2.0F, armWidth, 6, 4, scaleFactor + 0.25F)
				.setHeight(5.75F)
				.offsetBoxBy(-0.0025F, 0F, -0.0025F)
				.resizeBox(armWidth + 0.005F, 5.75F, 4.005F)
				.updateVertices()
				.setVisibility(ModelBox.BOTTOM, false);
		model.bipedRightArmwear = rightArmwear = (ModelPartChild) new ModelPartChild(model, 40, 32)
				.setParent(rightArm)
				.setBox(-armWidth + 1, -2F, -2F, armWidth, 6, 4, scaleFactor + 0.25F)
				.setHeight(5.75F)
				.offsetBoxBy(-0.0025F, 0F, -0.0025F)
				.resizeBox(armWidth + 0.005F, 5.75F, 4.005F)
				.updateVertices()
				.setVisibility(ModelBox.BOTTOM, false);

		leftForeArmwear = new ModelPart(model, 48, 48 + 6)
				.setBox(-1F, 0F, -4F, armWidth, 6, 4, scaleFactor + 0.25F)
				.setHeight(5.75F)
				.offsetBoxBy(-0.005F, 0.25F, -0.005F)
				.resizeBox(armWidth + 0.01F, 5.75F, 4.01F)
				.updateVertices()
				.setVisibility(ModelBox.TOP, false);
		leftForeArmwear.getBox().offsetTextureQuad(leftForeArmwear, ModelBox.BOTTOM, 0, -6F);
		leftForeArm.addChild(leftForeArmwear);
		
		rightForeArmwear = new ModelPart(model, 40, 32 + 6)
				.setBox(-armWidth + 1, 0F, -4F, armWidth, 6, 4, scaleFactor + 0.25F)
				.setHeight(5.75F)
				.offsetBoxBy(-0.005F, 0.25F, -0.005F)
				.resizeBox(armWidth + 0.01F, 5.75F, 4.01F)
				.updateVertices()
				.setVisibility(ModelBox.TOP, false);
		rightForeArmwear.getBox().offsetTextureQuad(rightForeArmwear, ModelBox.BOTTOM, 0, -6F);
		rightForeArm.addChild(rightForeArmwear);

		model.bipedLeftLegwear = leftLegwear = (ModelPartChild) new ModelPartChild(model, 0, 48)
				.setParent(leftLeg)
				.setBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, scaleFactor + 0.25F)
				.setHeight(5.75F)
				.updateVertices()
				.setVisibility(ModelBox.BOTTOM, false);
		model.bipedRightLegwear = rightLegwear = (ModelPartChild) new ModelPartChild(model, 0, 32)
				.setParent(rightLeg)
				.setBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, scaleFactor + 0.25F)
				.setHeight(5.75F)
				.updateVertices()
				.setVisibility(ModelBox.BOTTOM, false);

		leftForeLegwear = new ModelPart(model, 0, 48 + 6)
				.setBox(-2F, 0F, 0F, 4, 6, 4, scaleFactor + 0.25F)
				.setHeight(5.75F)
				.offsetBoxBy(-0.005F, 0.25F, -0.005F)
				.resizeBox(4.01F, 5.75F, 4.01F)
				.updateVertices()
				.setVisibility(ModelBox.TOP, false);
		leftForeLegwear.getBox().offsetTextureQuad(leftForeLegwear, ModelBox.BOTTOM, 0, -6F);
		leftForeLeg.addChild(leftForeLegwear);
		
		rightForeLegwear = new ModelPart(model, 0, 32 + 6)
				.setBox(-2F, 0F, 0F, 4, 6, 4, scaleFactor + 0.25F)
				.setHeight(5.75F)
				.offsetBoxBy(-0.005F, 0.25F, -0.005F)
				.resizeBox(4.01F, 5.75F, 4.01F)
				.updateVertices()
				.setVisibility(ModelBox.TOP, false);
		rightForeLegwear.getBox().offsetTextureQuad(rightForeLegwear, ModelBox.BOTTOM, 0, -6F);
		rightForeLeg.addChild(rightForeLegwear);
		
		nameToPartMap = new HashMap<String, IModelPart>();
		nameToPartMap.put("body", body);
		nameToPartMap.put("head", head);
		nameToPartMap.put("leftArm", leftArm);
		nameToPartMap.put("rightArm", rightArm);
		nameToPartMap.put("leftLeg", leftLeg);
		nameToPartMap.put("rightLeg", rightLeg);
		nameToPartMap.put("leftForeArm", leftForeArm);
		nameToPartMap.put("rightForeArm", rightForeArm);
		nameToPartMap.put("leftForeLeg", leftForeLeg);
		nameToPartMap.put("rightForeLeg", rightForeLeg);
	}
	
	public void demutate(AbstractClientPlayer entityPlayer, RenderPlayer renderer)
	{
		ModelPlayer model = renderer.getMainModel();
		
		model.bipedBody = this.vanillaModel.bipedBody;
		model.bipedBodyWear = this.vanillaModel.bipedBodyWear;
		model.bipedHead = this.vanillaModel.bipedHead;
		model.bipedHeadwear = this.vanillaModel.bipedHeadwear;
		model.bipedLeftArm = this.vanillaModel.bipedLeftArm;
		model.bipedLeftArmwear = this.vanillaModel.bipedLeftArmwear;
		model.bipedLeftLeg = this.vanillaModel.bipedLeftLeg;
		model.bipedLeftLegwear = this.vanillaModel.bipedLeftLegwear;
		model.bipedRightArm = this.vanillaModel.bipedRightArm;
		model.bipedRightArmwear = this.vanillaModel.bipedRightArmwear;
		model.bipedRightLeg = this.vanillaModel.bipedRightLeg;
		model.bipedRightLegwear = this.vanillaModel.bipedRightLegwear;
		
		if (this.layerRenderers != null)
		{
			for (int i = 0; i < layerRenderers.size(); ++i)
			{
				LayerRenderer<EntityLivingBase> layer = layerRenderers.get(i);
				if (layer instanceof LayerCustomBipedArmor)
				{
					layerRenderers.set(i, this.vanillaLayerArmor);
				}
				else if (layer instanceof LayerCustomHeldItem)
				{
					layerRenderers.set(i, this.vanillaLayerHeldItem);
				}
			}
		}
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
		AnimatedEntity animatedEntity = AnimatedEntity.getByEntity(player);
		float ticks = player.ticksExisted + partialTicks;

		leftForeArmwear.setVisible(leftArmwear.isShowing());
		rightForeArmwear.setVisible(rightArmwear.isShowing());
		leftForeLegwear.setVisible(leftLegwear.isShowing());
		rightForeLegwear.setVisible(rightLegwear.isShowing());
		
		leftItemTransform.position.setY(-3);
		rightItemTransform.position.setY(-3);

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
	public static void apply(RenderPlayer renderer, AbstractClientPlayer entityPlayer, float partialTicks)
	{
		PlayerMutator mutator = mutatorMap.get(renderer);
		if (!mutatorMap.containsKey(renderer))
		{
			mutator = new PlayerMutator();
			mutator.mutate(entityPlayer, renderer);
			mutatorMap.put(renderer, mutator);
		}

		mutator.updateModel(entityPlayer, renderer, partialTicks);
	}
	
	/*
	 * Used to reverse the effect of the mutation.
	 * Called from PlayerRenderHandler.beforePlayerRender().
	 */
	public static void deapply(RenderPlayer renderer, AbstractClientPlayer entityPlayer, float partialTicks)
	{
		if (mutatorMap.containsKey(renderer))
		{
			PlayerMutator mutator = mutatorMap.get(renderer);
			mutator.demutate(entityPlayer, renderer);
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
