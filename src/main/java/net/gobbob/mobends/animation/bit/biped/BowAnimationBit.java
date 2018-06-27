package net.gobbob.mobends.animation.bit.biped;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.client.model.IModelPart;
import net.gobbob.mobends.data.BipedEntityData;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHandSide;

public class BowAnimationBit extends AnimationBit
{
	protected EnumHandSide actionHand = EnumHandSide.RIGHT;
	
	@Override
	public String[] getActions(EntityData entityData)
	{
		return new String[] { "bow" };
	}

	public void setActionHand(EnumHandSide handSide)
	{
		this.actionHand = handSide;
	}
	
	@Override
	public void perform(EntityData entityData)
	{
		if (!(entityData instanceof BipedEntityData))
			return;
		if (!(entityData.getEntity() instanceof EntityLivingBase))
			return;

		BipedEntityData data = (BipedEntityData) entityData;
		EntityLivingBase living = (EntityLivingBase) data.getEntity();

		boolean mainHandSwitch = this.actionHand == EnumHandSide.RIGHT;
		// Main Hand Direction Multiplier - it helps switch animation sides depending on
		// what is your main hand.
		float handDirMtp = mainHandSwitch ? 1 : -1;
		IModelPart mainArm = mainHandSwitch ? data.rightArm : data.leftArm;
		IModelPart offArm = mainHandSwitch ? data.leftArm : data.rightArm;
		IModelPart mainForeArm = mainHandSwitch ? data.rightForeArm : data.leftForeArm;
		IModelPart offForeArm = mainHandSwitch ? data.leftForeArm : data.rightForeArm;

		float aimedBowDuration = 0;

		if (living != null)
		{
			aimedBowDuration = living.getItemInUseMaxCount();
		}

		if (aimedBowDuration > 15)
		{
			aimedBowDuration = 15;
		}

		if (aimedBowDuration < 10)
		{
			mainArm.getPreRotation().slideToZero(0.3F);
			offArm.getPreRotation().slideToZero(0.3F);
			mainArm.getRotation().slideToZero( 0.3F);
			offArm.getRotation().slideToZero(0.3F);

			data.body.rotation.slideX(30, 0.3F);
			data.body.rotation.slideY(0, 0.3F);

			mainArm.getRotation().slideZ(0);
			mainArm.getRotation().slideX(-30);
			offArm.getRotation().slideX(-0 - 30);

			offArm.getRotation().slideY(80F * handDirMtp);

			float var = (aimedBowDuration / 10.0f);
			offForeArm.getRotation().slideX(var * -50F);

			data.head.rotation.slideX(data.getHeadPitch() - 30, 0.3F);
		}
		else
		{
			mainArm.getPreRotation().slideToZero(0.3F);
			offArm.getPreRotation().slideToZero(0.3F);
			mainArm.getRotation().slideToZero(0.3F);
			offArm.getRotation().slideToZero(0.3F);

			float var1 = 20 - (((aimedBowDuration - 10)) / 5.0f) * 20;
			data.body.rotation.slideX(var1, 0.3f);
			float var = (((aimedBowDuration - 10) / 5.0f) * -25) * handDirMtp;
			data.body.rotation.slideY(-var + data.getHeadYaw(), 0.3F);

			if (data.isClimbing())
			{
				data.body.rotation.slideY(var + data.getHeadYaw() * 1.5F, 0.3F);
			}

			mainForeArm.getRotation().slideX(0.0f, 1.0f);

			mainArm.getRotation().slideX(-90 - var1, 0.3f);
			offArm.getRotation().slideX(-0 - 30);

			offArm.getRotation().slideY(80.0f * handDirMtp);

			float var2 = (aimedBowDuration / 10.0f);
			offForeArm.getRotation().slideX(var2 * -30.0f);

			mainArm.getPreRotation().slideY(var);

			float var5 = -90 + data.getHeadPitch();
			var5 = Math.max(var5, -120);
			offArm.getPreRotation().slideX(var5, 0.3F);

			mainArm.getRotation().slideX(data.getHeadPitch() - 90F);

			data.head.rotation.setY(var);
			data.head.preRotation.slideX(-var1, 0.3f);
			data.head.rotation.setX(data.getHeadPitch());

			/*
			 * if(offHandItemStack != null &&
			 * offHandItemStack.getItem().getItemUseAction(offHandItemStack) ==
			 * EnumAction.BLOCK){ if(model.rightArmPose == ArmPose.BOW_AND_ARROW){
			 * model.renderLeftItemRotation.slide(new Vector3f(45.0f,70.0f,40), 0.8f);
			 * }else{ model.renderRightItemRotation.slide(new Vector3f(45.0f,-70.0f,-40),
			 * 0.8f); } }
			 */
		}
	}
}
