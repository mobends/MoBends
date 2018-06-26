package net.gobbob.mobends.animation.bit.biped;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.data.BipedEntityData;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.pack.BendsPack;
import net.minecraft.util.math.MathHelper;

public class SneakAnimationBit extends AnimationBit
{
	@Override
	public String[] getActions(EntityData entityData)
	{
		return new String[] { "sneak" };
	}

	@Override
	public void perform(EntityData entityData)
	{
		if (!(entityData instanceof BipedEntityData))
			return;

		BipedEntityData data = (BipedEntityData) entityData;

		float var = (float) ((float) (data.getLimbSwing() * 0.6662F) / Math.PI) % 2;
		data.rightLeg.rotation.slideX(-5.0f
				+ 1.1f * (float) ((MathHelper.cos(data.getLimbSwing() * 0.6662F) * 1.4F * data.getLimbSwingAmount())
						/ Math.PI * 180.0f),
				1.0f);
		data.leftLeg.rotation
				.slideX(-5.0f + 1.1f * (float) ((MathHelper.cos(data.getLimbSwing() * 0.6662F + (float) Math.PI) * 1.4F
						* data.getLimbSwingAmount()) / Math.PI * 180.0f), 1.0f);
		data.rightLeg.rotation.slideZ(10);
		data.leftLeg.rotation.slideZ(-10);

		data.rightArm.rotation
				.slideX(-20 + 20f * (float) (MathHelper.cos(data.getLimbSwing() * 0.6662F + (float) Math.PI)));
		data.leftArm.rotation.slideX(-20 + 20f * (float) (MathHelper.cos(data.getLimbSwing() * 0.6662F)));

		data.leftArm.rotation.addZ(-10.0F);
		data.rightArm.rotation.addZ(10.0F);

		data.leftForeLeg.rotation.slideX((var > 1 ? 45 : 10), 0.3f);
		data.rightForeLeg.rotation.slideX((var > 1 ? 10 : 45), 0.3f);
		// data.leftForeArm.rotation.slideX((var > 1 ? -10 : -45), 0.01f);
		// data.rightForeArm.rotation.slideX((var > 1 ? -45 : -10), 0.01f);

		float var2 = 25.0f + (float) Math.cos(data.getLimbSwing() * 0.6662F * 2.0f) * 5;
		data.body.rotation.addX(var2);
	}
}
