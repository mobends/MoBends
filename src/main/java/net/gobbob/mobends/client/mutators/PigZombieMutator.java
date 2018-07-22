package net.gobbob.mobends.client.mutators;

import java.util.HashMap;
import java.util.Map;

import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.animation.controller.Controller;
import net.gobbob.mobends.client.model.IModelPart;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.EntityDatabase;
import net.gobbob.mobends.data.PigZombieData;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPigZombie;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityPigZombie;

public class PigZombieMutator extends BipedMutator<EntityPigZombie, ModelZombie>
{
	public static HashMap<RenderPigZombie, PigZombieMutator> mutatorMap = new HashMap<RenderPigZombie, PigZombieMutator>();
	
	// Should the height of the texture be 64 or 32(half)?
	protected boolean halfTexture = false;
	
	@Override
	public void fetchFields(RenderLivingBase<? extends EntityPigZombie> renderer)
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

	@Override
	public void performAnimations(EntityPigZombie zombie, RenderLivingBase<? extends EntityPigZombie> renderer, float partialTicks)
	{
		EntityData entityData = EntityDatabase.instance.getAndMake(PigZombieData.class, zombie);
		if (!(entityData instanceof PigZombieData))
			return;
		
		PigZombieData data = (PigZombieData) entityData;
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

	/**
	 * Used to apply the effect of the mutation, or just to update the model
	 * if it was already mutated.
	 * Called from AnimatedEntity.
	 */
	public static void apply(RenderLivingBase renderer, EntityLivingBase entity, float partialTicks)
	{
		if (!(renderer instanceof RenderPigZombie))
			return;
		if (!(entity instanceof EntityPigZombie))
			return;
		
		RenderPigZombie rendererZombie = (RenderPigZombie) renderer;
		EntityPigZombie entityZombie = (EntityPigZombie) entity;
		
		PigZombieMutator mutator = mutatorMap.get(renderer);
		if (!mutatorMap.containsKey(renderer))
		{
			mutator = new PigZombieMutator();
			mutator.mutate(entityZombie, rendererZombie);
			mutatorMap.put(rendererZombie, mutator);
		}

		mutator.updateModel(entityZombie, rendererZombie, partialTicks);
		mutator.performAnimations(entityZombie, rendererZombie, partialTicks);
	}
	
	/**
	 * Used to reverse the effect of the mutation.
	 * Called from AnimatedEntity.
	 */
	public static void deapply(RenderLivingBase renderer, EntityLivingBase entity)
	{
		if (!(renderer instanceof RenderPigZombie))
			return;
		if (!(entity instanceof EntityPigZombie))
			return;
		
		if (mutatorMap.containsKey(renderer))
		{
			PigZombieMutator mutator = mutatorMap.get(renderer);
			mutator.demutate((EntityPigZombie) entity, (RenderPigZombie) renderer);
			mutatorMap.remove(renderer);
		}
	}

	/**
	 * Used to refresh the mutators in case of real-time changes during development.
	 */
	public static void refresh()
	{
		for (Map.Entry<RenderPigZombie, PigZombieMutator> mutator : mutatorMap.entrySet())
		{
			mutator.getValue().mutate(null, mutator.getKey());
			if (mutator.getValue().layerArmor != null)
				mutator.getValue().layerArmor.initArmor();
		}
	}

	public static PigZombieMutator getMutatorForRenderer(Render render)
	{
		return mutatorMap.get(render);
	}
}
