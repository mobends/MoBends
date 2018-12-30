package net.gobbob.mobends.standard.animation.bit.spider;

import net.gobbob.mobends.core.animation.bit.AnimationBit;
import net.gobbob.mobends.core.client.event.DataUpdateHandler;
import net.gobbob.mobends.core.client.event.EntityRenderHandler;
import net.gobbob.mobends.core.data.EntityData;
import net.gobbob.mobends.standard.data.SpiderData;
import net.minecraft.util.math.MathHelper;

public class SpiderDeathAnimationBit extends AnimationBit<SpiderData>
{
	protected static final float PI = (float) Math.PI;
	
	protected float wiggleSpeedMultiplier = 1.0F;
	protected float wigglePhase = 0.0F;
	
	@Override
	public String[] getActions(SpiderData entityData)
	{
		return new String[] { "death" };
	}
	
	@Override
	public void onPlay(SpiderData entityData)
	{
		wiggleSpeedMultiplier = 1.0F;
		wigglePhase = 0.0F;
	}
	
	@Override
	public void perform(SpiderData data)
	{
		data.renderOffset.slideY(10.0F, 0.3F);
		
		float headYaw = data.headYaw.get();
		float headPitch = data.headPitch.get();
		
		data.spiderHead.rotation.orientInstantX(headPitch);
		data.spiderHead.rotation.rotateY(headYaw);
		
        data.upperLimbs[0].part.rotation.orientInstantZ(-45F);
        data.upperLimbs[1].part.rotation.orientInstantZ(45F);
        data.upperLimbs[2].part.rotation.orientInstantZ(-33.3F);
        data.upperLimbs[3].part.rotation.orientInstantZ(33.3F);
        data.upperLimbs[4].part.rotation.orientInstantZ(-33.3F);
        data.upperLimbs[5].part.rotation.orientInstantZ(33.3F);
        data.upperLimbs[6].part.rotation.orientInstantZ(-45F);
        data.upperLimbs[7].part.rotation.orientInstantZ(45F);
        
        data.upperLimbs[0].part.rotation.rotateY(45F);
        data.upperLimbs[1].part.rotation.rotateY(-45F);
        data.upperLimbs[2].part.rotation.rotateY(22.5F);
        data.upperLimbs[3].part.rotation.rotateY(-22.5F);
        data.upperLimbs[4].part.rotation.rotateY(-22.5F);
        data.upperLimbs[5].part.rotation.rotateY(22.5F);
        data.upperLimbs[6].part.rotation.rotateY(-45F);
        data.upperLimbs[7].part.rotation.rotateY(45F);
        
        float foreBend = 89;
        data.lowerLimbs[0].part.rotation.orientInstantZ(-foreBend);
        data.lowerLimbs[1].part.rotation.orientInstantZ(foreBend);
        data.lowerLimbs[2].part.rotation.orientInstantZ(-foreBend);
        data.lowerLimbs[3].part.rotation.orientInstantZ(foreBend);
        data.lowerLimbs[4].part.rotation.orientInstantZ(-foreBend);
        data.lowerLimbs[5].part.rotation.orientInstantZ(foreBend);
        data.lowerLimbs[6].part.rotation.orientInstantZ(-foreBend);
        data.lowerLimbs[7].part.rotation.orientInstantZ(foreBend);
        
        float limbSwing = data.limbSwing.get() * 0.6662F;
		float limbSwingAmount = data.limbSwingAmount.get() / (float) Math.PI * 180F;
        float f3 = -(MathHelper.cos(limbSwing * 2.0F + 0.0F) * 0.4F) * limbSwingAmount;
        float f4 = -(MathHelper.cos(limbSwing * 2.0F + (float)Math.PI) * 0.4F) * limbSwingAmount;
        float f5 = -(MathHelper.cos(limbSwing * 2.0F + ((float)Math.PI / 2F)) * 0.4F) * limbSwingAmount;
        float f6 = -(MathHelper.cos(limbSwing * 2.0F + ((float)Math.PI * 3F / 2F)) * 0.4F) * limbSwingAmount;
        float f7 = Math.abs(MathHelper.sin(limbSwing + 0.0F) * 0.4F) * limbSwingAmount;
        float f8 = Math.abs(MathHelper.sin(limbSwing + (float)Math.PI) * 0.4F) * limbSwingAmount;
        float f9 = Math.abs(MathHelper.sin(limbSwing + ((float)Math.PI / 2F)) * 0.4F) * limbSwingAmount;
        float f10 = Math.abs(MathHelper.sin(limbSwing + ((float)Math.PI * 3F / 2F)) * 0.4F) * limbSwingAmount;
        data.upperLimbs[0].part.rotation.rotateY(f3);
        data.upperLimbs[1].part.rotation.rotateY(-f3);
        data.upperLimbs[2].part.rotation.rotateY(f4);
        data.upperLimbs[3].part.rotation.rotateY(-f4);
        data.upperLimbs[4].part.rotation.rotateY(f5);
        data.upperLimbs[5].part.rotation.rotateY(-f5);
        data.upperLimbs[6].part.rotation.rotateY(f6);
        data.upperLimbs[7].part.rotation.rotateY(-f6);
        
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
        
        data.upperLimbs[0].part.rotation.rotateZ(f7 + wiggle1);
        data.upperLimbs[1].part.rotation.rotateZ(-f7 + wiggle2);
        data.upperLimbs[2].part.rotation.rotateZ(f8 + wiggle3);
        data.upperLimbs[3].part.rotation.rotateZ(-f8 + wiggle4);
        data.upperLimbs[4].part.rotation.rotateZ(f9 + wiggle1);
        data.upperLimbs[5].part.rotation.rotateZ(-f9 + wiggle2);
        data.upperLimbs[6].part.rotation.rotateZ(f10 + wiggle3);
        data.upperLimbs[7].part.rotation.rotateZ(-f10 + wiggle4);
	}
}
