package goblinbob.mobends.standard.data;

import goblinbob.mobends.core.ModStatics;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ResourceLocation;

public class ZombieData extends ZombieDataBase<EntityZombie>
{
	
	private static final ResourceLocation ANIMATOR = new ResourceLocation(ModStatics.MODID, "bends/animators/zombie.json");
	
	public ZombieData(EntityZombie entity)
	{
		super(entity);
	}
	
	@Override
	protected ResourceLocation getModelDefinition()
	{
		return new ResourceLocation(ModStatics.MODID, "bends/models/zombie.json");
	}

	@Override
	protected ResourceLocation getDefaultAnimator()
	{
		return ANIMATOR;
	}

}
