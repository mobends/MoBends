package net.gobbob.mobends.animation.bit.biped;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.data.BipedEntityData;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

public class JumpAnimationBit extends AnimationBit
{
	@Override
	public String[] getActions(EntityData entityData)
	{
		return new String[] { "jump" };
	}
	
	@Override
	public void onPlay(EntityData entityData)
	{
		if (!(entityData instanceof BipedEntityData))
			return;

		BipedEntityData data = (BipedEntityData) entityData;

		data.body.rotation.setX(20);
		data.rightLeg.rotation.setX(0);
		data.leftLeg.rotation.setX(0);
		data.leftLeg.rotation.setY(0);
		data.leftLeg.rotation.setZ(0);
		data.rightForeLeg.rotation.setX(0);
		data.leftForeLeg.rotation.setX(0);
		data.rightArm.rotation.setZ(2);
		data.leftArm.rotation.setZ(-2);
		data.rightForeArm.rotation.setX(-20);
		data.leftForeArm.rotation.setX(-20);
	}

	@Override
	public void perform(EntityData entityData)
	{
		if (!(entityData instanceof BipedEntityData))
			return;
		if (!(entityData.getEntity() instanceof EntityLivingBase))
			return;

		if (entityData.getPreviousMotion().y < 0 && entityData.getMotion().y > 0)
		{
			/*
			 * Restarting the animation if the player is going back up
			 * again after falling down.
			 */
			this.onPlay(entityData);
		}

		BipedEntityData data = (BipedEntityData) entityData;
		EntityLivingBase player = (EntityLivingBase) data.getEntity();

		data.renderOffset.slideToZero(0.3F);
		data.renderRotation.slideToZero();
		data.renderRightItemRotation.slideToZero();
		data.renderLeftItemRotation.slideToZero();
		
		data.body.preRotation.slideToZero(0.5F);
		data.body.rotation.slideX(Math.max(1.0F - data.getTicksAfterLiftoff() * 0.1F, 0.0F), 0.2f);
		data.body.rotation.slideY(0, 0.1f);
		data.body.rotation.slideZ(0.0f, 0.3f);
		data.rightArm.preRotation.slideToZero();
		data.rightArm.rotation.slideToZero(0.5f);
		data.rightArm.rotation.slideZ(45, 0.05f);
		data.leftArm.preRotation.slideToZero();
		data.leftArm.rotation.slideToZero(0.5f);
		data.leftArm.rotation.slideZ(-45, 0.05f);
		data.rightArm.rotation.slideX(0.0f, 0.5f);
		data.leftArm.rotation.slideX(0.0f, 0.5f);
		data.rightLeg.rotation.slideZ(10, 0.1f);
		data.leftLeg.rotation.slideZ(-10, 0.1f);

		data.rightLeg.rotation.slideX(-45, 0.3f);
		data.leftLeg.rotation.slideX(-17, 0.3f);
		data.rightForeLeg.rotation.slideX(70, 0.3f);
		data.leftForeLeg.rotation.slideX(17, 0.5f);

		data.rightForeArm.rotation.slideX(0, 0.3f);
		data.leftForeArm.rotation.slideX(0, 0.3f);

		data.head.preRotation.slideToZero();
		data.head.rotation.setY(data.getHeadYaw());
		data.head.rotation.setX(data.getHeadPitch() - data.body.rotation.getX());

		if (!data.isStillHorizontally())
		{
			data.rightLeg.rotation.slideX(-5.0f + 0.5f
					* (float) ((MathHelper.cos(data.getLimbSwing() * 0.6662F) * 1.4F * data.getLimbSwingAmount())
							/ Math.PI * 180.0f),
					1.0f);
			data.leftLeg.rotation
					.slideX(-5.0f + 0.5f * (float) ((MathHelper.cos(data.getLimbSwing() * 0.6662F + (float) Math.PI)
							* 1.4F * data.getLimbSwingAmount()) / Math.PI * 180.0f), 1.0f);

			float var = (float) ((float) (data.getLimbSwing() * 0.6662F) / Math.PI) % 2;
			data.leftForeLeg.rotation.slideX((var > 1 ? 45 : 0), 0.3f);
			data.rightForeLeg.rotation.slideX((var > 1 ? 0 : 45), 0.3f);
			data.leftForeArm.rotation.slideX(
					((float) (Math.cos(data.getLimbSwing() * 0.6662F + Math.PI / 2) + 1.0f) / 2.0f) * -20, 1.0f);
			data.rightForeArm.rotation
					.slideX(((float) (Math.cos(data.getLimbSwing() * 0.6662F) + 1.0f) / 2.0f) * -20, 0.3f);
		}
	}
}
