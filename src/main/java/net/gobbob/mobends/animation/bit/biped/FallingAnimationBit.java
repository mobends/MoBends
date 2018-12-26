package net.gobbob.mobends.animation.bit.biped;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.data.BipedEntityData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

public class FallingAnimationBit extends AnimationBit<BipedEntityData<?, ?>>
{
	private static final String[] ACTIONS = new String[] { "falling" };
	
	@Override
	public String[] getActions(BipedEntityData<?, ?> entityData)
	{
		return ACTIONS;
	}

	@Override
	public void perform(BipedEntityData<?, ?> data)
	{
		EntityLivingBase living = data.getEntity();
		
		data.head.rotation.orientX(MathHelper.wrapDegrees(data.getHeadPitch()))
		  				  .rotateY(MathHelper.wrapDegrees(data.getHeadYaw()));
		data.body.rotation.orientY(0).setSmoothness(0.5F);
		
		float ticks = DataUpdateHandler.getTicks() * 0.5F;
		float rightArmDelay = 1F;
		
		float armSpan = 20.0F;
		float legSpan = 10.0F;
		
		data.leftArm.rotation.orientZ(-90.0F + MathHelper.sin(ticks) * armSpan).rotateY(MathHelper.cos(ticks) * armSpan).setSmoothness(0.8F);
		data.rightArm.rotation.orientZ(90.0F + MathHelper.sin(ticks + rightArmDelay) * armSpan).rotateY(MathHelper.cos(ticks + rightArmDelay) * armSpan).setSmoothness(0.8F);
		data.leftForeArm.rotation.orientX(-15.0F);
		data.rightForeArm.rotation.orientX(-15.0F);
		data.leftLeg.rotation.orientX(MathHelper.sin(ticks) * legSpan).rotateZ(-20.0F + MathHelper.cos(ticks) * legSpan).setSmoothness(0.8F);
		data.rightLeg.rotation.orientX(MathHelper.sin(ticks + rightArmDelay) * legSpan).rotateZ(20.0F + MathHelper.cos(ticks + rightArmDelay) * legSpan).setSmoothness(0.8F);
		data.leftForeLeg.rotation.orientX(20.0F);
		data.rightForeLeg.rotation.orientX(20.0F);
		data.renderRotation.orientX(20.0F);
		data.head.rotation.rotateX(-20.0F);
	}
}
