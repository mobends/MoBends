package net.gobbob.mobends.animation.bit.biped;

import net.gobbob.mobends.core.animation.bit.AnimationBit;
import net.gobbob.mobends.data.BipedEntityData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

public class JumpAnimationBit<T extends BipedEntityData<?, ?>> extends AnimationBit<T>
{
	private static final String[] ACTIONS = new String[] { "jump" };
	
	@Override
	public String[] getActions(T entityData)
	{
		return ACTIONS;
	}

	@Override
	public void onPlay(T data)
	{
		data.centerRotation.identity();
		data.body.rotation.orientInstantX(20F);
		data.rightLeg.rotation.orientInstantX(0F);
		data.leftLeg.rotation.orientInstantX(0F);
		data.rightForeLeg.rotation.orientInstantX(0F);
		data.leftForeLeg.rotation.orientInstantX(0F);
		data.rightArm.rotation.orientInstantZ(2F);
		data.leftArm.rotation.orientInstantZ(-2F);
		data.rightForeArm.rotation.orientInstantX(-20F);
		data.leftForeArm.rotation.orientInstantX(-20F);
	}

	@Override
	public void perform(T data)
	{
		if (data.getPrevMotionY() < 0 && data.getMotionY() > 0)
		{
			/*
			 * Restarting the animation if the player is going back up again after falling
			 * down.
			 */
			this.onPlay(data);
		}

		EntityLivingBase biped = data.getEntity();

		data.renderOffset.slideToZero(0.3F);
		data.renderRotation.setSmoothness(.3F).orientZero();
		data.renderRightItemRotation.setSmoothness(.3F).orientZero();
		data.renderLeftItemRotation.setSmoothness(.3F).orientZero();

		float bodyRotationX = Math.max(1.0F - data.getTicksInAir() * 0.1F, 0.0F);
		data.body.rotation.setSmoothness(0.2F).orientX(bodyRotationX);
		data.rightArm.rotation.setSmoothness(0.05F).orientZ(45F);
		data.leftArm.rotation.setSmoothness(0.05F).orientZ(-45F);
		data.rightForeArm.rotation.setSmoothness(0.3F).orientX(0);
		data.leftForeArm.rotation.setSmoothness(0.3F).orientX(0);

		data.head.rotation.orientX(MathHelper.wrapDegrees(data.getHeadPitch()) - bodyRotationX)
						  .rotateY(MathHelper.wrapDegrees(data.getHeadYaw()));

		if (!data.isStillHorizontally())
		{
			final float PI = (float) Math.PI;
			float limbSwing = data.getLimbSwing() * 0.6662F;
			float limbSwingAmount = 0.7F * data.getLimbSwingAmount() / PI * 180F;
			data.rightLeg.rotation.setSmoothness(1.0F).orientX(-5F + MathHelper.cos(limbSwing) * limbSwingAmount);
			data.leftLeg.rotation.setSmoothness(1.0F).orientX(-5F + MathHelper.cos(limbSwing + PI) * limbSwingAmount);

			float var = (limbSwing / PI) % 2;
			data.leftForeLeg.rotation.setSmoothness(0.3F).orientX((var > 1 ? 45 : 0));
			data.rightForeLeg.rotation.setSmoothness(0.3F).orientX((var > 1 ? 0 : 45));
			data.leftForeArm.rotation.setSmoothness(0.3F).orientX((MathHelper.cos(limbSwing + PI/2) / 2F + 0.5F) * -20F);
			data.rightForeArm.rotation.setSmoothness(0.3F).orientX((MathHelper.cos(limbSwing) / 2F + 0.5F) * -20F);
		}
		else
		{
			data.rightLeg.rotation.setSmoothness(0.1F).orientZ(10);
			data.rightLeg.rotation.setSmoothness(0.3F).rotateX(-45);
			data.leftLeg.rotation.setSmoothness(0.1F).orientZ(-10);
			data.leftLeg.rotation.setSmoothness(0.3F).rotateX(-17);
			data.rightForeLeg.rotation.setSmoothness(0.3F).orientX(70);
			data.leftForeLeg.rotation.setSmoothness(0.3F).orientX(17);
		}
	}
}
