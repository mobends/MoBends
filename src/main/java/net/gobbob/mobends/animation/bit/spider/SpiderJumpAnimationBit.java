package net.gobbob.mobends.animation.bit.spider;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.SpiderData;
import net.gobbob.mobends.util.GUtil;

public class SpiderJumpAnimationBit extends AnimationBit
{
	@Override
	public String[] getActions(EntityData entityData)
	{
		return new String[] { "jump" };
	}
	
	@Override
	public void perform(EntityData entityData)
	{
		if (!(entityData instanceof SpiderData))
			return;
		SpiderData data = (SpiderData) entityData;
		
		float ticksInAir = data.getTicksAfterLiftoff();
		ticksInAir = Math.min(ticksInAir * 0.1F, 1.0F);
		ticksInAir = (float) GUtil.easeInOut(ticksInAir, 3);
		
		float legAngle = -50.0F + ticksInAir * 65.0F;
		float smoothness = 0.5F;
		data.spiderLeg1.rotation.slideZ(legAngle, smoothness);
		data.spiderLeg2.rotation.slideZ(-legAngle, smoothness);
		data.spiderLeg3.rotation.slideZ(legAngle, smoothness);
		data.spiderLeg4.rotation.slideZ(-legAngle, smoothness);
		data.spiderLeg5.rotation.slideZ(legAngle, smoothness);
		data.spiderLeg6.rotation.slideZ(-legAngle, smoothness);
		data.spiderLeg7.rotation.slideZ(legAngle, smoothness);
		data.spiderLeg8.rotation.slideZ(-legAngle, smoothness);
		
		float foreLegAngle = -20.0F - ticksInAir * 50.0F;
		data.spiderForeLeg1.rotation.slideZ(foreLegAngle, smoothness);
		data.spiderForeLeg2.rotation.slideZ(-foreLegAngle, smoothness);
		data.spiderForeLeg3.rotation.slideZ(foreLegAngle, smoothness);
		data.spiderForeLeg4.rotation.slideZ(-foreLegAngle, smoothness);
		data.spiderForeLeg5.rotation.slideZ(foreLegAngle, smoothness);
		data.spiderForeLeg6.rotation.slideZ(-foreLegAngle, smoothness);
		data.spiderForeLeg7.rotation.slideZ(foreLegAngle, smoothness);
		data.spiderForeLeg8.rotation.slideZ(-foreLegAngle, smoothness);
		
	}
}
