package net.gobbob.mobends.animation.bit.zombie;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.ZombieData;
import net.minecraft.entity.monster.EntityZombie;

public class ZombieLeanAnimationBit extends AnimationBit
{

	@Override
	public String[] getActions(EntityData entityData)
	{
		return new String[] {};
	}
	
	@Override
	public void perform(EntityData entityData)
	{
		if (!(entityData instanceof ZombieData))
			return;
		if (!(entityData.getEntity() instanceof EntityZombie))
			return;
		
		ZombieData data = (ZombieData) entityData;
		
		/*data.renderOffset.slideY(-3.0F);
		
		data.body.rotation.addX(30F);
		
		data.rightArm.rotation.addX(-30F);
		data.leftArm.rotation.addX(-30F);
		
		data.rightLeg.rotation.slideZ(10F, 0.3F);
		data.leftLeg.rotation.slideZ(-10F, 0.3F);
		
		data.rightLeg.rotation.addX(-20);
		data.leftLeg.rotation.addX(-20);
		
		data.rightForeLeg.rotation.addX(25);
		data.leftForeLeg.rotation.addX(25);
		
		if(!data.isStillHorizontally() && data.getCurrentWalkingState() == 1)
		{
			data.rightArm.rotation.slideX(-90 - 30.0f);
			data.leftArm.rotation.slideX(-90 - 30.0f);
		}*/
	}
}
