package net.gobbob.mobends.animation.bit.player;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.data.PlayerData;
import net.gobbob.mobends.util.GUtil;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.math.MathHelper;

public class FlyingAnimationBit extends AnimationBit<PlayerData>
{
	private static final String[] ACTIONS = new String[] { "flying" };
	private static final float PI = (float) Math.PI;
	private static final float PI_2 = PI * 2;
	private static final double STILL_MOTION_THRESHOLD = 0.1F;

	private float transformTransition = 0F;
	private float transitionSpeed = 0.1F;

	@Override
	public String[] getActions(PlayerData entityData)
	{
		return ACTIONS;
	}

	@Override
	public void onPlay(PlayerData data)
	{
		transformTransition = 0F;
		transitionSpeed = .1F;
	}

	@Override
	public void perform(PlayerData data)
	{
		AbstractClientPlayer player = data.getEntity();
		
		final double motionX = data.getInterpolatedMotionX();
		final double motionY = data.getInterpolatedMotionY();
		final double motionZ = data.getInterpolatedMotionZ();
		final double magnitude = data.getInterpolatedMotionMagnitude();

		float ticks = DataUpdateHandler.getTicks();
		float armSway = (MathHelper.cos(ticks * .1625F)+1F)/2.0f;
		float armSway2 = (-MathHelper.sin(ticks * .1625F)+1F)/2.0f;
		float legFlap = MathHelper.cos(ticks * .06F);
		float foreArmSway = ((ticks * .1625F) % PI_2)/PI_2;
		float foreArmStretch = armSway * 2F;
		foreArmStretch -= 1F;
		foreArmStretch = Math.max(foreArmStretch, 0);
		
		float t = (float) GUtil.easeInOut(this.transformTransition, 3F);
		
		float forwardMomentum = (float) data.getForwardMomentum();
		float sideMomentum = (float) data.getSidewaysMomentum();
		
		float headPitch = MathHelper.wrapDegrees(data.getHeadPitch());
		float headYaw = MathHelper.wrapDegrees(data.getHeadYaw());
		
		if (magnitude < STILL_MOTION_THRESHOLD || data.isDrawingBow() || data.getTicksAfterAttack() < 10)
		{
			if (this.transformTransition > 0F)
			{
				this.transformTransition -= DataUpdateHandler.ticksPerFrame * this.transitionSpeed;
				this.transformTransition = Math.max(0F, this.transformTransition);
			}
			
			armSway = (MathHelper.cos(ticks * .0825F) + 1) / 2;
			armSway2 = (-MathHelper.sin(ticks * .0825F) + 1) / 2;
			legFlap = MathHelper.cos(ticks * .125F);
			
			data.leftArm.rotation.setSmoothness(.3F).orientX(armSway2*30-15).rotateZ(-armSway*30);
			data.rightArm.rotation.setSmoothness(.3F).orientX(armSway2*30-15).rotateZ(armSway*30);
			data.leftForeArm.rotation.setSmoothness(.3F).orientX(armSway2*-40);
			data.rightForeArm.rotation.setSmoothness(.3F).orientX(armSway2*-40);
			data.leftLeg.rotation.setSmoothness(.3F).orientZ(-10 + legFlap*5);
			data.rightLeg.rotation.setSmoothness(.3F).orientZ(10 -legFlap*5);
			data.leftForeLeg.rotation.setSmoothness(.4F).orientX(5);
			data.rightForeLeg.rotation.setSmoothness(.4F).orientX(5);
			data.body.rotation.orientX(armSway * 10.0F);
			
			data.centerRotation.orientZero();
			
			data.head.rotation.setSmoothness(1.0F).orientX(headPitch)
					.rotateY(MathHelper.wrapDegrees(data.getHeadYaw())).rotateX(-80F * t);
		}
		else if (!player.isSprinting())
		{
			data.centerRotation.orientZero();
			data.centerRotation.rotateX(forwardMomentum * 50.0F);
			data.leftArm.rotation.orientX(forwardMomentum * 90.0F).localRotateZ(sideMomentum * -80.0F - 20.0F);
			data.rightArm.rotation.orientX(forwardMomentum * 90.0F).localRotateZ(sideMomentum * -80.0F + 20.0F);
			data.leftForeArm.rotation.orientZero();
			data.rightForeArm.rotation.orientZero();
			
			data.head.rotation.setSmoothness(1.0F).orientX(headPitch).rotateX(-forwardMomentum * 50.0F);
			
			data.centerRotation.localRotateY(-headYaw);
		}
		else
		{
			data.centerRotation.setSmoothness(0.7F).orientX(headPitch + 90.0F);
			
			data.head.rotation.setSmoothness(1.0F).orientY(MathHelper.wrapDegrees(data.getHeadYaw()))
					.rotateX(-90F);
		}
		
		data.renderRotation.setSmoothness(.7F).orientX(t * 80F);
		data.renderOffset.slideToZero(.7F);
	}
}
