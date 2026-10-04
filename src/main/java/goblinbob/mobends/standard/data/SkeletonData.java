package goblinbob.mobends.standard.data;

import goblinbob.mobends.core.ModStatics;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.util.ResourceLocation;

public class SkeletonData extends BipedEntityData<EntitySkeleton>
{

	private static final ResourceLocation ANIMATOR = new ResourceLocation(ModStatics.MODID, "bends/animators/skeleton.json");

	public SkeletonData(EntitySkeleton entity)
	{
		super(entity);
	}

	@Override
	protected ResourceLocation getModelDefinition()
	{
		return new ResourceLocation(ModStatics.MODID, "bends/models/skeleton.json");
	}

	@Override
	protected ResourceLocation getDefaultAnimator()
	{
		return ANIMATOR;
	}


	@Override
	public void initModelPose()
	{
		super.initModelPose();

		this.rightArm.position.set(-5F, -10F, 0F);
		this.leftArm.position.set(5F, -10f, 0f);
		this.rightLeg.position.set(-2F, 12.0F, 0.0F);
		this.leftLeg.position.set(2F, 12.0F, 0.0F);
		this.rightForeArm.position.set(0F, 4F, 1F);
		this.leftForeArm.position.set(0F, 4F, 1F);
		this.leftForeLeg.position.set(0, 6.0F, -1.0F);
		this.rightForeLeg.position.set(0, 6.0F, -1.0F);
	}

}
