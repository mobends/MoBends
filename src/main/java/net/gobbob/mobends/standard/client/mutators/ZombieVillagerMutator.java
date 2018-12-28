package net.gobbob.mobends.standard.client.mutators;

import java.util.function.Function;

import net.gobbob.mobends.core.client.model.ModelPartChild;
import net.gobbob.mobends.standard.data.ZombieVillagerData;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.model.ModelZombieVillager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.monster.EntityZombieVillager;

public class ZombieVillagerMutator extends BipedMutator<ZombieVillagerData, EntityZombieVillager, ModelZombieVillager>
{
	
	// Should the height of the texture be 64 or 32(half)?
	protected boolean halfTexture = false;
	
	public ZombieVillagerMutator()
	{
		super(ZombieVillagerData::new);
	}
	
	@Override
	public void fetchFields(RenderLivingBase<? extends EntityZombieVillager> renderer)
	{
		super.fetchFields(renderer);

		if (renderer.getMainModel() instanceof ModelZombie)
		{
			ModelZombieVillager model = (ModelZombieVillager) renderer.getMainModel();
			
			this.halfTexture = model.textureHeight == 32;
		}
	}
	
	@Override
	public void storeVanillaModel(ModelZombieVillager model)
	{
		ModelZombieVillager vanillaModel = new ModelZombieVillager(0.0F, 0.0F, this.halfTexture);
		this.vanillaModel = vanillaModel;
		
		// Calling the super method here, since it
		// requires the vanillaModel property to be
		// set.
		super.storeVanillaModel(model);
	}
	
	@Override
	public boolean createParts(ModelZombieVillager original, float scaleFactor)
	{
		original.bipedHead = this.head = (ModelPartChild) new ModelPartChild(original, 0, 0)
				.setParent(body)
				.setHideLikeParent(false)
				.setPosition(0.0F, -12.0F, 0.0F)
				.setBox(-4.0F, -10.0F, -4.0F, 8, 10, 8, scaleFactor);

		this.head.setTextureOffset(24, 0).addBox(-1.0F, -3.0F, -6.0F, 2, 4, 2, scaleFactor);
		
		return true;
	}

	@Override
	public boolean isModelEligible(ModelBase model)
	{
		return model instanceof ModelZombieVillager;
	}
	
}
