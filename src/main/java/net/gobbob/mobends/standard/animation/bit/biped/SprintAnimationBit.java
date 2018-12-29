package net.gobbob.mobends.standard.animation.bit.biped;

import net.gobbob.mobends.core.animation.bit.AnimationBit;
import net.gobbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.math.MathHelper;

public class SprintAnimationBit<T extends BipedEntityData<?>> extends AnimationBit<T>
{
	private static String[] ACTIONS = new String[] { "sprint" };
	
	@Override
	public String[] getActions(T entityData)
	{
		return ACTIONS;
	}

	@Override
	public void perform(T data)
	{
		data.renderOffset.slideToZero(0.1F);
		data.centerRotation.setSmoothness(.3F).orientZero();
		data.renderRotation.setSmoothness(.3F).orientZero();
		data.renderRightItemRotation.setSmoothness(.3F).orientZero();
		data.renderLeftItemRotation.setSmoothness(.3F).orientZero();

		final float PI = (float) Math.PI;
		float limbSwing = data.limbSwing.get() * 0.6662F * 0.8F;
		float armSwingAmount =  data.limbSwingAmount.get() / PI * 180F * 1.1F;
		data.rightArm.rotation.setSmoothness(0.8F).orientX(MathHelper.cos(limbSwing + PI) * armSwingAmount)
				.rotateZ(5);
		data.leftArm.rotation.setSmoothness(0.8F).orientX(MathHelper.cos(limbSwing) * armSwingAmount)
				.rotateZ(-5);
		
		float legSwingAmount = 1.26F * data.limbSwingAmount.get() / PI * 180F;
		data.rightLeg.rotation.setSmoothness(1.0F).orientX(-5F + MathHelper.cos(limbSwing) * legSwingAmount)
				.rotateZ(2);
		data.leftLeg.rotation.setSmoothness(1.0F).orientX(-5F + MathHelper.cos(limbSwing + PI) * legSwingAmount)
				.rotateZ(-2);

		float foreLegSwingAmount = 0.7F * data.limbSwingAmount.get() / PI * 180F;
		float var = (limbSwing / PI) % 2;
		data.leftForeLeg.rotation.setSmoothness(.7F).orientX(40F + MathHelper.cos(limbSwing + 1.8F) * foreLegSwingAmount);
		data.rightForeLeg.rotation.setSmoothness(.7F).orientX(40F + MathHelper.cos(limbSwing + PI + 1.8F) * foreLegSwingAmount);
		data.leftForeArm.rotation.setSmoothness(.3F).orientX((var > 1 ? -10F : -45F));
		data.rightForeArm.rotation.setSmoothness(.3F).orientX((var > 1 ? -45F : -10F));

		float bodyRotationY = MathHelper.cos(limbSwing) * -40;
		float bodyRotationX = MathHelper.cos(limbSwing * 2F) * 10F + 10F;
		float var10 = data.headYaw.get() * .3F;
		var10 = Math.max(-10, Math.min(var10, 10));
		data.body.rotation.setSmoothness(.8F).orientY(bodyRotationY)
				.rotateX(bodyRotationX)
				.rotateZ(-var10);
		data.head.rotation.setSmoothness(.5F).orientX(MathHelper.wrapDegrees(data.headPitch.get()) - bodyRotationX)
											 .rotateY(MathHelper.wrapDegrees(data.headYaw.get()) - bodyRotationY);
		

		data.renderOffset.slideY(MathHelper.cos(limbSwing * 2F + 0.6F) * 1.5f, .9f);
	}

}
