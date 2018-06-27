package net.gobbob.mobends.animation.bit.biped;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.client.model.IModelPart;
import net.gobbob.mobends.data.BipedEntityData;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.PlayerData;
import net.gobbob.mobends.pack.BendsPack;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;

public class AttackSlashDownAnimationBit extends AnimationBit
{
	private float ticksPlayed;
	
	@Override
	public String[] getActions(EntityData entityData)
	{
		return new String[] { "attack", "attack_1" };
	}

	@Override
	public void onPlay(EntityData entityData)
	{
		ticksPlayed = 0F;
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

		ItemStack offHandItemStack = living.getHeldItemOffhand();

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

		float attackState = this.ticksPlayed / 10.0F;
		float armSwing = attackState * 3.0F;
		armSwing = Math.min(armSwing, 1.0F);

		Vector3f bodyRot = new Vector3f(0, 0, 0);

		bodyRot.x = 20.0f - attackState * 20.0f;
		bodyRot.y = (-40.0f * attackState + 50 * attackState) * handDirMtp;

		data.body.rotation.slideTo(bodyRot, 0.9F);
		data.head.rotation.setY(data.getHeadYaw());
		data.head.rotation.setX(data.getHeadPitch());
		data.head.preRotation.slideX(-bodyRot.x, 0.9f);
		data.head.preRotation.slideY(-bodyRot.y, 0.9f);

		mainArm.getPreRotation().slideZ(60.0f * handDirMtp, 0.3f);
		mainArm.getRotation().slideX(-20 + armSwing * 100, 3.0f);
		mainArm.getRotation().slideY(0.0f, 0.3f);
		mainArm.getRotation().slideZ(0.0f, 0.9f);
		offArm.getRotation().slideZ(20 * handDirMtp, 0.3f);
		offArm.getPreRotation().slideZ(-80 * handDirMtp, 0.3f);
		offArm.getRotation().slideY(0.0f, 0.3f);

		if (offHandItemStack != null
				&& offHandItemStack.getItem().getItemUseAction(offHandItemStack) == EnumAction.BLOCK)
		{
			offArm.getPreRotation().slideZ(-40 * handDirMtp, 0.3f);
		}

		mainForeArm.getRotation().slideX(-20, 0.3f);
		offForeArm.getRotation().slideX(-60, 0.3f);

		if (data.isStillHorizontally())
		{
			data.rightLeg.rotation.slideX(-30, 0.3f);
			data.leftLeg.rotation.slideX(-30, 0.3f);
			data.leftLeg.rotation.slideY(-25, 0.3f);
			data.rightLeg.rotation.slideZ(10);
			data.leftLeg.rotation.slideZ(-10);

			data.rightForeLeg.rotation.slideX(30, 0.3f);
			data.leftForeLeg.rotation.slideX(30, 0.3f);

			if (!living.isRiding())
			{
				data.renderOffset.slideY(-2.0f);
				data.renderRotation.slideY(-30 * handDirMtp, 0.7F);
				data.head.rotation.addY(-30 * handDirMtp);
			}
		}

		if (mainHandSwitch)
		{
			data.renderRightItemRotation.slideX(90, 2f);
		}
		else
		{
			data.renderLeftItemRotation.slideX(90, 2f);
		}

		ticksPlayed += DataUpdateHandler.ticksPerFrame;
	}
}
