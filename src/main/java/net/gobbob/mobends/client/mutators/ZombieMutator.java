package net.gobbob.mobends.client.mutators;

import java.util.HashMap;
import java.util.Map;

import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.animation.controller.Controller;
import net.gobbob.mobends.client.model.IModelPart;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.EntityDatabase;
import net.gobbob.mobends.data.ZombieData;
import net.gobbob.mobends.util.GUtil;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RenderZombie;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.math.MathHelper;

public class ZombieMutator extends BipedMutator<EntityZombie, ModelZombie>
{
	public static HashMap<RenderZombie, ZombieMutator> mutatorMap = new HashMap<RenderZombie, ZombieMutator>();
	
	// Should the height of the texture be 64 or 32(half)?
	protected boolean halfTexture = false;
	
	@Override
	public void fetchFields(RenderLivingBase<EntityZombie> renderer)
	{
		super.fetchFields(renderer);

		if (renderer.getMainModel() instanceof ModelZombie)
		{
			ModelZombie model = (ModelZombie) renderer.getMainModel();
			
			this.halfTexture = model.textureHeight == 32;
		}
	}
	
	@Override
	public void storeVanillaModel(ModelZombie model)
	{
		ModelZombie vanillaModel = new ModelZombie(0.0F, this.halfTexture);
		this.vanillaModel = vanillaModel;
		
		// Calling the super method here, since it
		// requires the vanillaModel property to be
		// set.
		super.storeVanillaModel(model);
	}
	
	public boolean mutate(EntityZombie zombie, RenderZombie renderer)
	{
		fetchFields(renderer);

		if (!(renderer.getMainModel() instanceof ModelZombie))
			// The mutation was unsuccessful
			return false;
		
		ModelZombie model = (ModelZombie) renderer.getMainModel();
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
				this.swapLayer(renderer, i, isModelVanilla);
			}
		}
		
		this.createParts(model, scaleFactor);
		
		return true;
	}

	public void updateModel(EntityZombie zombie, RenderZombie renderer, float partialTicks)
	{
		boolean shouldSit = zombie.isRiding()
				&& (zombie.getRidingEntity() != null && zombie.getRidingEntity().shouldRiderSit());
		float f = GUtil.interpolateRotation(zombie.prevRenderYawOffset, zombie.renderYawOffset, partialTicks);
		float f1 = GUtil.interpolateRotation(zombie.prevRotationYawHead, zombie.rotationYawHead, partialTicks);
		float yaw = f1 - f;

		if (shouldSit && zombie.getRidingEntity() instanceof EntityLivingBase)
		{
			EntityLivingBase entitylivingbase = (EntityLivingBase) zombie.getRidingEntity();
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

		float pitch = zombie.prevRotationPitch + (zombie.rotationPitch - zombie.prevRotationPitch) * partialTicks;
		float f5 = 0.0F;
		float f6 = 0.0F;

		if (!zombie.isRiding())
		{
			f5 = zombie.prevLimbSwingAmount + (zombie.limbSwingAmount - zombie.prevLimbSwingAmount) * partialTicks;
			f6 = zombie.limbSwing - zombie.limbSwingAmount * (1.0F - partialTicks);

			if (zombie.isChild())
				f6 *= 3.0F;
			if (f5 > 1.0F)
				f5 = 1.0F;
			yaw = f1 - f;
		}

		this.headYaw = yaw;
		this.headPitch = pitch;
		this.limbSwing = f6;
		this.limbSwingAmount = f5;
		
		performAnimations(zombie, renderer, partialTicks);
	}

	public void performAnimations(EntityZombie zombie, RenderZombie renderer, float partialTicks)
	{
		EntityData entityData = EntityDatabase.instance.getAndMake(ZombieData.class, zombie);
		if (!(entityData instanceof ZombieData))
			return;
		ZombieData data = (ZombieData) entityData;
		AnimatedEntity animatedEntity = AnimatedEntity.getForEntity(zombie);
		float ticks = zombie.ticksExisted + partialTicks;
		
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
	public static void apply(RenderLivingBase renderer, EntityLivingBase entity,
			float partialTicks)
	{
		if (!(renderer instanceof RenderZombie))
			return;
		if (!(entity instanceof EntityZombie))
			return;
		
		ZombieMutator mutator = mutatorMap.get(renderer);
		if (!mutatorMap.containsKey(renderer))
		{
			mutator = new ZombieMutator();
			mutator.mutate((EntityZombie) entity, (RenderZombie) renderer);
			mutatorMap.put((RenderZombie) renderer, mutator);
		}

		mutator.updateModel((EntityZombie) entity, (RenderZombie) renderer, partialTicks);
	}
	
	/*
	 * Used to reverse the effect of the mutation.
	 * Called from PlayerRenderHandler.beforePlayerRender().
	 */
	public static void deapply(RenderLivingBase renderer, EntityLivingBase entity)
	{
		if (!(renderer instanceof RenderZombie))
			return;
		if (!(entity instanceof EntityZombie))
			return;
		
		if (mutatorMap.containsKey(renderer))
		{
			ZombieMutator mutator = mutatorMap.get(renderer);
			mutator.demutate((EntityZombie) entity, (RenderZombie) renderer);
			mutatorMap.remove(renderer);
		}
	}

	/*
	 * Used to refresh the mutators in case of real-time changes during development.
	 */
	public static void refresh()
	{
		for (Map.Entry<RenderZombie, ZombieMutator> mutator : mutatorMap.entrySet())
		{
			mutator.getValue().mutate(null, mutator.getKey());
			if (mutator.getValue().layerArmor != null)
				mutator.getValue().layerArmor.initArmor();
		}
	}

	public static ZombieMutator getMutatorForRenderer(Render render)
	{
		return mutatorMap.get(render);
	}
}
