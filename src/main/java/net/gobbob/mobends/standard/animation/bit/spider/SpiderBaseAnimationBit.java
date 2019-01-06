package net.gobbob.mobends.standard.animation.bit.spider;

import net.gobbob.mobends.core.animation.bit.AnimationBit;
import net.gobbob.mobends.core.client.event.DataUpdateHandler;
import net.gobbob.mobends.core.util.GUtil;
import net.gobbob.mobends.core.util.SmoothOrientation;
import net.gobbob.mobends.core.util.Vector3;
import net.gobbob.mobends.standard.data.SpiderData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.monster.EntitySpider;
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
	}
	
	@Override
	public void perform(SpiderData data)
	{
		final float pt = DataUpdateHandler.partialTicks;
		EntitySpider spider = data.getEntity();
		
		float headYaw = data.headYaw.get();
		float headPitch = data.headPitch.get();
		float ticks = DataUpdateHandler.getTicks();
		
		double groundLevel = Math.sin(ticks * 0.1F) * 0.5;
		float touchdown = Math.min(data.getTicksAfterTouchdown() / kneelDuration, 1.0F);
		
		if (touchdown < 1.0F)
		{
			float touchdownInv = 1.0F - touchdown;
			groundLevel += Math.sin((touchdown * 1.2F - 0.2F) * Math.PI * 2) * 7.0F * touchdownInv;
		}
		
		data.spiderHead.rotation.orientInstantX(headPitch);
		data.spiderHead.rotation.rotateY(headYaw).finish();
		
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
        
    	final double bodyX = Math.sin(ticks * 0.2F) * 0.4;
    	final double bodyZ = Math.cos(ticks * 0.2F) * 0.4;
    	
        for (SpiderData.Limb limb : data.limbs)
        {
        	SpiderData.IKResult ikResult = limb.solveIK(bodyX, bodyZ, pt);
        	double deviation = GUtil.getRadianDifference(limb.getNaturalYaw(), ikResult.xzAngle + Math.PI/2);
        	
        	if (deviation > 0.9 || ikResult.xzDistance*0.0625 > 1.2)
        	{
        		//limb.adjustToNeutralPosition();
        	}
        	
        	limb.applyIK(ikResult, groundLevel, 4, pt);
        }
        
        data.limbs[6].adjustToLocalPosition(0, 5.5, 0.2F);
        data.limbs[7].adjustToLocalPosition(0, 5.5, 0.2F);
        
        data.renderOffset.set((float) bodyX, (float) -groundLevel, (float) -bodyZ);
        data.renderRotation.orientZero();
	}
}
