package net.gobbob.mobends.standard.animation.bit.spider;

import net.gobbob.mobends.core.animation.bit.AnimationBit;
import net.gobbob.mobends.core.util.GUtil;
import net.gobbob.mobends.standard.data.SpiderData;

public class SpiderJumpAnimationBit extends AnimationBit<SpiderData>
{
	@Override
	public String[] getActions(SpiderData entityData)
	{
		return new String[] { "jump" };
	}
	
	@Override
	public void perform(SpiderData data)
	{
		float ticksInAir = data.getTicksInAir();
		ticksInAir = Math.min(ticksInAir * 0.1F, 1.0F);
		ticksInAir = (float) GUtil.easeInOut(ticksInAir, 3);
		
		float legAngle = -100.0F + ticksInAir * 95.0F;
		float smoothness = 1F;
		data.limbs[0].upperPart.rotation.setSmoothness(smoothness).localRotateZ(legAngle);
		data.limbs[1].upperPart.rotation.setSmoothness(smoothness).localRotateZ(-legAngle);
		data.limbs[2].upperPart.rotation.setSmoothness(smoothness).localRotateZ(legAngle);
		data.limbs[3].upperPart.rotation.setSmoothness(smoothness).localRotateZ(-legAngle);
		data.limbs[4].upperPart.rotation.setSmoothness(smoothness).localRotateZ(legAngle);
		data.limbs[5].upperPart.rotation.setSmoothness(smoothness).localRotateZ(-legAngle);
		data.limbs[6].upperPart.rotation.setSmoothness(smoothness).localRotateZ(legAngle);
		data.limbs[7].upperPart.rotation.setSmoothness(smoothness).localRotateZ(-legAngle);
		
		float foreLegAngle = 70.0F - ticksInAir * 50.0F;
		data.limbs[0].lowerPart.rotation.setSmoothness(smoothness).rotateZ(foreLegAngle);
		data.limbs[1].lowerPart.rotation.setSmoothness(smoothness).rotateZ(-foreLegAngle);
		data.limbs[2].lowerPart.rotation.setSmoothness(smoothness).rotateZ(foreLegAngle);
		data.limbs[3].lowerPart.rotation.setSmoothness(smoothness).rotateZ(-foreLegAngle);
		data.limbs[4].lowerPart.rotation.setSmoothness(smoothness).rotateZ(foreLegAngle);
		data.limbs[5].lowerPart.rotation.setSmoothness(smoothness).rotateZ(-foreLegAngle);
		data.limbs[6].lowerPart.rotation.setSmoothness(smoothness).rotateZ(foreLegAngle);
		data.limbs[7].lowerPart.rotation.setSmoothness(smoothness).rotateZ(-foreLegAngle);
	}
}
