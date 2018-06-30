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
        data.spiderLeg1.rotation.setZ(legBend);
        data.spiderLeg2.rotation.setZ(-legBend);
        data.spiderLeg3.rotation.setZ(legBend);
        data.spiderLeg4.rotation.setZ(-legBend);
        data.spiderLeg5.rotation.setZ(legBend);
        data.spiderLeg6.rotation.setZ(-legBend);
        data.spiderLeg7.rotation.setZ(legBend);
        data.spiderLeg8.rotation.setZ(-legBend);
        
        data.spiderLeg1.preRotation.setY(65F);
        data.spiderLeg2.preRotation.setY(-65F);
        data.spiderLeg3.preRotation.setY(40F);
        data.spiderLeg4.preRotation.setY(-40F);
        data.spiderLeg5.preRotation.setY(-40F);
        data.spiderLeg6.preRotation.setY(40F);
        data.spiderLeg7.preRotation.setY(-65F);
        data.spiderLeg8.preRotation.setY(65F);
        
        data.spiderLeg1.rotation.setX(0.0F);
        data.spiderLeg2.rotation.setX(0.0F);
        data.spiderLeg3.rotation.setX(0.0F);
        data.spiderLeg4.rotation.setX(0.0F);
        data.spiderLeg5.rotation.setX(0.0F);
        data.spiderLeg6.rotation.setX(0.0F);
        data.spiderLeg7.rotation.setX(0.0F);
        data.spiderLeg8.rotation.setX(0.0F);
        
        float foreBend = 89;
        data.spiderForeLeg1.rotation.setZ(-foreBend);
        data.spiderForeLeg2.rotation.setZ(foreBend);
        data.spiderForeLeg3.rotation.setZ(-foreBend);
        data.spiderForeLeg4.rotation.setZ(foreBend);
        data.spiderForeLeg5.rotation.setZ(-foreBend);
        data.spiderForeLeg6.rotation.setZ(foreBend);
        data.spiderForeLeg7.rotation.setZ(-foreBend);
        data.spiderForeLeg8.rotation.setZ(foreBend);
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
		
		data.spiderHead.rotation.setY(headYaw);
		data.spiderHead.rotation.setX(headPitch);
        float legBend = 33.3F - bodyHeight;
        data.spiderLeg1.rotation.slideZ(legBend);
        data.spiderLeg2.rotation.slideZ(-legBend);
        data.spiderLeg3.rotation.slideZ(legBend);
        data.spiderLeg4.rotation.slideZ(-legBend);
        data.spiderLeg5.rotation.slideZ(legBend);
        data.spiderLeg6.rotation.slideZ(-legBend);
        data.spiderLeg7.rotation.slideZ(legBend);
        data.spiderLeg8.rotation.slideZ(-legBend);
        
        data.spiderLeg1.preRotation.setY(65F);
        data.spiderLeg2.preRotation.setY(-65F);
        data.spiderLeg3.preRotation.setY(40F);
        data.spiderLeg4.preRotation.setY(-40F);
        data.spiderLeg5.preRotation.setY(-40F);
        data.spiderLeg6.preRotation.setY(40F);
        data.spiderLeg7.preRotation.setY(-65F);
        data.spiderLeg8.preRotation.setY(65F);
        
        data.spiderLeg1.rotation.setX(0.0F);
        data.spiderLeg2.rotation.setX(0.0F);
        data.spiderLeg3.rotation.setX(0.0F);
        data.spiderLeg4.rotation.setX(0.0F);
        data.spiderLeg5.rotation.setX(0.0F);
        data.spiderLeg6.rotation.setX(0.0F);
        data.spiderLeg7.rotation.setX(0.0F);
        data.spiderLeg8.rotation.setX(0.0F);
        
        float foreBend = 89 - bodyHeight * 0.5F;
        data.spiderForeLeg1.rotation.slideZ(-foreBend);
        data.spiderForeLeg2.rotation.slideZ(foreBend);
        data.spiderForeLeg3.rotation.slideZ(-foreBend);
        data.spiderForeLeg4.rotation.slideZ(foreBend);
        data.spiderForeLeg5.rotation.slideZ(-foreBend);
        data.spiderForeLeg6.rotation.slideZ(foreBend);
        data.spiderForeLeg7.rotation.slideZ(-foreBend);
        data.spiderForeLeg8.rotation.slideZ(foreBend);
        
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
        data.spiderLeg1.preRotation.addY(f3);
        data.spiderLeg2.preRotation.addY(-f3);
        data.spiderLeg3.preRotation.addY(f4);
        data.spiderLeg4.preRotation.addY(-f4);
        data.spiderLeg5.preRotation.addY(f5);
        data.spiderLeg6.preRotation.addY(-f5);
        data.spiderLeg7.preRotation.addY(f6);
        data.spiderLeg8.preRotation.addY(-f6);
        data.spiderLeg1.rotation.addZ(f7);
        data.spiderLeg2.rotation.addZ(-f7);
        data.spiderLeg3.rotation.addZ(f8);
        data.spiderLeg4.rotation.addZ(-f8);
        data.spiderLeg5.rotation.addZ(f9);
        data.spiderLeg6.rotation.addZ(-f9);
        data.spiderLeg7.rotation.addZ(f10);
        data.spiderLeg8.rotation.addZ(-f10);
	}
}
