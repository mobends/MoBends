package net.gobbob.mobends.animation.bit.player;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.PlayerData;

public class SprintJumpAnimationBit extends AnimationBit
{
	@Override
	public String[] getActions(EntityData entityData)
	{
		return new String[] { "sprint_jump" };
	}
	
	@Override
	public void perform(EntityData entityData)
	{
		if (!(entityData instanceof PlayerData))
			return;
		
		PlayerData data = (PlayerData) entityData;
		
		float bodyRot = 0.0f;
		float bodyLean = data.getMotion().y;
		if (bodyLean < -0.2f)
			bodyLean = -0.2f;
		if (bodyLean > 0.2f)
			bodyLean = 0.2f;
		bodyLean = bodyLean * -100.0f + 20;

		data.body.rotation.slideX(bodyLean, 0.3f);
		data.rightLeg.rotation.slideZ(5, 0.3f);
		data.leftLeg.rotation.slideZ(-5, 0.3f);
		data.rightArm.rotation.slideZ(10, 0.3f);
		data.leftArm.rotation.slideZ(-10, 0.3f);

		if (data.getSprintJumpLeg())
		{
			data.rightLeg.rotation.slideX(-45, 0.4f);
			data.leftLeg.rotation.slideX(45, 0.4f);
			data.rightArm.rotation.slideX(50, 0.3f);
			data.leftArm.rotation.slideX(-50, 0.3f);
			bodyRot = 20;
		}
		else
		{
			data.rightLeg.rotation.slideX(45, 0.4f);
			data.leftLeg.rotation.slideX(-45, 0.4f);
			data.rightArm.rotation.slideX(-50, 0.3f);
			data.leftArm.rotation.slideX(50, 0.3f);
			bodyRot = -20;
		}

		data.body.rotation.slideY(bodyRot, 0.3f);

		data.head.rotation.setY(data.getHeadYaw() - bodyRot);
		data.head.rotation.setX(data.getHeadPitch() - 20);

		float var = data.rightLeg.rotation.getX();
		data.leftForeLeg.rotation.slideX((var < 0 ? 45 : 2), 0.3f);
		data.rightForeLeg.rotation.slideX((var < 0 ? 2 : 45), 0.3f);
	}
}
