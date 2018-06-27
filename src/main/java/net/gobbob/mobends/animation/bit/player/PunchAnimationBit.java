package net.gobbob.mobends.animation.bit.player;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.PlayerData;
import net.gobbob.mobends.util.EnumAxis;
import net.minecraft.entity.EntityLivingBase;

public class PunchAnimationBit extends AnimationBit
{

	@Override
	public String[] getActions(EntityData entityData)
	{
		// TODO Auto-generated method stub
		return new String[] { "attack", "punch" };
	}

	@Override
	public void perform(EntityData entityData)
	{
		if (!(entityData instanceof PlayerData))
			return;
		if (!(entityData.getEntity() instanceof EntityLivingBase))
			return;

		PlayerData data = (PlayerData) entityData;
		EntityLivingBase living = (EntityLivingBase) data.getEntity();

		data.rightArm.rotation.slideX(-90, 0.3f);
		data.rightForeArm.rotation.slideX(-80, 0.3f);

		data.leftArm.rotation.slideX(-90, 0.3f);
		data.leftForeArm.rotation.slideX(-80, 0.3f);

		data.rightArm.rotation.slideZ(20, 0.3f);
		data.leftArm.rotation.slideZ(-20, 0.3f);
		
		float renderRotationY = 0F;
		
		if (data.isStillHorizontally())
		{
			renderRotationY = -20F;
			data.renderOffset.slideY(-2.0f);

			data.rightLeg.rotation.slideX(-30, 0.3f);
			data.leftLeg.rotation.slideX(-30, 0.3f);
			data.leftLeg.rotation.slideY(-25, 0.3f);
			data.rightLeg.rotation.slideZ(10);
			data.leftLeg.rotation.slideZ(-10);

			data.rightForeLeg.rotation.slideX(30, 0.3f);
			data.leftForeLeg.rotation.slideX(30, 0.3f);
		}
		
		if (data.getFistPunchArm())
		{
			data.rightArm.preRotation.slideZ(90, 0.9F);

			data.rightArm.rotation.slideX(-80 - data.renderRotation.getEnd(EnumAxis.Y), 0.9F);
			data.rightArm.preRotation.slideY(-(-20.0f + data.getHeadPitch()), 0.9f);
			data.rightForeArm.rotation.slideX(0, 0.9f);
			
			data.body.rotation.slideY(-20.0f + renderRotationY, 0.6f);

			//data.head.rotation.slideY(data.getHeadYaw() + 20.0F + renderRotationY);
			//data.head.rotation.setX(data.getHeadPitch() - 10);
		}
		else
		{
			data.leftArm.preRotation.slideZ(-100, 0.9f);
			data.leftArm.preRotation.slideY(-15, 0.9f);

			data.leftArm.rotation.slideX(-70, 0.9f);
			data.leftArm.preRotation.slideY(-20.0f + data.getHeadPitch(), 0.9f);
			data.leftForeArm.rotation.slideX(0, 0.9f);

			data.body.rotation.slideY(20.0f + renderRotationY, 0.6f);

			//data.head.rotation.slideY(data.getHeadYaw() - 20.0F + renderRotationY);
			//data.head.rotation.setX(data.getHeadPitch() - 10);
		}
		
		data.head.rotation.setY(data.getHeadYaw() - data.body.rotation.getY() + renderRotationY);
		data.body.rotation.slideX(10, 0.3f);
		data.renderRotation.slideY(renderRotationY);
	}

}
