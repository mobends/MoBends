package net.gobbob.mobends.animation.bit.biped;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.data.BipedEntityData;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.entity.EntityLivingBase;

public class FistGuardAnimationBit extends AnimationBit
{

	@Override
	public String[] getActions(EntityData entityData)
	{
		// TODO Auto-generated method stub
		return new String[] { "fist_guard" };
	}

	@Override
	public void perform(EntityData entityData)
	{
		if (!(entityData instanceof BipedEntityData))
			return;
		if (!(entityData.getEntity() instanceof EntityLivingBase))
			return;

		BipedEntityData data = (BipedEntityData) entityData;
		EntityLivingBase living = (EntityLivingBase) data.getEntity();

		/*data.renderRotation.slideY(-20.0f);
		data.renderOffset.addY(-2.0f);

		data.rightArm.rotation.slideX(-90, 0.3f);
		data.rightForeArm.rotation.slideX(-80, 0.3f);

		data.leftArm.rotation.slideX(-90, 0.3f);
		data.leftForeArm.rotation.slideX(-80, 0.3f);

		data.rightArm.rotation.slideZ(20, 0.3f);
		data.leftArm.rotation.slideZ(-20, 0.3f);

		data.body.rotation.addX(10);

		data.rightLeg.rotation.slideX(-30, 0.3f);
		data.leftLeg.rotation.slideX(-30, 0.3f);
		data.leftLeg.rotation.slideY(-25, 0.3f);
		data.rightLeg.rotation.slideZ(10);
		data.leftLeg.rotation.slideZ(-10);

		data.rightForeLeg.rotation.slideX(30, 0.3f);
		data.leftForeLeg.rotation.slideX(30, 0.3f);

		data.head.rotation.setY(data.getHeadYaw() - data.body.rotation.getY() - 20);
		data.head.rotation.setX(data.getHeadPitch() - 10);*/
	}
}
