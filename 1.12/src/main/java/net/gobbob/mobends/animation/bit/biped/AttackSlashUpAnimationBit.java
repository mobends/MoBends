package net.gobbob.mobends.animation.bit.biped;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.client.model.IModelPart;
import net.gobbob.mobends.data.BipedEntityData;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.util.GUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;

public class AttackSlashUpAnimationBit extends AnimationBit
{
	@Override
	public String[] getActions(EntityData entityData)
	{
		return new String[] { "attack", "attack_0" };
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
		EnumHandSide primaryHand = living.getPrimaryHand();

		boolean mainHandSwitch = primaryHand == EnumHandSide.RIGHT;
		// Main Hand Direction Multiplier - it helps switch animation sides depending on
		// what is your main hand.
		float handDirMtp = mainHandSwitch ? 1 : -1;
		IModelPart mainArm = mainHandSwitch ? data.rightArm : data.leftArm;
		IModelPart offArm = mainHandSwitch ? data.leftArm : data.rightArm;
		IModelPart mainForeArm = mainHandSwitch ? data.rightForeArm : data.leftForeArm;
		IModelPart offForeArm = mainHandSwitch ? data.leftForeArm : data.rightForeArm;

		if (data.getTicksAfterAttack() < 0.5f)
		{
			data.swordTrail.reset();
		}

		if (living.getHeldItem(EnumHand.MAIN_HAND) != null)
		{
			if (data.getTicksAfterAttack() < 4F
					&& living.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemSword)
			{
				data.swordTrail.add(data);
			}
		}

		float attackState = data.getTicksAfterAttack() / 10F;
		float armSwing = attackState * 3F;
		armSwing = Math.min(armSwing, 1F);

		Vector3f bodyRot = new Vector3f(0, 0, 0);

		bodyRot.x = 20F - armSwing * 20F;
		bodyRot.y = -90F * armSwing * handDirMtp;

		data.body.rotation.slideTo(bodyRot, 0.9f);
		data.head.rotation.setY(data.getHeadYaw());
		data.head.rotation.setX(data.getHeadPitch());
		data.head.preRotation.slideX(-data.body.rotation.getX(), 0.9f);
		data.head.preRotation.slideY(-data.body.rotation.getY(), 0.9f);

		mainArm.getPreRotation().slideZ(60.0f * handDirMtp, 0.3f);

		mainArm.getRotation().slideX(-20 + armSwing * 100, 3.0f);
		mainArm.getRotation().slideX(60.0f - armSwing * 180, 3.0f);
		mainArm.getRotation().slideY(0.0f, 0.9f);
		mainArm.getRotation().slideZ(0.0f, 0.9f);
		offArm.getRotation().slideZ(20 * handDirMtp, 0.3f);
		offArm.getPreRotation().slideZ(-80 * handDirMtp, 0.3f);

		// if (offHandItemStack != null
		// && offHandItemStack.getItem().getItemUseAction(offHandItemStack) ==
		// EnumAction.BLOCK)
		// {
		// offArm.getPreRotation().slideZ(-40 * handDirMtp, 0.3f);
		// }

		mainForeArm.getRotation().slideX(-20, 0.3f);
		offForeArm.getRotation().slideX(-60, 0.3f);

		if (data.isStillHorizontally())
		{
			//data.rightLeg.rotation.slideX(-30, 0.3f);
			//data.leftLeg.rotation.slideX(-30, 0.3f);
			//data.rightLeg.rotation.slideY(0, 0.3f);
			//data.leftLeg.rotation.slideY(-25, 0.3f);
			data.rightLeg.rotation.slideZ(10);

			data.rightForeLeg.rotation.slideX(30, 0.3f);
			//data.leftForeLeg.rotation.slideX(30, 0.3f);

			if (!living.isRiding())
			{
				data.renderOffset.slideY(0.0f);
				//data.renderRotation.slideY(-30 * handDirMtp, 0.7F);
			}
		}
		else
		{
			data.body.rotation.slideY(-70.0F * armSwing * handDirMtp, 0.9f);
		}

		if (mainHandSwitch)
		{
			data.renderRightItemRotation.slideX(180, 0.9f);
		}
		else
		{
			data.renderLeftItemRotation.slideX(180, 0.9f);
		}
	}
}
