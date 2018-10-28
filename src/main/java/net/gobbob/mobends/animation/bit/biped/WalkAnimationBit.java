package net.gobbob.mobends.animation.bit.biped;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.data.BipedEntityData;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.util.math.MathHelper;

public class WalkAnimationBit<T extends BipedEntityData> extends AnimationBit<T>
{
	private static final String[] ACTIONS = new String[] { "walk" };
	
	protected final float kneelDuration = 0.15F;

	@Override
	public String[] getActions(T entityData)
	{
		return ACTIONS;
	}
	
	@Override
	public void perform(T data)
	{
		data.renderOffset.slideToZero(0.3F);
		data.renderRotation.setSmoothness(.3F).orientZero();
		data.renderRightItemRotation.setSmoothness(.3F).orientZero();
		data.renderLeftItemRotation.setSmoothness(.3F).orientZero();
		
		final float PI = (float) Math.PI;
		float limbSwing = data.getLimbSwing() * 0.6662F;
		float armSwingAmount = data.getLimbSwingAmount() * 0.5F / PI * 180F;
		data.rightArm.rotation.setSmoothness(0.8F).orientX(MathHelper.cos(limbSwing + PI) * armSwingAmount)
				.rotateZ(5);
		data.leftArm.rotation.setSmoothness(0.8F).orientX(MathHelper.cos(limbSwing) * armSwingAmount)
				.rotateZ(-5);

		float legSwingAmount = 0.7F * data.getLimbSwingAmount() / PI * 180F;
		data.rightLeg.rotation.setSmoothness(1.0F).orientX(-5F + MathHelper.cos(limbSwing) * legSwingAmount)
				.rotateZ(2);
		data.leftLeg.rotation.setSmoothness(1.0F).orientX(-5F + MathHelper.cos(limbSwing + PI) * legSwingAmount)
				.rotateZ(-2);
		
		float var = (limbSwing / PI) % 2;
		data.leftForeLeg.rotation.setSmoothness(0.5F).orientX(var > 1 ? 45F : 0F);
		data.rightForeLeg.rotation.setSmoothness(0.5F).orientX(var > 1 ? 0F : 45F);
		data.leftForeArm.rotation.setSmoothness(0.8F).orientX(MathHelper.cos(limbSwing + PI/2) * -10F - 10F);
		data.rightForeArm.rotation.setSmoothness(0.8F).orientX(MathHelper.cos(limbSwing) * -10F - 10F);

		float bodyRotationY = MathHelper.cos(limbSwing) * -20F;
		float bodyRotationX = MathHelper.cos(limbSwing * 2F) * 5F + 3F;
		float var10 = data.getHeadYaw() * .1F;
		var10 = Math.max(-10, Math.min(var10, 10));
		data.body.rotation.setSmoothness(0.5F).orientY(bodyRotationY)
				.rotateX(bodyRotationX)
				.rotateZ(-var10);
		
		data.head.rotation.setSmoothness(0.5F).orientX(MathHelper.wrapDegrees(data.getHeadPitch()) - bodyRotationX)
											  .rotateY(MathHelper.wrapDegrees(data.getHeadYaw()) - bodyRotationY);

		data.renderOffset.slideY(MathHelper.cos(limbSwing * 2) * 0.6F);
		
		float touchdown = Math.min(data.getTicksAfterTouchdown() * kneelDuration, 1.0F);
		if (touchdown < 1.0F)
		{
			data.body.rotation.setSmoothness(1F);
			data.body.rotation.orient(20.0F * (1 - touchdown), 1F, 0F, 0F);
			data.renderOffset.setY((float) -Math.sin(touchdown * Math.PI) * 2.0F);
		}
	}
}
