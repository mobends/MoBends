package net.gobbob.mobends.animation.bit.biped;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.data.BipedEntityData;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.util.GUtil;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBiped.ArmPose;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;

public class SwimmingAnimationBit extends AnimationBit<BipedEntityData>
{
	private static final float PI = (float) Math.PI;
	private static final float PI_2 = PI*2;
	
	private float transformTransition = 0F;
	private float transitionSpeed = 0.1F;
	
	@Override
	public String[] getActions(BipedEntityData entityData)
	{
		return new String[] { "swimming" };
	}
	
	@Override
	public void onPlay(BipedEntityData entityData)
	{
		transformTransition = 0F;
		transitionSpeed = 0.1F;
	}
	
	@Override
	public void perform(BipedEntityData data)
	{
		if (!(data.getEntity() instanceof EntityLivingBase))
			return;
		EntityLivingBase living = (EntityLivingBase) data.getEntity();
		
		float ticks = DataUpdateHandler.getTicks() + DataUpdateHandler.partialTicks;
		
		float armSway = (MathHelper.cos(ticks * .1625F)+1F)/2.0f;
		float armSway2 = (-MathHelper.sin(ticks * .1625F)+1F)/2.0f;
		float legFlap = MathHelper.cos(ticks * .4625F);
		float foreArmSway = ((ticks * .1625F) % PI_2)/PI_2;
		float foreArmStretch = armSway * 2F;
		foreArmStretch -= 1F;
		foreArmStretch = Math.max(foreArmStretch, 0);
		
		EnumHandSide primaryHand = living.getPrimaryHand();
		EnumHandSide offHand = primaryHand == EnumHandSide.RIGHT ? EnumHandSide.LEFT : EnumHandSide.RIGHT;
		ItemStack itemstack = living.getHeldItemMainhand();
        ItemStack itemstack1 = living.getHeldItemOffhand();
        ModelBiped.ArmPose armPoseMain = ModelBiped.ArmPose.EMPTY;
        ModelBiped.ArmPose armPoseOff = ModelBiped.ArmPose.EMPTY;

        boolean drawingBow = false;
        if (living.getItemInUseCount() > 0)
        {
	        if ((!itemstack.isEmpty() && itemstack.getItemUseAction() == EnumAction.BOW) ||
	        	(!itemstack1.isEmpty() && itemstack1.getItemUseAction() == EnumAction.BOW))
	        {
	        	drawingBow = true;
	        }
		}
		
        float t = (float) GUtil.easeInOut(this.transformTransition, 3F);
        
		if(data.isStillHorizontally() || drawingBow || data.getTicksAfterAttack() < 10)
		{
			if (this.transformTransition > 0F)
			{
				this.transformTransition -= DataUpdateHandler.ticksPerFrame * this.transitionSpeed;
				this.transformTransition = Math.max(0F, this.transformTransition);
			}
			
			armSway = (MathHelper.cos(ticks * .0825F) + 1) / 2;
			armSway2 = (-MathHelper.sin(ticks * .0825F) + 1) / 2;
			legFlap = MathHelper.cos(ticks * .2625F);
			
			data.leftArm.rotation.setSmoothness(.3F).orientX(armSway2*30-15).rotateZ(-armSway*30);
			data.rightArm.rotation.setSmoothness(.3F).orientX(armSway2*30-15).rotateZ(armSway*30);
			data.leftForeArm.rotation.setSmoothness(.3F).orientX(armSway2*-40);
			data.rightForeArm.rotation.setSmoothness(.3F).orientX(armSway2*-40);
			data.leftLeg.rotation.setSmoothness(.3F).orientX(legFlap*40);
			data.rightLeg.rotation.setSmoothness(.3F).orientX(-legFlap*40);
			data.leftForeLeg.rotation.setSmoothness(.4F).orientX(5);
			data.rightForeLeg.rotation.setSmoothness(.4F).orientX(5);
			data.body.rotation.orientX(armSway*10);
		}
		else
		{
			if (this.transformTransition < 1F)
			{
				this.transformTransition += DataUpdateHandler.ticksPerFrame * this.transitionSpeed;
				this.transformTransition = Math.min(this.transformTransition, 1F);
			}
			
			data.leftArm.rotation.setSmoothness(.3F).orientX(armSway*-120)
					.rotateY(-90F * t)
					.rotateX(armSway * 20);
			data.rightArm.rotation.setSmoothness(.3F).orientX(armSway*-120)
					.rotateY(90F * t)
					.rotateX(armSway * 20);
			
			data.leftForeArm.rotation.setSmoothness(.3F).orientX((foreArmSway < 0.55f | foreArmSway > 0.9) ? foreArmStretch*-60.0f : -60);
			data.rightForeArm.rotation.setSmoothness(.3F).orientX((foreArmSway < 0.55f | foreArmSway > 0.9) ? foreArmStretch*-60.0f : -60);
			
			data.leftLeg.rotation.setSmoothness(.3F).orientX(legFlap*40);
			data.rightLeg.rotation.setSmoothness(.3F).orientX(-legFlap*40);
			
			data.leftForeLeg.rotation.setSmoothness(.4F).orientX(5);
			data.rightForeLeg.rotation.setSmoothness(.4F).orientX(5);
			
			data.body.rotation.setSmoothness(.5F).orientX(armSway*-20);
			
			data.renderRightItemRotation.setSmoothness(.3F).orientX(armSway*50);
		}
		
		data.head.rotation.setSmoothness(1.0F).orientX(MathHelper.wrapDegrees(data.getHeadPitch()))
		  				  .rotateY(MathHelper.wrapDegrees(data.getHeadYaw()))
		  				  .rotateX(-80F * t);
		
		data.renderRotation.setSmoothness(.7F).orientX(t * 80F);
		data.renderOffset.slideZ(-20 * t, .7F);
		data.renderOffset.slideY(14 * t, .7F);
	}
}
