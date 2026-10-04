package goblinbob.mobends.standard.data;

import goblinbob.mobends.core.ModStatics;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.util.ResourceLocation;

public class PigZombieData extends BipedEntityData<EntityPigZombie>
{
	
	private static final ResourceLocation ANIMATOR = new ResourceLocation(ModStatics.MODID, "bends/animators/pig_zombie.json");
	
	public PigZombieData(EntityPigZombie entity)
	{
		super(entity);
	}

	@Override
	protected ResourceLocation getModelDefinition()
	{
		return new ResourceLocation(ModStatics.MODID, "bends/models/pig_zombie.json");
	}

	@Override
	protected ResourceLocation getDefaultAnimator()
	{
		return ANIMATOR;
	}

}
