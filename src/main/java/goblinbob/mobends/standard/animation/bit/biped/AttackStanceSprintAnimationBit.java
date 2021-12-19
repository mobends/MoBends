package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;

public class AttackStanceSprintAnimationBit extends AnimationBit<BipedEntityData<?>>
{
	@Override
	public void perform(BipedEntityData<?> data)
	{
		data.localOffset.slideToZero(0.3F);

		final EntityLivingBase living = data.getEntity();
		final EnumHandSide primaryHand = living.getPrimaryHand();

		boolean mainHandSwitch = primaryHand == EnumHandSide.RIGHT;
		// Main Hand Direction Multiplier - it helps switch animation sides depending on
		// what is your main hand.
		float handDirMtp = mainHandSwitch ? 1 : -1;
		IModelPart mainArm = mainHandSwitch ? data.rightArm : data.leftArm;
		IModelPart offArm = mainHandSwitch ? data.leftArm : data.rightArm;
		IModelPart mainForeArm = mainHandSwitch ? data.rightForeArm : data.leftForeArm;
		IModelPart offForeArm = mainHandSwitch ? data.leftForeArm : data.rightForeArm;

		if (living.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemSword)
		{
			data.swordTrail.add(data, 0.0F, 0.0F, -10.0F);
		}

		data.body.rotation.rotateY(20 * handDirMtp);
		data.head.rotation.rotateY(-20 * handDirMtp);
		mainArm.getRotation().orientZ(60.0F * handDirMtp);
		mainArm.getRotation().rotateY(60.0F * handDirMtp);
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
