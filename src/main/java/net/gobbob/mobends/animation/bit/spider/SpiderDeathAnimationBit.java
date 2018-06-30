package net.gobbob.mobends.animation.bit.spider;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.SpiderData;
import net.minecraft.util.math.MathHelper;

public class SpiderDeathAnimationBit extends AnimationBit
{
	@Override
	public String[] getActions(EntityData entityData)
	{
		return new String[] { "death" };
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
		
		data.spiderHead.rotation.setY(headYaw * 0.017453292F);
		data.spiderHead.rotation.setX(headPitch * 0.017453292F);
		
        data.spiderLeg1.rotation.setZ(-45F);
        data.spiderLeg2.rotation.setZ(45F);
        data.spiderLeg3.rotation.setZ(-33.3F);
        data.spiderLeg4.rotation.setZ(33.3F);
        data.spiderLeg5.rotation.setZ(-33.3F);
        data.spiderLeg6.rotation.setZ(33.3F);
        data.spiderLeg7.rotation.setZ(-45F);
        data.spiderLeg8.rotation.setZ(45F);
        
        data.spiderLeg1.rotation.setY(45F);
        data.spiderLeg2.rotation.setY(-45F);
        data.spiderLeg3.rotation.setY(22.5F);
        data.spiderLeg4.rotation.setY(-22.5F);
        data.spiderLeg5.rotation.setY(-22.5F);
        data.spiderLeg6.rotation.setY(22.5F);
        data.spiderLeg7.rotation.setY(-45F);
        data.spiderLeg8.rotation.setY(45F);
        
        float foreBend = 89;
        data.spiderForeLeg1.rotation.setZ(-foreBend);
        data.spiderForeLeg2.rotation.setZ(foreBend);
        data.spiderForeLeg3.rotation.setZ(-foreBend);
        data.spiderForeLeg4.rotation.setZ(foreBend);
        data.spiderForeLeg5.rotation.setZ(-foreBend);
        data.spiderForeLeg6.rotation.setZ(foreBend);
        data.spiderForeLeg7.rotation.setZ(-foreBend);
        data.spiderForeLeg8.rotation.setZ(foreBend);
        
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
        data.spiderLeg1.rotation.addY(f3);
        data.spiderLeg2.rotation.addY(-f3);
        data.spiderLeg3.rotation.addY(f4);
        data.spiderLeg4.rotation.addY(-f4);
        data.spiderLeg5.rotation.addY(f5);
        data.spiderLeg6.rotation.addY(-f5);
        data.spiderLeg7.rotation.addY(f6);
        data.spiderLeg8.rotation.addY(-f6);
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
