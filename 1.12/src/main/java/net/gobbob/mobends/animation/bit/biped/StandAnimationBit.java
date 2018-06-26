package net.gobbob.mobends.animation.bit.biped;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.data.BipedEntityData;
import net.gobbob.mobends.data.EntityData;

public class StandAnimationBit extends AnimationBit
{
	protected final float kneelDuration = 0.15F;

	@Override
	public String[] getActions(EntityData entityData)
	{
		return new String[] { "stand" };
	}
	
	@Override
	public void onPlay(EntityData entityData)
	{
		if (!(entityData instanceof BipedEntityData))
			return;

		BipedEntityData data = (BipedEntityData) entityData;
		
		float touchdown = Math.min(data.getTicksAfterTouchdown() * kneelDuration, 1.0F);
		if (touchdown < 0.5F)
		{
			data.body.rotation.setX(20);
			data.rightLeg.rotation.setX(-20);
			data.leftLeg.rotation.setX(-45);
			data.rightForeLeg.rotation.setX(60);
			data.leftForeLeg.rotation.setX(60);
		}
	}

	@Override
	public void perform(EntityData entityData)
	{
		if (!(entityData instanceof BipedEntityData))
			return;
		BipedEntityData data = (BipedEntityData) entityData;

		data.renderOffset.slideToZero(0.1F);
		data.renderRotation.slideToZero(0.3F);
		data.renderRightItemRotation.slideToZero();
		data.renderLeftItemRotation.slideToZero();

		data.body.preRotation.slideTo(new Vector3f(0.0f, 0.0f, 0.0f), 0.5f);
		data.body.rotation.slideTo(new Vector3f(0.0f, 0.0f, 0.0f), 0.5f);
		data.rightLeg.rotation.slideZ(2, 0.3f);
		data.leftLeg.rotation.slideZ(-2, 0.3f);
		data.rightLeg.rotation.slideX(0.0F, 0.3f);
		data.leftLeg.rotation.slideX(0.0F, 0.3f);

		data.leftLeg.rotation.slideY(-5);
		data.rightLeg.rotation.slideY(5);

		data.rightArm.preRotation.slideTo(new Vector3f(0.0f, 0.0f, 0.0f));
		data.rightArm.rotation.slideTo(new Vector3f(0.0F, 0.0F, 0.0F), 0.1f);
		data.leftArm.preRotation.slideTo(new Vector3f(0.0f, 0.0f, 0.0f));
		data.leftArm.rotation.slideTo(new Vector3f(0.0F, 0.0F, 0.0F), 0.1f);
		data.rightForeLeg.rotation.slideX(4.0F, 0.3f);
		data.leftForeLeg.rotation.slideX(4.0F, 0.3f);
		data.rightForeArm.rotation.slideX(-4.0F, 0.3f);
		data.leftForeArm.rotation.slideX(-4.0F, 0.3f);
		data.head.preRotation.slideTo(new Vector3f(0.0f, 0.0f, 0.0f));
		data.head.rotation.setY(data.getHeadYaw() - data.body.rotation.getNextY(DataUpdateHandler.ticksPerFrame));
		data.head.rotation.setX(data.getHeadPitch() - data.body.rotation.getNextX(DataUpdateHandler.ticksPerFrame));

		data.body.rotation.slideX((float) ((Math.cos(DataUpdateHandler.getTicks() / 10) - 1.0) / 2.0f) * -3);
		data.leftArm.rotation
				.slideZ(-(float) ((Math.cos(DataUpdateHandler.getTicks() / 10 + Math.PI / 2) - 1.0) / 2.0f) * -5);
		data.rightArm.rotation
				.slideZ(-(float) ((Math.cos(DataUpdateHandler.getTicks() / 10 + Math.PI / 2) - 1.0) / 2.0f) * 5);

		float touchdown = Math.min(data.getTicksAfterTouchdown() * kneelDuration, 1.0F);

		if (touchdown < 1.0F)
		{
			data.body.rotation.setX(20.0F * (1 - touchdown));
			data.renderOffset.setY((float) -Math.sin(touchdown * Math.PI) * 2.0F);
		}
	}
}
