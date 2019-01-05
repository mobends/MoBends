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
		float touchdown = Math.min(data.getTicksAfterTouchdown() * kneelDuration, 1.0F);
		
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
    	final double renderYawOffset = (spider.prevRenderYawOffset + (spider.renderYawOffset - spider.prevRenderYawOffset) * pt ) / 180 * Math.PI;
    	final double ryo = renderYawOffset;
    	final double worldX = spider.prevPosX + (spider.posX - spider.prevPosX) * pt;
    	final double worldZ = spider.prevPosZ + (spider.posZ - spider.prevPosZ) * pt;
    	
        for (int i = 0; i < data.limbs.length; ++i)
        {
        	SpiderData.Limb limb = data.limbs[i];
        	boolean odd = i % 2 == 1;
        	
        	double t = (double) i / (data.limbs.length-1) * 2 - 1;
        	t = odd ? (-t*1.3) : (Math.PI + t*1.3);
        	
        	double worldLimbX = (limb.getPrevWorldX() + (limb.getWorldX() - limb.getPrevWorldX()) * pt);
        	double worldLimbZ = (limb.getPrevWorldZ() + (limb.getWorldZ() - limb.getPrevWorldZ()) * pt);
        	double localX = (worldLimbX - worldX) / 0.0625;
        	double localZ = -(worldLimbZ - worldZ) / 0.0625;
        	double x = localX * Math.cos(ryo) - localZ * Math.sin(ryo) - bodyX;
        	double z = localX * Math.sin(ryo) + localZ * Math.cos(ryo) - bodyZ;
        	double partX = data.limbs[i].upperPart.position.x;
        	double partZ = data.limbs[i].upperPart.position.z;
        	double dX = (partX - x);
        	double dZ = (partZ - z);
        	double xzDistance = Math.sqrt(dX * dX + dZ * dZ);
        	double xzAngle = Math.atan2(dX, dZ);
        	double diff = GUtil.getRadianDifference(limb.getNaturalYaw(), xzAngle + Math.PI/2);
        	
        	xzAngle = odd ? (Math.PI/2 + xzAngle) : (-Math.PI/2 + xzAngle);
        	limb.upperPart.rotation.orientY((float)(xzAngle / Math.PI * 180F));
        	
        	if (diff > 0.9 || xzDistance*0.0625 > 1.2)
        	{
        		limb.adjustToNeutralPosition();
        	}
        	
        	putLimbOnGround(limb.upperPart.rotation, limb.lowerPart.rotation, odd, xzDistance, groundLevel - 7 + Math.sin(limb.getAdjustingProgress() * Math.PI) * 4);
        }
        
        data.renderOffset.set((float) bodyX, (float) -groundLevel, (float) -bodyZ);
        data.renderRotation.orientZero();
	}
	
	public static float getLowerLimbAngle(float upperLimbAngle)
	{
		final float limbSegmentLength = 12;
        final float height = 0;
        
        float xsinB = limbSegmentLength * MathHelper.sin(upperLimbAngle);
        float gamma = (float) Math.acos((height + xsinB) / limbSegmentLength);
        float alpha = 90 - gamma + upperLimbAngle;
        return alpha;
	}
	
	public static void putLimbInPlace(SpiderData.Limb limb, boolean odd, double groundLevel)
	{
		
	}
	
	public static void putLimbOnGround(SmoothOrientation upperLimb, SmoothOrientation lowerLimb, boolean odd, double stretchDistance, double groundLevel)
	{
		final float limbSegmentLength = 12F;
		final float maxStretch = limbSegmentLength * 2;
		
		double c = groundLevel == 0F ? stretchDistance : Math.sqrt(stretchDistance * stretchDistance + groundLevel * groundLevel);
		
		final double alpha = c > maxStretch ? 0 : Math.acos((c/2)/limbSegmentLength);
		final double beta = Math.atan2(stretchDistance, -groundLevel);
		
		double lowerAngle = Math.max(-2.3, -2 * alpha);
		double upperAngle = Math.min(1, alpha + beta - Math.PI/2);
		upperLimb.localRotateZ((float) (upperAngle / Math.PI * 180) * (odd ? -1 : 1)).finish();
		lowerLimb.orientInstantZ((float) (lowerAngle / Math.PI * 180) * (odd ? -1 : 1));
	}
}
