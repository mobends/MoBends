package net.gobbob.mobends.standard.animation.bit.biped;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.core.EntityData;
import net.gobbob.mobends.core.animation.bit.AnimationBit;
import net.gobbob.mobends.core.client.model.IModelPart;
import net.gobbob.mobends.core.pack.BendsPack;
import net.gobbob.mobends.core.util.GUtil;
import net.gobbob.mobends.core.util.SmoothOrientation;
import net.gobbob.mobends.standard.data.BipedEntityData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;

public class AttackSlashUpAnimationBit extends AnimationBit<BipedEntityData<?>>
{
	private static final String[] ACTIONS = new String[] { "attack", "attack_0" };
	
	@Override
	public String[] getActions(BipedEntityData<?> entityData)
	{
		return ACTIONS;
	}
	
	@Override
	public void onPlay(BipedEntityData<?> data)
	{
		data.swordTrail.reset();
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
		IModelPart mainLeg = mainHandSwitch ? data.rightLeg : data.leftLeg;
		IModelPart offLeg = mainHandSwitch ? data.leftLeg : data.rightLeg;
		IModelPart mainForeLeg = mainHandSwitch ? data.rightForeLeg : data.leftForeLeg;
		IModelPart offForeLeg = mainHandSwitch ? data.leftForeLeg : data.rightForeLeg;
		SmoothOrientation mainItemRotation = mainHandSwitch ? data.renderRightItemRotation : data.renderLeftItemRotation;
		
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
		bodyRot.y = -70F * armSwing * handDirMtp;

		data.body.rotation.setSmoothness(.9F).orientX(bodyRot.x)
				.orientY(bodyRot.y);
		data.head.rotation.orientX(MathHelper.wrapDegrees(data.getHeadPitch()) - bodyRot.x)
						  .rotateY(MathHelper.wrapDegrees(data.getHeadYaw()) - bodyRot.y);

		mainArm.getRotation().setSmoothness(.9F).orientZ(110F * armSwing * handDirMtp)
				.rotateY(60F - armSwing * 180F);
		
		offArm.getRotation().setSmoothness(.3F).orientZ(-20 * handDirMtp);

		mainForeArm.getRotation().setSmoothness(.3F).orientX(-20);
		offForeArm.getRotation().setSmoothness(.3F).orientX(-60);

		if (data.isStillHorizontally() && !living.isRiding())
		{
			data.rightLeg.rotation.orientZ(5)
					.rotateY(15F)
					.rotateX(-20F);
			data.leftLeg.rotation.orientZ(-5)
					.rotateY(-15F)
					.rotateX(-20F);
			data.rightForeLeg.rotation.orientX(25F);
			
			data.renderRotation.setSmoothness(.3F).orientY(0 * handDirMtp);
			data.renderOffset.slideY(-1.0F);
		}

		mainItemRotation.setSmoothness(.9F).orientInstantX(180);
	}
}