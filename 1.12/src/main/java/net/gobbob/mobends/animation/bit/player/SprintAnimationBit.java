package net.gobbob.mobends.animation.bit.player;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.PlayerData;
import net.gobbob.mobends.pack.BendsPack;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.math.MathHelper;

public class SprintAnimationBit extends AnimationBit
{

	@Override
	public void perform(EntityData entityData)
	{
		if (!(entityData instanceof PlayerData))
			return;
		if (!(entityData.getEntity() instanceof AbstractClientPlayer))
			return;

		PlayerData data = (PlayerData) entityData;

		data.renderOffset.slideToZero(0.1F);
		data.renderRotation.slideToZero(0.3F);
		data.renderRightItemRotation.slideToZero();
		data.renderLeftItemRotation.slideToZero();
		
		data.rightArm.preRotation.slideToZero();
		data.leftArm.preRotation.slideToZero();
		data.head.preRotation.slideToZero();

		data.rightArm.rotation.slideX(1.1f * (float) ((MathHelper.cos(data.getLimbSwing() * 0.6662F + (float) Math.PI)
				* 2.0F * data.getLimbSwingAmount() * 0.5F) / Math.PI * 180.0f));
		data.leftArm.rotation.slideX(1.1f
				* (float) ((MathHelper.cos(data.getLimbSwing() * 0.6662F) * 2.0F * data.getLimbSwingAmount() * 0.5F)
						/ Math.PI * 180.0f));

		data.rightArm.rotation.slideZ(5, 0.3f);
		data.leftArm.rotation.slideZ(-5, 0.3f);

		data.rightLeg.rotation.slideX(-5.0f
				+ 0.9f * (float) ((MathHelper.cos(data.getLimbSwing() * 0.6662F) * 1.4F * data.getLimbSwingAmount())
						/ Math.PI * 180.0f),
				1.0f);
		data.leftLeg.rotation
				.slideX(-5.0f + 0.9f * (float) ((MathHelper.cos(data.getLimbSwing() * 0.6662F + (float) Math.PI) * 1.4F
						* data.getLimbSwingAmount()) / Math.PI * 180.0f), 1.0f);

		data.rightLeg.rotation.slideY(0.0f);
		data.leftLeg.rotation.slideY(0.0f);

		data.rightLeg.rotation.slideZ(2, 0.2f);
		data.leftLeg.rotation.slideZ(-2, 0.2f);

		float var = (float) ((float) (data.getLimbSwing() * 0.6662F) / Math.PI) % 2;
		data.leftForeLeg.rotation.slideX((var > 1 ? 45 : 0), 0.3f);
		data.rightForeLeg.rotation.slideX((var > 1 ? 0 : 45), 0.3f);
		data.leftForeArm.rotation.slideX((var > 1 ? -10 : -45), 0.3f);
		data.rightForeArm.rotation.slideX((var > 1 ? -45 : -10), 0.3f);

		float var2 = (float) Math.cos(data.getLimbSwing() * 0.6662F) * -40;
		float var3 = (float) (Math.cos(data.getLimbSwing() * 0.6662F * 2.0f) * 0.5f + 0.5f) * 20;
		data.body.rotation.slideY(var2, 0.5f);
		data.body.rotation.slideX(var3);
		data.head.rotation.setY(data.getHeadYaw() - data.body.rotation.getNextY(DataUpdateHandler.ticksPerFrame));
		data.head.rotation.setX(data.getHeadPitch() - data.body.rotation.getNextX(DataUpdateHandler.ticksPerFrame));

		float var10 = data.getHeadYaw() * 0.3f;
		var10 = Math.min(var10, 20);
		var10 = Math.max(var10, -20);
		data.body.rotation.slideZ(-var10, 0.3f);

		data.renderOffset.slideY(var3 * 0.15f, 0.9f);

		BendsPack.animate(data, "player", "sprint");
	}

}
