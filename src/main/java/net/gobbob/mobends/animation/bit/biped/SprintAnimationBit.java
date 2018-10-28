package net.gobbob.mobends.animation.bit.biped;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.data.BipedEntityData;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.pack.BendsPack;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

public class SprintAnimationBit extends AnimationBit<BipedEntityData>
{
	private static String[] ACTIONS = new String[] { "sprint" };
	
	@Override
	public String[] getActions(BipedEntityData entityData)
	{
		return ACTIONS;
	}

	@Override
	public void perform(BipedEntityData data)
	{
		if (!(data.getEntity() instanceof EntityLivingBase))
			return;

		data.renderOffset.slideToZero(0.1F);
		data.renderRotation.setSmoothness(.3F).orientZero();
		data.renderRightItemRotation.setSmoothness(.3F).orientZero();
		data.renderLeftItemRotation.setSmoothness(.3F).orientZero();

		final float PI = (float) Math.PI;
		float limbSwing = data.getLimbSwing() * 0.6662F * 0.8F;
		float armSwingAmount =  data.getLimbSwingAmount() / PI * 180F * 1.1F;
		data.rightArm.rotation.setSmoothness(0.8F).orientX(MathHelper.cos(limbSwing + PI) * armSwingAmount)
				.rotateZ(5);
		data.leftArm.rotation.setSmoothness(0.8F).orientX(MathHelper.cos(limbSwing) * armSwingAmount)
				.rotateZ(-5);
		
		float legSwingAmount = 1.26F * data.getLimbSwingAmount() / PI * 180F;
		data.rightLeg.rotation.setSmoothness(1.0F).orientX(-5F + MathHelper.cos(limbSwing) * legSwingAmount)
				.rotateZ(2);
		data.leftLeg.rotation.setSmoothness(1.0F).orientX(-5F + MathHelper.cos(limbSwing + PI) * legSwingAmount)
				.rotateZ(-2);

		float foreLegSwingAmount = 0.7F * data.getLimbSwingAmount() / PI * 180F;
		float var = (limbSwing / PI) % 2;
		data.leftForeLeg.rotation.setSmoothness(.7F).orientX(40F + MathHelper.cos(limbSwing + 1.8F) * foreLegSwingAmount);
		data.rightForeLeg.rotation.setSmoothness(.7F).orientX(40F + MathHelper.cos(limbSwing + PI + 1.8F) * foreLegSwingAmount);
		data.leftForeArm.rotation.setSmoothness(.3F).orientX((var > 1 ? -10F : -45F));
		data.rightForeArm.rotation.setSmoothness(.3F).orientX((var > 1 ? -45F : -10F));

		float bodyRotationY = MathHelper.cos(limbSwing) * -40;
		float bodyRotationX = MathHelper.cos(limbSwing * 2F) * 10F + 10F;
		float var10 = data.getHeadYaw() * .3F;
		var10 = Math.max(-10, Math.min(var10, 10));
		data.body.rotation.setSmoothness(.8F).orientY(bodyRotationY)
				.rotateX(bodyRotationX)
				.rotateZ(-var10);
		data.head.rotation.setSmoothness(.5F).orientX(MathHelper.wrapDegrees(data.getHeadPitch()) - bodyRotationX)
											 .rotateY(MathHelper.wrapDegrees(data.getHeadYaw()) - bodyRotationY);
		

		data.renderOffset.slideY(MathHelper.cos(limbSwing * 2F + 0.6F) * 1.5f, .9f);
	}

}
