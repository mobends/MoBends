package net.gobbob.mobends.standard.mutators;

import net.gobbob.mobends.core.data.IEntityDataFactory;
import net.gobbob.mobends.standard.data.PigZombieData;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.monster.EntityPigZombie;

public class PigZombieMutator extends BipedMutator<PigZombieData, EntityPigZombie, ModelZombie>
{

	// Should the height of the texture be 64 or 32(half)?
	protected boolean halfTexture = false;
	
	public PigZombieMutator(IEntityDataFactory<EntityPigZombie> dataFactory)
	{
		super(dataFactory);
	}
	
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
	public boolean shouldModelBeSkipped(ModelBase model)
	{
		return !(model instanceof ModelZombie);
	}
	
}
