package net.gobbob.mobends.standard.animation.bit.biped;

import net.gobbob.mobends.core.animation.bit.AnimationBit;
import net.gobbob.mobends.core.client.model.IModelPart;
import net.gobbob.mobends.core.data.EntityData;
import net.gobbob.mobends.standard.data.BipedEntityData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;

public class AttackStanceSprintAnimationBit extends AnimationBit<BipedEntityData<?>>
{
	private static final String[] ACTIONS = new String[] { "attack_stance_sprint" };
	
	@Override
	public String[] getActions(BipedEntityData<?> entityData)
	{
		return ACTIONS;
	}

	@Override
	public void perform(BipedEntityData<?> data)
	{
		EntityLivingBase living = data.getEntity();
		EnumHandSide primaryHand = living.getPrimaryHand();

		boolean mainHandSwitch = primaryHand == EnumHandSide.RIGHT;
		// Main Hand Direction Multiplier - it helps switch animation sides depending on
		// what is your main hand.
		float handDirMtp = mainHandSwitch ? 1 : -1;
		IModelPart mainArm = mainHandSwitch ? data.rightArm : data.leftArm;
		IModelPart offArm = mainHandSwitch ? data.leftArm : data.rightArm;
		IModelPart mainForeArm = mainHandSwitch ? data.rightForeArm : data.leftForeArm;
		IModelPart offForeArm = mainHandSwitch ? data.leftForeArm : data.rightForeArm;

		if (living.getHeldItem(EnumHand.MAIN_HAND) != null)
		{
			if (living.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemSword)
			{
				data.swordTrail.add(data, 0.0F, 0.0F, -10.0F);
			}
		}
		
		data.body.rotation.rotateY(20 * handDirMtp);
		data.head.rotation.rotateY(-20 * handDirMtp);
		mainArm.getRotation().orientZ(60.0F * handDirMtp);
		mainArm.getRotation().rotateY(60.0F);
		offArm.getRotation().rotateZ(-30.0F * handDirMtp);
		
		if (mainHandSwitch)
		{
			data.renderRightItemRotation.setSmoothness(.3F).orientX(45);
		}
		else
		{
			data.renderLeftItemRotation.setSmoothness(.3F).orientX(45);
		}
	}
}
