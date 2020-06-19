package goblinbob.mobends.standard.animation.bit.skeleton;

import goblinbob.mobends.standard.data.SkeletonData;
import net.minecraft.util.math.MathHelper;

public class WalkAnimationBit extends goblinbob.mobends.standard.animation.bit.biped.WalkAnimationBit<SkeletonData>
{
	@Override
	public void perform(SkeletonData data)
	{
		super.perform(data);

		if (data.isStrafing())
		{
			final float PI = (float) Math.PI;
			float limbSwing = data.limbSwing.get() * 0.6662F;

			float legSwingAmount = 0.7F * data.limbSwingAmount.get() / PI * 180F;
			data.rightLeg.rotation.setSmoothness(1.0F).orientZ(-5F + MathHelper.cos(limbSwing) * legSwingAmount);
			data.leftLeg.rotation.setSmoothness(1.0F).orientZ(-5F + MathHelper.cos(limbSwing + PI) * legSwingAmount);
		}
	}
}
