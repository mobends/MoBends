package net.gobbob.mobends.client.mutators;

import java.util.HashMap;
import java.util.Map;

import net.gobbob.mobends.animation.controller.Controller;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.EntityDatabase;
import net.gobbob.mobends.data.ZombieData;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderZombie;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;

public class ZombieMutator<T extends EntityZombie> extends BipedMutator<T, ModelZombie>
{
	public static HashMap<RenderZombie, ZombieMutator> mutatorMap = new HashMap<>();
	
	// Should the height of the texture be 64 or 32(half)?
	protected boolean halfTexture = false;
	
	@Override
	public void fetchFields(RenderLivingBase<? extends T> renderer)
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
	public void performAnimations(T zombie, RenderLivingBase<? extends T> renderer, float partialTicks)
	{
		EntityData entityData = EntityDatabase.instance.getAndMake(ZombieData::new, zombie);
		if (!(entityData instanceof ZombieData))
			return;
		
		ZombieData data = (ZombieData) entityData;
		
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
	public static void apply(RenderLivingBase<? extends EntityLivingBase> renderer, EntityLivingBase entity, float partialTicks)
	{
		if (!(renderer instanceof RenderZombie))
			return;
		if (!(entity instanceof EntityZombie))
			return;
		
		RenderZombie rendererZombie = (RenderZombie) renderer;
		EntityZombie entityZombie = (EntityZombie) entity;
		
		ZombieMutator<EntityZombie> mutator = mutatorMap.get(renderer);
		if (!mutatorMap.containsKey(renderer))
		{
			mutator = new ZombieMutator<>();
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
	public static void deapply(RenderLivingBase<? extends EntityLivingBase> renderer, EntityLivingBase entity)
	{
		if (!(renderer instanceof RenderZombie))
			return;
		if (!(entity instanceof EntityZombie))
			return;
		
		if (mutatorMap.containsKey(renderer))
		{
			ZombieMutator<EntityZombie> mutator = mutatorMap.get(renderer);
			mutator.demutate((EntityZombie) entity, (RenderZombie) renderer);
			mutatorMap.remove(renderer);
		}
	}

	/**
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

	public static ZombieMutator<EntityZombie> getMutatorForRenderer(RenderZombie render)
	{
		return mutatorMap.get(render);
	}
}
