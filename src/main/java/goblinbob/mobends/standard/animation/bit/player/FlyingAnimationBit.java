package goblinbob.mobends.standard.animation.bit.player;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.util.GUtil;
import goblinbob.mobends.standard.data.PlayerData;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.math.MathHelper;

public class FlyingAnimationBit extends AnimationBit<PlayerData>
{
	private static final float PI = (float) Math.PI;
	private static final float PI_2 = PI * 2;
	private static final double STILL_MOTION_THRESHOLD = 0.1;

	private float transformTransition = 0F;

	@Override
	public void perform(PlayerData data)
	{
		final AbstractClientPlayer player = data.getEntity();
		
		final double magnitude = data.getInterpolatedMotionMagnitude();

		float ticks = DataUpdateHandler.getTicks();

		float forwardMomentum = MathHelper.clamp((float) data.getForwardMomentum(), -1F, 1F);
		float sideMomentum = MathHelper.clamp((float) data.getSidewaysMomentum(), -1F, 1F);
		double xzMomentum = data.getInterpolatedXZMotionMagnitude();
		
		float headPitch = data.headPitch.get();
		float headYaw = data.headYaw.get();
		float headYawAbs = MathHelper.abs(headYaw);
		float yMomentumAngle = (float) MathHelper.atan2(xzMomentum,  data.getMotionY()) * 180.0F / GUtil.PI;
		
		if (player.isSprinting() && !data.isDrawingBow() && data.getTicksAfterAttack() >= 10)
		{
			float speedFactor = MathHelper.clamp((float) magnitude, 0.0F, 0.2F) / 0.2F;
			
			// Full Speed
			data.root.rotation.setSmoothness(1.0F).orientX(yMomentumAngle * speedFactor).rotateZ(-headYaw);
			
			float bodyRotationX = MathHelper.clamp(headPitch * 0.8F, -60.0F, 0.0F);
			data.head.rotation.setSmoothness(1.0F).orientY(headYaw).rotateX(headPitch - bodyRotationX - yMomentumAngle * speedFactor);
			data.body.rotation.setSmoothness(0.7F).orientX(bodyRotationX);
			data.leftArm.rotation.setSmoothness(0.7F).orientX(-bodyRotationX).rotateZ(-60F + 55F * speedFactor - headYawAbs * 0.5F);
			data.rightArm.rotation.setSmoothness(0.7F).orientX(-bodyRotationX).rotateZ(60F - 55F * speedFactor + headYawAbs * 0.5F);
			data.leftForearm.rotation.setSmoothness(.7F).orientZero();
			data.rightForearm.rotation.setSmoothness(.7F).orientZero();
			data.leftLeg.rotation.setSmoothness(0.7F).orientZ(-5.0F);
			data.rightLeg.rotation.setSmoothness(0.7F).orientZ(5.0F);
			data.leftShin.rotation.setSmoothness(0.7F).orientX(0.0F);
			data.rightShin.rotation.setSmoothness(0.7F).orientX(0.0F);
		}
		else if (magnitude < STILL_MOTION_THRESHOLD)
		{
			// Hovering Still
			
			float armSway = (MathHelper.cos(ticks * .0825F) + 1) / 2;
			float armSway2 = (-MathHelper.sin(ticks * .0825F) + 1) / 2;
			float legFlap = MathHelper.cos(ticks * .125F);
			float legFlap2 = MathHelper.sin(ticks * .125F);
			float foreArmSway = ((ticks * .1625F) % PI_2)/PI_2;
			float foreArmStretch = armSway * 2F;
			foreArmStretch -= 1F;
			foreArmStretch = Math.max(foreArmStretch, 0);
			
			data.leftArm.rotation.setSmoothness(.3F).orientX(armSway2*30-15).rotateZ(-armSway*30);
			data.rightArm.rotation.setSmoothness(.3F).orientX(armSway2*30-15).rotateZ(armSway*30);
			data.leftForearm.rotation.setSmoothness(.3F).orientX(armSway2*-40);
			data.rightForearm.rotation.setSmoothness(.3F).orientX(armSway2*-40);
			data.leftLeg.rotation.setSmoothness(.3F).orientZ(-5 + legFlap*3).rotateX(-25F + legFlap2*5);
			data.rightLeg.rotation.setSmoothness(.3F).orientZ(5 -legFlap*3).rotateX(-6F + legFlap2*5);
			data.leftShin.rotation.setSmoothness(.4F).orientX(20 - legFlap2*15);
			data.rightShin.rotation.setSmoothness(.4F).orientX(5);
			data.body.rotation.orientX(armSway * 10.0F);
			
			data.head.rotation.setSmoothness(1.0F).orientX(headPitch)
					.rotateY(headYaw);
		}
		else
		{
			// Moving

			data.root.rotation.orientZero();
			data.root.rotation.rotateX(forwardMomentum * 50.0F);
			data.body.rotation.orientZero();
			data.leftArm.rotation.orientX(forwardMomentum * 90.0F).localRotateZ(sideMomentum * -80.0F - 20.0F);
			data.rightArm.rotation.orientX(forwardMomentum * 90.0F).localRotateZ(sideMomentum * -80.0F + 20.0F);
			data.leftForearm.rotation.orientZero();
			data.rightForearm.rotation.orientZero();
			
			data.leftLeg.rotation.orientX(-45F).localRotateZ(sideMomentum * -40.0F - 5.0F);
			data.rightLeg.rotation.orientX(-6F).localRotateZ(sideMomentum * -40.0F + 5.0F);
			data.leftShin.rotation.orientX(30F);
			data.rightShin.rotation.orientX(10F);
			
			data.head.rotation.setSmoothness(1.0F).orientX(headPitch).rotateX(-forwardMomentum * 50.0F);
			
			if (!data.isDrawingBow())
			{
				data.root.rotation.localRotateY(headYaw);
			}
		}
	}

}
