package net.gobbob.mobends.animation.bit.biped;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.client.model.IModelPart;
import net.gobbob.mobends.data.BipedEntityData;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;

public class EatingAnimationBit extends AnimationBit<BipedEntityData>
{
	protected EnumHandSide actionHand = EnumHandSide.RIGHT;
	
	protected float bringUpAnimation;
	
	@Override
	public String[] getActions(BipedEntityData data)
	{
		return new String[] { "eating" };
	}

	public void setActionHand(EnumHandSide handSide)
	{
		this.actionHand = handSide;
	}
	
	@Override
	public void onPlay(BipedEntityData data)
	{
		bringUpAnimation = 0F;
	}
	
	@Override
	public void perform(BipedEntityData data)
	{
		if (!(data.getEntity() instanceof EntityLivingBase))
			return;
		
		float ticks = DataUpdateHandler.getTicks();
		
		boolean mainHandSwitch = this.actionHand == EnumHandSide.RIGHT;
		// Main Hand Direction Multiplier - it helps switch animation sides depending on
		// what is your main hand.
		float handDirMtp = mainHandSwitch ? 1 : -1;
		IModelPart mainArm = mainHandSwitch ? data.rightArm : data.leftArm;
		IModelPart offArm = mainHandSwitch ? data.leftArm : data.rightArm;
		IModelPart mainForeArm = mainHandSwitch ? data.rightForeArm : data.leftForeArm;
		IModelPart offForeArm = mainHandSwitch ? data.leftForeArm : data.rightForeArm;
		
		if (bringUpAnimation < 1F)
		{
			bringUpAnimation += DataUpdateHandler.ticksPerFrame * 0.15F;
			bringUpAnimation = Math.min(bringUpAnimation, 1F);
		}
		else
		{
			//final float PI = (float) Math.PI;
			float wiggle = MathHelper.cos(ticks * 1F);
			data.head.rotation.orientX(wiggle * 5.0F)
					.rotateY(15.0F * handDirMtp);
		}
		
		mainArm.getRotation().orientX(bringUpAnimation * -80.0F)
				.rotateZ(45.0F * bringUpAnimation * handDirMtp);
		mainForeArm.getRotation().orientX(bringUpAnimation * -45.0F);
	}
}
