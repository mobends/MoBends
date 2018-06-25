package net.gobbob.mobends.animation.bit.player;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.animation.layer.AnimationLayer;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.data.PlayerData;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.data.EntityData;

public class StandAnimationBit extends AnimationBit
{
	protected String animationTarget;

	public StandAnimationBit(String animationTarget)
	{
		this.animationTarget = animationTarget;
	}

	@Override
	public void perform(EntityData entityData)
	{
		if (!(entityData instanceof PlayerData))
			return;
		PlayerData data = (PlayerData) entityData;

		data.renderOffset.slideToZero(0.1F);
		data.renderRotation.slideToZero(0.3F);
		
		data.body.preRotation.slideTo(new Vector3f(0.0f, 0.0f, 0.0f), 0.5f);
		data.body.rotation.slideTo(new Vector3f(0.0f, 0.0f, 0.0f), 0.5f);
		data.rightLeg.rotation.slideZ(2, 0.2f);
		data.leftLeg.rotation.slideZ(-2, 0.2f);
		data.rightLeg.rotation.slideX(0.0F, 0.1f);
		data.leftLeg.rotation.slideX(0.0F, 0.1f);

		data.leftLeg.rotation.slideY(-5);
		data.rightLeg.rotation.slideY(5);

		data.rightArm.preRotation.slideTo(new Vector3f(0.0f, 0.0f, 0.0f));
		data.rightArm.rotation.slideTo(new Vector3f(0.0F, 0.0F, 0.0F), 0.1f);
		data.leftArm.preRotation.slideTo(new Vector3f(0.0f, 0.0f, 0.0f));
		data.leftArm.rotation.slideTo(new Vector3f(0.0F, 0.0F, 0.0F), 0.1f);
		data.rightForeLeg.rotation.slideX(4.0F, 0.1f);
		data.leftForeLeg.rotation.slideX(4.0F, 0.1f);
		data.rightForeArm.rotation.slideX(-4.0F, 0.1f);
		data.leftForeArm.rotation.slideX(-4.0F, 0.1f);
		data.head.preRotation.slideTo(new Vector3f(0.0f, 0.0f, 0.0f));
		data.head.rotation.setY(data.getHeadYaw() - data.body.rotation.getNextY(DataUpdateHandler.ticksPerFrame));
		data.head.rotation.setX(data.getHeadPitch() - data.body.rotation.getNextX(DataUpdateHandler.ticksPerFrame));

		data.body.rotation.slideX((float) ((Math.cos(DataUpdateHandler.getTicks() / 10) - 1.0) / 2.0f) * -3);
		data.leftArm.rotation
				.slideZ(-(float) ((Math.cos(DataUpdateHandler.getTicks() / 10 + Math.PI / 2) - 1.0) / 2.0f) * -5);
		data.rightArm.rotation
				.slideZ(-(float) ((Math.cos(DataUpdateHandler.getTicks() / 10 + Math.PI / 2) - 1.0) / 2.0f) * 5);

		BendsPack.animate(entityData, this.animationTarget, "stand");
	}
}
