package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.math.MathHelper;

public class WalkAnimationBit<T extends BipedEntityData<?>> extends AnimationBit<T>
{
	private static final String[] ACTIONS = new String[] { "walk" };
	
	protected final float KNEEL_DURATION = 0.15F;

	@Override
	public String[] getActions(T entityData)
	{
		return ACTIONS;
	}
	
	@Override
	public void perform(T data)
	{
		data.localOffset.slideToZero(0.3F);
		data.globalOffset.slideToZero(0.3F);
		data.centerRotation.setSmoothness(.3F).orientZero();
		data.renderRotation.setSmoothness(.3F).orientZero();
		data.renderRightItemRotation.setSmoothness(.3F).orientZero();
		data.renderLeftItemRotation.setSmoothness(.3F).orientZero();
		
		final float PI = (float) Math.PI;
		float limbSwing = data.limbSwing.get() * 0.6662F;
		float armSwingAmount = data.limbSwingAmount.get() * 0.5F / PI * 180F;
		data.rightArm.rotation.setSmoothness(0.8F).orientX(MathHelper.cos(limbSwing + PI) * armSwingAmount)
				.rotateZ(5);
		data.leftArm.rotation.setSmoothness(0.8F).orientX(MathHelper.cos(limbSwing) * armSwingAmount)
				.rotateZ(-5);

		float legSwingAmount = 0.7F * data.limbSwingAmount.get() / PI * 180F;
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
		float var10 = data.headYaw.get() * .1F;
		var10 = Math.max(-10, Math.min(var10, 10));
		data.body.rotation.setSmoothness(0.5F).orientY(bodyRotationY)
				.rotateX(bodyRotationX)
				.rotateZ(-var10);
		
		data.head.rotation.setSmoothness(0.5F).orientX(MathHelper.wrapDegrees(data.headPitch.get()) - bodyRotationX)
										  	  .rotateY(MathHelper.wrapDegrees(data.headYaw.get()) - bodyRotationY);

		data.globalOffset.slideY(MathHelper.cos(limbSwing * 2) * 0.6F);
		
		float touchdown = Math.min(data.getTicksAfterTouchdown() * KNEEL_DURATION, 1.0F);
		if (touchdown < 1.0F)
		{
			data.body.rotation.setSmoothness(1F);
			data.body.rotation.orient(20.0F * (1 - touchdown), 1F, 0F, 0F);
			data.globalOffset.setY((float) -Math.sin(touchdown * Math.PI) * 2.0F);
		}
	}
}
