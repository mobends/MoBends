package net.gobbob.mobends.animation.bit.spider;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.client.event.EntityRenderHandler;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.SpiderData;
import net.minecraft.util.math.MathHelper;

public class SpiderDeathAnimationBit extends AnimationBit
{
	protected static final float PI = (float) Math.PI;
	
	protected float wiggleSpeedMultiplier = 1.0F;
	protected float wigglePhase = 0.0F;
	
	@Override
	public String[] getActions(EntityData entityData)
	{
		return new String[] { "death" };
	}
	
	@Override
	public void onPlay(EntityData entityData)
	{
		wiggleSpeedMultiplier = 1.0F;
		wigglePhase = 0.0F;
	}
	
	@Override
	public void perform(EntityData entityData)
	{
		if (!(entityData instanceof SpiderData))
			return;
		SpiderData data = (SpiderData) entityData;
		
		data.renderOffset.slideY(10.0F, 0.3F);
		
		float headYaw = data.getHeadYaw();
		float headPitch = data.getHeadPitch();
		
		data.spiderHead.rotation.orientInstantX(headPitch);
		data.spiderHead.rotation.rotateY(headYaw);
		
        data.spiderLeg1.rotation.orientInstantZ(-45F);
        data.spiderLeg2.rotation.orientInstantZ(45F);
        data.spiderLeg3.rotation.orientInstantZ(-33.3F);
        data.spiderLeg4.rotation.orientInstantZ(33.3F);
        data.spiderLeg5.rotation.orientInstantZ(-33.3F);
        data.spiderLeg6.rotation.orientInstantZ(33.3F);
        data.spiderLeg7.rotation.orientInstantZ(-45F);
        data.spiderLeg8.rotation.orientInstantZ(45F);
        
        data.spiderLeg1.rotation.rotateY(45F);
        data.spiderLeg2.rotation.rotateY(-45F);
        data.spiderLeg3.rotation.rotateY(22.5F);
        data.spiderLeg4.rotation.rotateY(-22.5F);
        data.spiderLeg5.rotation.rotateY(-22.5F);
        data.spiderLeg6.rotation.rotateY(22.5F);
        data.spiderLeg7.rotation.rotateY(-45F);
        data.spiderLeg8.rotation.rotateY(45F);
        
        float foreBend = 89;
        data.spiderForeLeg1.rotation.orientInstantZ(-foreBend);
        data.spiderForeLeg2.rotation.orientInstantZ(foreBend);
        data.spiderForeLeg3.rotation.orientInstantZ(-foreBend);
        data.spiderForeLeg4.rotation.orientInstantZ(foreBend);
        data.spiderForeLeg5.rotation.orientInstantZ(-foreBend);
        data.spiderForeLeg6.rotation.orientInstantZ(foreBend);
        data.spiderForeLeg7.rotation.orientInstantZ(-foreBend);
        data.spiderForeLeg8.rotation.orientInstantZ(foreBend);
        
        float limbSwing = data.getLimbSwing() * 0.6662F;
		float limbSwingAmount = data.getLimbSwingAmount() / (float) Math.PI * 180F;
        float f3 = -(MathHelper.cos(limbSwing * 2.0F + 0.0F) * 0.4F) * limbSwingAmount;
        float f4 = -(MathHelper.cos(limbSwing * 2.0F + (float)Math.PI) * 0.4F) * limbSwingAmount;
        float f5 = -(MathHelper.cos(limbSwing * 2.0F + ((float)Math.PI / 2F)) * 0.4F) * limbSwingAmount;
        float f6 = -(MathHelper.cos(limbSwing * 2.0F + ((float)Math.PI * 3F / 2F)) * 0.4F) * limbSwingAmount;
        float f7 = Math.abs(MathHelper.sin(limbSwing + 0.0F) * 0.4F) * limbSwingAmount;
        float f8 = Math.abs(MathHelper.sin(limbSwing + (float)Math.PI) * 0.4F) * limbSwingAmount;
        float f9 = Math.abs(MathHelper.sin(limbSwing + ((float)Math.PI / 2F)) * 0.4F) * limbSwingAmount;
        float f10 = Math.abs(MathHelper.sin(limbSwing + ((float)Math.PI * 3F / 2F)) * 0.4F) * limbSwingAmount;
        data.spiderLeg1.rotation.rotateY(f3);
        data.spiderLeg2.rotation.rotateY(-f3);
        data.spiderLeg3.rotation.rotateY(f4);
        data.spiderLeg4.rotation.rotateY(-f4);
        data.spiderLeg5.rotation.rotateY(f5);
        data.spiderLeg6.rotation.rotateY(-f5);
        data.spiderLeg7.rotation.rotateY(f6);
        data.spiderLeg8.rotation.rotateY(-f6);
        
        if (wiggleSpeedMultiplier > 0.0F)
        {
        	wiggleSpeedMultiplier -= DataUpdateHandler.ticksPerFrame*0.1F;
        	wiggleSpeedMultiplier = Math.max(0, wiggleSpeedMultiplier);
        }
        
        wigglePhase += (0.3F + wiggleSpeedMultiplier * 2F) *  DataUpdateHandler.ticksPerFrame;
        
        float wiggleAmount = 10.0F + wiggleSpeedMultiplier*10.0F;
        float wiggle1 = MathHelper.cos(wigglePhase) * wiggleAmount;
        float wiggle2 = MathHelper.cos(wigglePhase + PI/4) * wiggleAmount;
        float wiggle3 = MathHelper.cos(wigglePhase + PI/2) * wiggleAmount;
        float wiggle4 = MathHelper.cos(wigglePhase + PI/4*3) * wiggleAmount;
        
        data.spiderLeg1.rotation.rotateZ(f7 + wiggle1);
        data.spiderLeg2.rotation.rotateZ(-f7 + wiggle2);
        data.spiderLeg3.rotation.rotateZ(f8 + wiggle3);
        data.spiderLeg4.rotation.rotateZ(-f8 + wiggle4);
        data.spiderLeg5.rotation.rotateZ(f9 + wiggle1);
        data.spiderLeg6.rotation.rotateZ(-f9 + wiggle2);
        data.spiderLeg7.rotation.rotateZ(f10 + wiggle3);
        data.spiderLeg8.rotation.rotateZ(-f10 + wiggle4);
	}
}
