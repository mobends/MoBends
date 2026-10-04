package goblinbob.mobends.standard.data;

import goblinbob.mobends.core.ModStatics;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.util.ResourceLocation;

public class ZombieVillagerData extends ZombieDataBase<EntityZombieVillager>
{

	private static final ResourceLocation ANIMATOR = new ResourceLocation(ModStatics.MODID, "bends/animators/zombie_villager.json");
	
	public ZombieVillagerData(EntityZombieVillager entity)
	{
		super(entity);
	}
	
	@Override
	protected ResourceLocation getModelDefinition()
	{
		return new ResourceLocation(ModStatics.MODID, "bends/models/zombie_villager.json");
	}

	@Override
	protected ResourceLocation getDefaultAnimator()
	{
		return ANIMATOR;
	}

}
