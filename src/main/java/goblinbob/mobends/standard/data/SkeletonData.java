package goblinbob.mobends.standard.data;

import goblinbob.mobends.core.kumo.KumoAnimatorController;
import goblinbob.mobends.standard.main.ModStatics;
import net.minecraft.entity.monster.EntitySkeleton;

public class SkeletonData extends BipedEntityData<EntitySkeleton>
{

	/** The procedural SkeletonController is kept as the parity reference; the entity animates from its animator asset. */
	private final KumoAnimatorController<SkeletonData> controller = new KumoAnimatorController<>(ModStatics.MODID, "bends/animators/skeleton.json");

	public SkeletonData(EntitySkeleton entity)
	{
		super(entity);
	}

	@Override
	public KumoAnimatorController<SkeletonData> getController()
	{
		return controller;
	}

	@Override
	public void onTicksRestart()
	{
		// No behaviour
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
