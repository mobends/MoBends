package net.gobbob.mobends.standard.animation.bit.biped;

import net.gobbob.mobends.core.animation.bit.AnimationBit;
import net.gobbob.mobends.core.client.event.DataUpdateHandler;
import net.gobbob.mobends.standard.data.BipedEntityData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

public class FallingAnimationBit extends AnimationBit<BipedEntityData<?>>
{
	private static final String[] ACTIONS = new String[] { "falling" };
	public static final float TICKS_BEFORE_FALLING = 10;
	public static final float FALLING_TRANSITION_TICKS = 80;

	@Override
	public String[] getActions(BipedEntityData<?> entityData)
	{
		return ACTIONS;
	}

	@Override
	public void perform(BipedEntityData<?> data)
	{
		EntityLivingBase living = data.getEntity();

		data.centerRotation.setSmoothness(.3F).orientZero();
		
		data.head.rotation.orientX(MathHelper.wrapDegrees(data.headPitch.get()))
				.rotateY(MathHelper.wrapDegrees(data.headYaw.get()));
		data.body.rotation.orientY(0).setSmoothness(0.5F);

		float ticks = DataUpdateHandler.getTicks() * 0.5F;
		float rightArmDelay = 1F;

		float armSpan = 20.0F;
		float legSpan = 10.0F;

		float transition = (data.getTicksFalling() - TICKS_BEFORE_FALLING) / FALLING_TRANSITION_TICKS;
		transition = MathHelper.clamp(transition, 0.0F, 1.0F);
		float s = 0.0F + transition * 0.9F;

		data.leftArm.rotation.setSmoothness(s).orientZ(-90.0F + MathHelper.sin(ticks) * armSpan)
				.rotateY(MathHelper.cos(ticks) * armSpan);
		data.rightArm.rotation.setSmoothness(s).orientZ(90.0F + MathHelper.sin(ticks + rightArmDelay) * armSpan)
				.rotateY(MathHelper.cos(ticks + rightArmDelay) * armSpan);
		data.leftForeArm.rotation.setSmoothness(s).orientX(-15.0F);
		data.rightForeArm.rotation.setSmoothness(s).orientX(-15.0F);
		data.leftLeg.rotation.setSmoothness(s).orientX(MathHelper.sin(ticks) * legSpan)
				.rotateZ(-20.0F + MathHelper.cos(ticks) * legSpan);
		data.rightLeg.rotation.setSmoothness(s).orientX(MathHelper.sin(ticks + rightArmDelay) * legSpan)
				.rotateZ(20.0F + MathHelper.cos(ticks + rightArmDelay) * legSpan);
		data.leftForeLeg.rotation.setSmoothness(s).orientX(20.0F);
		data.rightForeLeg.rotation.setSmoothness(s).orientX(20.0F);
		data.renderRotation.setSmoothness(s).orientX(20.0F);
		data.head.rotation.setSmoothness(s).rotateX(-20.0F);
	}
}
