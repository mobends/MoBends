package net.gobbob.mobends.standard.animation.bit.spider;

import net.gobbob.mobends.core.animation.bit.AnimationBit;
import net.gobbob.mobends.core.client.event.DataUpdateHandler;
import net.gobbob.mobends.core.util.GUtil;
import net.gobbob.mobends.standard.data.SpiderData;
import net.minecraft.util.math.MathHelper;

public class SpiderBaseAnimationBit extends AnimationBit<SpiderData>
{
	protected static final float PI = (float) Math.PI;
	protected final float kneelDuration = 0.15F;
	
	@Override
	public String[] getActions(SpiderData entityData)
	{
		return null;
	}
	
	@Override
	public void onPlay(SpiderData data)
	{
		float legBend = 33.3F;
		for (int i = 0; i < data.upperLimbs.length; ++i)
		{
			data.upperLimbs[i].part.rotation.orientInstantZ(i%2 == 0 ? legBend : -legBend);
			data.upperLimbs[i].part.rotation.rotateX(0.0F);
		}
        
        data.upperLimbs[0].part.rotation.rotateY(65F);
        data.upperLimbs[1].part.rotation.rotateY(-65F);
        data.upperLimbs[2].part.rotation.rotateY(40F);
        data.upperLimbs[3].part.rotation.rotateY(-40F);
        data.upperLimbs[4].part.rotation.rotateY(-40F);
        data.upperLimbs[5].part.rotation.rotateY(40F);
        data.upperLimbs[6].part.rotation.rotateY(-65F);
        data.upperLimbs[7].part.rotation.rotateY(65F);
        
        final float foreBend = 89;
        for (int i = 0; i < data.lowerLimbs.length; ++i)
		{
			data.lowerLimbs[i].part.rotation.orientInstantZ(i%2 == 0 ? -foreBend : foreBend);
		}
	}
	
	@Override
	public void perform(SpiderData data)
	{
		float headYaw = data.headYaw.get();
		float headPitch = data.headPitch.get();
		float ticks = data.getEntity().ticksExisted + DataUpdateHandler.partialTicks;
		float bodyHeight = 0.0F;
		
		float touchdown = Math.min(data.getTicksAfterTouchdown() * kneelDuration, 1.0F);
		
		if (touchdown < 1.0F)
		{
			float touchdownInv = 1.0F - touchdown;
			bodyHeight = touchdownInv * -15.0F;
		}
		
		data.renderOffset.setY(bodyHeight * 0.2F + -2F);
		
		data.spiderHead.rotation.orientInstantX(headPitch);
		data.spiderHead.rotation.rotateY(headYaw);
        //float legBend = 33.3F - bodyHeight;
        float legBend = 0F;
		
        for (int i = 0; i < data.upperLimbs.length; ++i)
		{
			data.upperLimbs[i].part.rotation.orientZ(i%2 == 0 ? legBend : -legBend);
		}
        
        data.upperLimbs[0].part.rotation.rotateY(65F);
        data.upperLimbs[1].part.rotation.rotateY(-65F);
        data.upperLimbs[2].part.rotation.rotateY(20F);
        data.upperLimbs[3].part.rotation.rotateY(-20F);
        data.upperLimbs[4].part.rotation.rotateY(-20F);
        data.upperLimbs[5].part.rotation.rotateY(20F);
        data.upperLimbs[6].part.rotation.rotateY(-65F);
        data.upperLimbs[7].part.rotation.rotateY(65F);
        
        float foreBend = 89 - bodyHeight * 0.5F;
        for (int i = 0; i < data.lowerLimbs.length; ++i)
		{
			data.lowerLimbs[i].part.rotation.orientZ(i%2 == 0 ? -foreBend : foreBend);
		}
        
        float limbSwing = data.limbSwing.get() * 0.8662F;
		float limbSwingAmount = data.limbSwingAmount.get();
		float forwardBackSwing = limbSwingAmount * 16;
		float upDownSwing = limbSwingAmount * 26;
		
        float f3 = forwardBackSwing * MathHelper.cos(limbSwing + 0.0F);
        float f4 = forwardBackSwing * MathHelper.cos(limbSwing + GUtil.PI);
        float f5 = forwardBackSwing * MathHelper.cos(limbSwing + (GUtil.PI / 2F));
        float f6 = forwardBackSwing * MathHelper.cos(limbSwing + (GUtil.PI * 3F / 2F));
        
        // Lifting limbs off the ground
        float f7 =  upDownSwing * Math.max(0F, MathHelper.sin(limbSwing + 0.0F));
        float f8 =  upDownSwing * Math.max(0F, MathHelper.sin(limbSwing + GUtil.PI));
        float f9 =  upDownSwing * Math.max(0F, MathHelper.sin(limbSwing + (GUtil.PI / 2F)));
        float f10 = upDownSwing * Math.max(0F, MathHelper.sin(limbSwing + (GUtil.PI * 3F / 2F)));
        
        /*data.upperLimbs[0].part.rotation.rotateY(f3);
        data.upperLimbs[1].part.rotation.rotateY(-f4);
        data.upperLimbs[2].part.rotation.rotateY(f4);
        data.upperLimbs[3].part.rotation.rotateY(-f3);
        data.upperLimbs[4].part.rotation.rotateY(f5);
        data.upperLimbs[5].part.rotation.rotateY(-f6);
        data.upperLimbs[6].part.rotation.rotateY(f6);
        data.upperLimbs[7].part.rotation.rotateY(-f5);
        
        data.upperLimbs[0].part.rotation.localRotateZ(f7);
        data.upperLimbs[1].part.rotation.localRotateZ(-f8);
        data.upperLimbs[2].part.rotation.localRotateZ(f8);
        data.upperLimbs[3].part.rotation.localRotateZ(-f7);
        data.upperLimbs[4].part.rotation.localRotateZ(f9);
        data.upperLimbs[5].part.rotation.localRotateZ(-f10);
        data.upperLimbs[6].part.rotation.localRotateZ(f10);
        data.upperLimbs[7].part.rotation.localRotateZ(-f9);*/
        
        
        float beta = MathHelper.sin(ticks * 0.3F) * 1F;
        beta = Math.max(-0.2F, beta);
        
        for (int i = 0; i < data.upperLimbs.length; ++i)
        {
        	float angle = getLowerLimbAngle(beta);
	        data.upperLimbs[i].part.rotation.localRotateZ((i%2 == 0 ? beta : -beta) / GUtil.PI * 180F);
	        data.lowerLimbs[i].part.rotation.orientZ((i%2 == 0 ? -angle : angle) / GUtil.PI * 180F);
        }
        
        /*final float foreArmBend = 2F;
        data.spiderForeLeg1.rotation.localRotateZ(-f7 * foreArmBend);
        data.spiderForeLeg1.rotation.localRotateZ(f7 * foreArmBend);
        data.spiderForeLeg1.rotation.localRotateZ(-f8 * foreArmBend);
        data.spiderForeLeg1.rotation.localRotateZ(f8 * foreArmBend);
        data.spiderForeLeg1.rotation.localRotateZ(-f9 * foreArmBend);
        data.spiderForeLeg1.rotation.localRotateZ(f9 * foreArmBend);
        data.spiderForeLeg1.rotation.localRotateZ(-f10 * foreArmBend);
        data.spiderForeLeg1.rotation.localRotateZ(f10 * foreArmBend);*/
	}
	
	public static float getLowerLimbAngle(float upperLimbAngle)
	{
		float limbSegmentLength = 8;
        float height = 0;
        
        float xsinB = limbSegmentLength * MathHelper.sin(upperLimbAngle);
        float gamma = (float) Math.acos((height + xsinB) / limbSegmentLength);
        float alpha = 90 - gamma + upperLimbAngle;
        if (Float.isNaN(alpha))
        	return 0;
        return alpha;
	}
}
