package net.gobbob.mobends.standard.client.mutators;

import java.util.function.Function;

import net.gobbob.mobends.core.EntityDatabase;
import net.gobbob.mobends.core.animation.controller.Controller;
import net.gobbob.mobends.core.client.model.IModelPart;
import net.gobbob.mobends.standard.data.ZombieData;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.monster.EntityZombie;

public class ZombieMutator extends ZombieMutatorBase<ZombieData, EntityZombie, ModelZombie>
{

	public ZombieMutator()
	{
		super(ZombieData::new);
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
	public boolean isModelEligible(ModelBase model)
	{
		return model instanceof ModelZombie;
	}
	
}
