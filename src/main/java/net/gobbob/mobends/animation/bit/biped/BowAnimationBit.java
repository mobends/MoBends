package net.gobbob.mobends.animation.bit.biped;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.client.model.IModelPart;
import net.gobbob.mobends.data.BipedEntityData;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;

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
			data.body.getRotation().setSmoothness(.3F).orientX(30);

			mainArm.getRotation().setSmoothness(.3F).orientX(-30);
			offArm.getRotation().setSmoothness(.3F).orientX(-30)
					.rotateY(80F * handDirMtp);

			float var = (aimedBowDuration / 10.0f);
			offForeArm.getRotation().orientX(var * -50F);

			data.head.rotation.setSmoothness(.3F).rotateX(-30);
		}
		else
		{
			float var = (((aimedBowDuration - 10) / 5.0f) * -25) * handDirMtp;
			float var2 = (aimedBowDuration / 10.0f);
			float var5 = data.getHeadPitch() - 90F;
			var5 = Math.max(var5, -160);
			
			float bodyRotationX = 20 - (((aimedBowDuration - 10)) / 5.0f) * 20;
			float bodyRotationY = -var + data.getHeadYaw();
			if (data.isClimbing())
			{
				bodyRotationY = var + data.getHeadYaw() * 1.5F;
			}
			
			data.body.rotation.setSmoothness(.3F).orientX(bodyRotationX)
			.rotateY(bodyRotationY);

			mainArm.getRotation().setSmoothness(.8F).orientX(data.getHeadPitch() - 90F)
					.rotateY(var);
			offArm.getRotation().setSmoothness(1F).orientY(80F * handDirMtp)
				.rotateZ(-MathHelper.cos(data.getHeadPitch()/180F*3.14F)*40.0F + 40.0F) //Keeping it close to the arm no matter the head pitch
				.rotateX(var5);
					//

			mainForeArm.getRotation().setSmoothness(1F).orientX(0);
			offForeArm.getRotation().orientX(var2 * -30F);
			
			data.head.rotation.setSmoothness(0.5F).orientX(MathHelper.wrapDegrees(data.getHeadPitch()) - bodyRotationX)
												  .rotateY(MathHelper.wrapDegrees(data.getHeadYaw()) - bodyRotationY);
		}
	}
}
