package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.client.model.ModelPart;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.data.ZombieVillagerData;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelZombieVillager;
import net.minecraft.entity.monster.EntityZombieVillager;

public class ZombieVillagerMutator extends ZombieMutatorBase<ZombieVillagerData, EntityZombieVillager, ModelZombieVillager>
{

	public ZombieVillagerMutator(IEntityDataFactory<EntityZombieVillager> dataFactory)
	{
		super(dataFactory);
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
		boolean success = super.createParts(original, scaleFactor);
		
		original.bipedHead = this.head = new ModelPart(original, 0, 0)
				.setParent(body)
				.setPosition(0.0F, -12.0F, 0.0F);
		
		this.head.addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8, scaleFactor);
		this.head.setTextureOffset(24, 0).addBox(-1.0F, -3.0F, -6.0F, 2, 4, 2, scaleFactor);
		
		return success;
	}

	@Override
	public boolean shouldModelBeSkipped(ModelBase model)
	{
		return !(model instanceof ModelZombieVillager);
	}
	
}
