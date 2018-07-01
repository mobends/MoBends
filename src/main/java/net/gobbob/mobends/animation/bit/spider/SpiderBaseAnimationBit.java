package net.gobbob.mobends.animation.bit.spider;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.client.event.EntityRenderHandler;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.SpiderData;
import net.minecraft.util.math.MathHelper;

public class SpiderBaseAnimationBit extends AnimationBit
{
	protected final float kneelDuration = 0.15F;
	
	@Override
	public String[] getActions(EntityData entityData)
	{
		return new String[] {};
	}
	
	@Override
	public void onPlay(EntityData entityData)
	{
		if (!(entityData instanceof SpiderData))
			return;
		SpiderData data = (SpiderData) entityData;
		
		float legBend = 33.3F;
        data.spiderLeg1.rotation.orientInstantZ(legBend);
        data.spiderLeg2.rotation.orientInstantZ(-legBend);
        data.spiderLeg3.rotation.orientInstantZ(legBend);
        data.spiderLeg4.rotation.orientInstantZ(-legBend);
        data.spiderLeg5.rotation.orientInstantZ(legBend);
        data.spiderLeg6.rotation.orientInstantZ(-legBend);
        data.spiderLeg7.rotation.orientInstantZ(legBend);
        data.spiderLeg8.rotation.orientInstantZ(-legBend);
        
        data.spiderLeg1.rotation.rotateY(65F);
        data.spiderLeg2.rotation.rotateY(-65F);
        data.spiderLeg3.rotation.rotateY(40F);
        data.spiderLeg4.rotation.rotateY(-40F);
        data.spiderLeg5.rotation.rotateY(-40F);
        data.spiderLeg6.rotation.rotateY(40F);
        data.spiderLeg7.rotation.rotateY(-65F);
        data.spiderLeg8.rotation.rotateY(65F);
        
        data.spiderLeg1.rotation.rotateX(0.0F);
        data.spiderLeg2.rotation.rotateX(0.0F);
        data.spiderLeg3.rotation.rotateX(0.0F);
        data.spiderLeg4.rotation.rotateX(0.0F);
        data.spiderLeg5.rotation.rotateX(0.0F);
        data.spiderLeg6.rotation.rotateX(0.0F);
        data.spiderLeg7.rotation.rotateX(0.0F);
        data.spiderLeg8.rotation.rotateX(0.0F);
        
        float foreBend = 89;
        data.spiderForeLeg1.rotation.orientInstantZ(-foreBend);
        data.spiderForeLeg2.rotation.orientInstantZ(foreBend);
        data.spiderForeLeg3.rotation.orientInstantZ(-foreBend);
        data.spiderForeLeg4.rotation.orientInstantZ(foreBend);
        data.spiderForeLeg5.rotation.orientInstantZ(-foreBend);
        data.spiderForeLeg6.rotation.orientInstantZ(foreBend);
        data.spiderForeLeg7.rotation.orientInstantZ(-foreBend);
        data.spiderForeLeg8.rotation.orientInstantZ(foreBend);
	}
	
	@Override
	public void perform(EntityData entityData)
	{
		if (!(entityData instanceof SpiderData))
			return;
		SpiderData data = (SpiderData) entityData;
		
		float headYaw = data.getHeadYaw();
		float headPitch = data.getHeadPitch();
		float ticks = entityData.getEntity().ticksExisted + EntityRenderHandler.partialTicks;
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
        float legBend = 33.3F - bodyHeight;
        data.spiderLeg1.rotation.orientZ(legBend);
        data.spiderLeg2.rotation.orientZ(-legBend);
        data.spiderLeg3.rotation.orientZ(legBend);
        data.spiderLeg4.rotation.orientZ(-legBend);
        data.spiderLeg5.rotation.orientZ(legBend);
        data.spiderLeg6.rotation.orientZ(-legBend);
        data.spiderLeg7.rotation.orientZ(legBend);
        data.spiderLeg8.rotation.orientZ(-legBend);
        
        data.spiderLeg1.rotation.rotateY(65F);
        data.spiderLeg2.rotation.rotateY(-65F);
        data.spiderLeg3.rotation.rotateY(40F);
        data.spiderLeg4.rotation.rotateY(-40F);
        data.spiderLeg5.rotation.rotateY(-40F);
        data.spiderLeg6.rotation.rotateY(40F);
        data.spiderLeg7.rotation.rotateY(-65F);
        data.spiderLeg8.rotation.rotateY(65F);
        
        data.spiderLeg1.rotation.rotateX(0.0F);
        data.spiderLeg2.rotation.rotateX(0.0F);
        data.spiderLeg3.rotation.rotateX(0.0F);
        data.spiderLeg4.rotation.rotateX(0.0F);
        data.spiderLeg5.rotation.rotateX(0.0F);
        data.spiderLeg6.rotation.rotateX(0.0F);
        data.spiderLeg7.rotation.rotateX(0.0F);
        data.spiderLeg8.rotation.rotateX(0.0F);
        
        float foreBend = 89 - bodyHeight * 0.5F;
        data.spiderForeLeg1.rotation.orientZ(-foreBend);
        data.spiderForeLeg2.rotation.orientZ(foreBend);
        data.spiderForeLeg3.rotation.orientZ(-foreBend);
        data.spiderForeLeg4.rotation.orientZ(foreBend);
        data.spiderForeLeg5.rotation.orientZ(-foreBend);
        data.spiderForeLeg6.rotation.orientZ(foreBend);
        data.spiderForeLeg7.rotation.orientZ(-foreBend);
        data.spiderForeLeg8.rotation.orientZ(foreBend);
        
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
        
        data.spiderLeg1.rotation.rotateZ(f7);
        data.spiderLeg2.rotation.rotateZ(-f7);
        data.spiderLeg3.rotation.rotateZ(f8);
        data.spiderLeg4.rotation.rotateZ(-f8);
        data.spiderLeg5.rotation.rotateZ(f9);
        data.spiderLeg6.rotation.rotateZ(-f9);
        data.spiderLeg7.rotation.rotateZ(f10);
        data.spiderLeg8.rotation.rotateZ(-f10);
	}
}
