package net.gobbob.mobends.standard.animation.bit.spider;

import net.gobbob.mobends.core.animation.bit.AnimationBit;
import net.gobbob.mobends.core.client.event.DataUpdateHandler;
import net.gobbob.mobends.core.util.GUtil;
import net.gobbob.mobends.standard.data.SpiderData;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.util.math.MathHelper;

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
		final EntitySpider spider = data.getEntity();
		final float pt = DataUpdateHandler.partialTicks;
		float ticksInAir = data.getTicksInAir();
		ticksInAir = Math.min(ticksInAir * 0.1F, 1.0F);
		ticksInAir = (float) GUtil.easeInOut(ticksInAir, 3);
		
		for (int i = 0; i < data.limbs.length; ++i)
		{
			boolean odd = i%2 == 1;
			SpiderData.Limb limb = data.limbs[i];
			float naturalYaw = -((float) i / (data.limbs.length-1) * 2 - 1);
			naturalYaw = odd ? (-naturalYaw*1.3F) : (naturalYaw*1.3F);
			limb.upperPart.rotation.orientY(naturalYaw / GUtil.PI * 180F);
		}
		float motionY = (float) -data.getInterpolatedMotionY() * 5;
		motionY = MathHelper.clamp(motionY, -1, 1);
		System.out.println(motionY);
		float legAngle = -20.0F + motionY * 25.0F;
		float smoothness = 1F;
		data.limbs[0].upperPart.rotation.setSmoothness(smoothness).localRotateZ(legAngle);
		data.limbs[1].upperPart.rotation.setSmoothness(smoothness).localRotateZ(-legAngle);
		data.limbs[2].upperPart.rotation.setSmoothness(smoothness).localRotateZ(legAngle);
		data.limbs[3].upperPart.rotation.setSmoothness(smoothness).localRotateZ(-legAngle);
		data.limbs[4].upperPart.rotation.setSmoothness(smoothness).localRotateZ(legAngle);
		data.limbs[5].upperPart.rotation.setSmoothness(smoothness).localRotateZ(-legAngle);
		data.limbs[6].upperPart.rotation.setSmoothness(smoothness).localRotateZ(legAngle);
		data.limbs[7].upperPart.rotation.setSmoothness(smoothness).localRotateZ(-legAngle);
		
		float foreLegAngle = -70.0F - motionY * 40.0F;
		data.limbs[0].lowerPart.rotation.setSmoothness(smoothness).orientZ(foreLegAngle);
		data.limbs[1].lowerPart.rotation.setSmoothness(smoothness).orientZ(-foreLegAngle);
		data.limbs[2].lowerPart.rotation.setSmoothness(smoothness).orientZ(foreLegAngle);
		data.limbs[3].lowerPart.rotation.setSmoothness(smoothness).orientZ(-foreLegAngle);
		data.limbs[4].lowerPart.rotation.setSmoothness(smoothness).orientZ(foreLegAngle);
		data.limbs[5].lowerPart.rotation.setSmoothness(smoothness).orientZ(-foreLegAngle);
		data.limbs[6].lowerPart.rotation.setSmoothness(smoothness).orientZ(foreLegAngle);
		data.limbs[7].lowerPart.rotation.setSmoothness(smoothness).orientZ(-foreLegAngle);
	}
}
