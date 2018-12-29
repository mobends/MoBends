package net.gobbob.mobends.standard.animation.bit.biped;

import net.gobbob.mobends.core.animation.bit.AnimationBit;
import net.gobbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.math.MathHelper;

public class SneakAnimationBit extends AnimationBit<BipedEntityData<?>>
{
	private static final String[] ACTIONS = new String[] { "sneak" };
	
	@Override
	public String[] getActions(BipedEntityData<?> entityData)
	{
		return ACTIONS;
	}

	@Override
	public void perform(BipedEntityData<?> data)
	{
		data.renderOffset.slideY(-1.3F);
	
		final float PI = (float) Math.PI;
		float limbSwing = data.limbSwing.get() * 0.6662F;
		float limbSwingAmount = data.limbSwingAmount.get() * 1.4F * 1.1F / PI * 180F;
		float var = (limbSwing / PI) % 2;
		data.rightLeg.rotation.setSmoothness(1.0F).orientX(MathHelper.cos(limbSwing) * limbSwingAmount - 5F)
				.rotateZ(10);
		data.leftLeg.rotation.setSmoothness(1.0F).orientX(MathHelper.cos(limbSwing + PI) * limbSwingAmount - 5F)
				.rotateZ(-10);

		data.rightArm.rotation.setSmoothness(0.8F).orientX(20F * MathHelper.cos(limbSwing + PI) - 20F)
				.rotateZ(10.0F);
		data.leftArm.rotation.setSmoothness(0.8F).orientX(20F * MathHelper.cos(limbSwing) - 20F)
				.rotateZ(-10.0F);
		
		data.leftForeLeg.rotation.setSmoothness(0.3F).orientX(var > 1 ? 45F : 10F);
		data.rightForeLeg.rotation.setSmoothness(0.3F).orientX(var > 1 ? 10F : 45F);

		float var2 = 25F + MathHelper.cos(limbSwing * 2F) * 5F;
		data.body.rotation.localRotateX(var2);
		data.head.rotation.rotateX(-var2);
	}
}
