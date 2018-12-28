package net.gobbob.mobends.standard.animation.bit.biped;

import net.gobbob.mobends.core.EntityData;
import net.gobbob.mobends.core.animation.bit.AnimationBit;
import net.gobbob.mobends.core.util.GUtil;
import net.gobbob.mobends.standard.data.BipedEntityData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

public class LadderClimbAnimationBit extends AnimationBit<BipedEntityData<?>>
{
	private static final String[] ACTIONS = new String[] { "ladder_climb" };
	
	@Override
	public String[] getActions(BipedEntityData<?> entityData)
	{
		return ACTIONS;
	}
	
	@Override
	public void perform(BipedEntityData<?> data)
	{
		EntityLivingBase living = data.getEntity();
		
		data.centerRotation.setSmoothness(.3F).orientZero();
		
		float legAnimationOffset = (float)Math.PI;
		float progress = data.getClimbingCycle();
		float armSwingRight = (float) Math.sin(progress)*0.5f+0.5f;
		float armSwingLeft = (float) Math.sin(progress+Math.PI)*0.5f+0.5f;
		float armSwingRight2 = (float) Math.sin(progress-0.3f)*0.5f+0.5f;
		float armSwingLeft2 = (float) Math.sin(progress+Math.PI-0.3f)*0.5f+0.5f;
		float armSwingDouble = (float) Math.sin(progress*2)*0.5f+0.5f;
		float armSwingDouble2 = (float) Math.sin(progress*2-1.8f)*0.5f+0.5f;
		
		float legSwingRight = (float) Math.sin(progress+legAnimationOffset)*0.5f+0.5f;
		float legSwingLeft = (float) Math.sin(progress+legAnimationOffset+Math.PI)*0.5f+0.5f;
		float legSwingRight2 = (float) Math.sin(progress+legAnimationOffset+0.3f)*0.5f+0.5f;
		float legSwingLeft2 = (float) Math.sin(progress+legAnimationOffset+Math.PI+0.3f)*0.5f+0.5f;
		
		float armOrientX = -45F;
		
		float climbingRotation = data.getClimbingRotation();
		float renderRotationY = MathHelper.wrapDegrees(living.rotationYaw-data.getHeadYaw()-climbingRotation);
		data.renderRotation.setSmoothness(.6F).orientY(renderRotationY);
		data.renderOffset.slideZ(armSwingDouble2, .6F);
		
		data.body.rotation.setSmoothness(.5F).orientX(armSwingDouble*10F);
		data.rightArm.rotation.setSmoothness(.5F).orientX(-90F+armOrientX+armSwingRight*70F);
		data.leftArm.rotation.setSmoothness(.5F).orientX(-90.0F+armOrientX+armSwingLeft*70.0f);
		data.rightForeArm.rotation.setSmoothness(.5F).orientX(armSwingRight2*-80.0f);
		data.leftForeArm.rotation.setSmoothness(.5F).orientX(armSwingLeft2*-80.0f);
		
		data.rightLeg.rotation.setSmoothness(.5F).orientX(-45.0f-legSwingRight*50F);
		data.leftLeg.rotation.setSmoothness(.5F).orientX(-45.0f-legSwingLeft*50F);
		data.rightForeLeg.rotation.setSmoothness(.5F).orientX(20.0f+legSwingRight2*90F);
		data.leftForeLeg.rotation.setSmoothness(.5F).orientX(20.0f+legSwingLeft2*90F);
		
		data.head.rotation.orientX(MathHelper.wrapDegrees(data.getHeadPitch()))
		  				  .rotateY(GUtil.clamp(MathHelper.wrapDegrees(data.getHeadYaw() + renderRotationY), -90F, 90F));
		
		float ledgeClimbStart = 0.6F;
		if(data.getLedgeHeight() >= ledgeClimbStart)
		{
        	float armRotX = data.getLedgeHeight()-ledgeClimbStart;
        	data.body.rotation.setSmoothness(.5F).orientX(armRotX*50F);
        	
        	data.rightArm.rotation.setSmoothness(.5F).orientX(-100F+armRotX*40F);
    		data.leftArm.rotation.setSmoothness(.5F).orientX(-100F+armRotX*40F);
    		data.rightForeArm.rotation.setSmoothness(.5F).orientX(-10F);
    		data.leftForeArm.rotation.setSmoothness(.5F).orientX(-10F);
		}
	}
}
