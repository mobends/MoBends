package net.gobbob.mobends.animation.bit.biped;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.client.model.IModelPart;
import net.gobbob.mobends.data.BipedEntityData;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.util.GUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;

public class AttackWhirlSlashAnimationBit extends AnimationBit
{
	@Override
	public String[] getActions(EntityData entityData)
	{
		return new String[] { "attack", "attack_2" };
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
			if (living.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemSword)
			{
				data.swordTrail.add(data);
			}
		}

		float attackState = data.getTicksAfterAttack() / 10.0f;
		float armSwing = attackState * 2.0f;
		armSwing = Math.min(armSwing, 1F);

		float var5 = attackState * 1.6F;
		var5 = Math.min(var5, 1F);

		float var = 50 + 360 * var5;
		float var2 = 50 + 360 * var5;

		while (var2 > 360.0f)
		{
			var2 -= 360.0f;
		}

		/*if (var > 360.0f)
		{
			data.renderRotation.setY(-var2 * handDirMtp);
		}
		else
		{
			data.renderRotation.slideY(-var * handDirMtp, 0.7F);
		}

		Vector3f bodyRot = new Vector3f(0, 0, 0);

		bodyRot.x = 20.0f - attackState * 20.0F;
		bodyRot.y = -40.0f * attackState * handDirMtp;

		data.body.rotation.slideTo(bodyRot, 0.9F);
		data.head.rotation.setY(data.getHeadYaw() - 30 * handDirMtp);
		data.head.rotation.setX(data.getHeadPitch());
		data.head.preRotation.slideX(-bodyRot.x, 0.3F);
		data.head.preRotation.slideY(-bodyRot.y, 0.3F);

		data.rightLeg.rotation.slideX(-30, 0.3f);
		data.leftLeg.rotation.slideX(-30, 0.3f);
		data.rightLeg.rotation.slideZ(10);
		data.leftLeg.rotation.slideZ(-10);

		data.rightForeLeg.rotation.slideX(30, 0.3f);
		data.leftForeLeg.rotation.slideX(30, 0.3f);

		mainArm.getPreRotation().slideZ(-(-60.0f - var5 * 80) * handDirMtp, 0.3f);
		mainArm.getRotation().slideX(-20 + armSwing * 70, 3.0f);
		mainArm.getRotation().slideY(0.0f, 0.3f);
		mainArm.getRotation().slideZ(0.0f, 0.9f);

		offArm.getRotation().slideZ(20 * handDirMtp, 0.3f);
		offArm.getPreRotation().slideZ(-80 * handDirMtp, 0.3f);

		mainForeArm.getRotation().slideX(-20, 0.3f);
		offForeArm.getRotation().slideX(-60, 0.3f);

		if (mainHandSwitch)
		{
			data.renderRightItemRotation.slideX(90 * attackState, 0.9f);
		}
		else
		{
			data.renderLeftItemRotation.slideX(90 * attackState, 0.9f);
		}

		float var61 = data.getTicksAfterAttack() * 5;
		float var62 = data.getTicksAfterAttack() * 5;

		var61 = (MathHelper.cos(var61 * 0.0625f) + 1.0f) / 2.0f * 20;
		var62 = (MathHelper.cos(var62 * 0.0625f) + 1.0f) / 2.0f * 20;

		data.rightLeg.rotation.slideY(0, 0.9f);
		data.leftLeg.rotation.slideY(-25, 0.9f);

		data.rightLeg.rotation.slideZ(var61);
		data.leftLeg.rotation.slideZ(-var61);
		data.renderOffset.slideY(-2F);*/
	}
}
