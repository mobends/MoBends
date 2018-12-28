package net.gobbob.mobends.standard.client.mutators;

import java.util.HashMap;
import java.util.Map;

import net.gobbob.mobends.core.EntityData;
import net.gobbob.mobends.core.EntityDatabase;
import net.gobbob.mobends.core.animation.controller.Controller;
import net.gobbob.mobends.standard.data.PigZombieData;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPigZombie;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityPigZombie;

public class PigZombieMutator extends BipedMutator<EntityPigZombie, ModelZombie>
{
	
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
		PigZombieData data = EntityDatabase.instance.getAndMake(PigZombieData::new, zombie);
		
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
	
}
