package net.gobbob.mobends.animation.bit.biped;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.core.EntityData;
import net.gobbob.mobends.core.animation.bit.AnimationBit;
import net.gobbob.mobends.core.client.event.DataUpdateHandler;
import net.gobbob.mobends.core.client.model.IModelPart;
import net.gobbob.mobends.core.pack.BendsPack;
import net.gobbob.mobends.core.util.GUtil;
import net.gobbob.mobends.core.util.SmoothOrientation;
import net.gobbob.mobends.data.BipedEntityData;
import net.gobbob.mobends.data.PlayerData;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;

public class AttackSlashDownAnimationBit extends AnimationBit<BipedEntityData<?, ?>>
{
	private static final String[] ACTIONS = new String[] { "attack", "attack_1" };
	
	private float ticksPlayed;
	
	@Override
	public String[] getActions(BipedEntityData entityData)
	{
		return ACTIONS;
	}

	@Override
	public void onPlay(BipedEntityData data)
	{
		data.swordTrail.reset();
		
		this.ticksPlayed = 0F;
	}

	@Override
	public void perform(BipedEntityData data)
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
		SmoothOrientation mainItemRotation = mainHandSwitch ? data.renderRightItemRotation : data.renderLeftItemRotation;
		ItemStack offHandItemStack = living.getHeldItemOffhand();

		if (living.getHeldItem(EnumHand.MAIN_HAND) != null)
		{
			if (data.getTicksAfterAttack() < 4F &&
				living.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemSword)
			{
				data.swordTrail.add(data);
			}
		}

		float attackState = this.ticksPlayed / 10F;
		float armSwing = GUtil.clamp(attackState * 3F, 0F, 1F);

		float bodyRotationX = 20F - attackState * 20F;
		float bodyRotationY = (30F + 10F * attackState) * handDirMtp;

		data.body.rotation.setSmoothness(.9F).orientX(bodyRotationX)
											 .orientY(bodyRotationY);
		data.head.rotation.orientX(MathHelper.wrapDegrees(data.getHeadPitch()) - bodyRotationX)
						  .rotateY(MathHelper.wrapDegrees(data.getHeadYaw()) - bodyRotationY);
		
		mainArm.getRotation().setSmoothness(.3F).orientZ(60F * handDirMtp)
												.rotateInstantY(-20F + armSwing * 70F);
		offArm.getRotation().setSmoothness(.3F).orientZ(-80 * handDirMtp);

		mainForeArm.getRotation().setSmoothness(.3F).orientX(-20F);
		offForeArm.getRotation().setSmoothness(.3F).orientX(-60F);

		if (data.isStillHorizontally() && !living.isRiding())
		{
			data.rightLeg.rotation.setSmoothness(.3F).orientX(-30F)
					.rotateZ(10)
					.rotateY(25);
			data.leftLeg.rotation.setSmoothness(.3F).orientX(-30F)
					.rotateZ(-10)
					.rotateY(-25);
			
			data.rightForeLeg.rotation.setSmoothness(.3F).orientX(30F);
			data.leftForeLeg.rotation.setSmoothness(.3F).orientX(30F);
			
			data.head.rotation.rotateY(-30 * handDirMtp);
			data.renderOffset.slideY(-2F);
			data.renderRotation.setSmoothness(.3F).orientY(-30 * handDirMtp);
		}

		mainItemRotation.orientInstantX(90);

		ticksPlayed += DataUpdateHandler.ticksPerFrame;
	}
}
