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
        data.spiderLeg3.rotation.rotateY(20F);
        data.spiderLeg4.rotation.rotateY(-20F);
        data.spiderLeg5.rotation.rotateY(-20F);
        data.spiderLeg6.rotation.rotateY(20F);
        data.spiderLeg7.rotation.rotateY(-65F);
        data.spiderLeg8.rotation.rotateY(65F);
        
        float foreBend = 89 - bodyHeight * 0.5F;
        data.spiderForeLeg1.rotation.orientZ(-foreBend);
        data.spiderForeLeg2.rotation.orientZ(foreBend);
        data.spiderForeLeg3.rotation.orientZ(-foreBend);
        data.spiderForeLeg4.rotation.orientZ(foreBend);
        data.spiderForeLeg5.rotation.orientZ(-foreBend);
        data.spiderForeLeg6.rotation.orientZ(foreBend);
        data.spiderForeLeg7.rotation.orientZ(-foreBend);
        data.spiderForeLeg8.rotation.orientZ(foreBend);
        
        
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
        
        data.spiderLeg1.rotation.rotateY(f3);
        data.spiderLeg2.rotation.rotateY(-f4);
        data.spiderLeg3.rotation.rotateY(f4);
        data.spiderLeg4.rotation.rotateY(-f3);
        data.spiderLeg5.rotation.rotateY(f5);
        data.spiderLeg6.rotation.rotateY(-f6);
        data.spiderLeg7.rotation.rotateY(f6);
        data.spiderLeg8.rotation.rotateY(-f5);
        
        data.spiderLeg1.rotation.localRotateZ(f7);
        data.spiderLeg2.rotation.localRotateZ(-f8);
        data.spiderLeg3.rotation.localRotateZ(f8);
        data.spiderLeg4.rotation.localRotateZ(-f7);
        data.spiderLeg5.rotation.localRotateZ(f9);
        data.spiderLeg6.rotation.localRotateZ(-f10);
        data.spiderLeg7.rotation.localRotateZ(f10);
        data.spiderLeg8.rotation.localRotateZ(-f9);
        
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
}
