package net.gobbob.mobends.animation.bit.player;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.animation.layer.AnimationLayer;
import net.gobbob.mobends.client.model.IModelPart;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.PlayerData;
import net.gobbob.mobends.pack.BendsPack;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.EnumHandSide;

public class AttackStanceAnimationBit extends AnimationBit
{
	public AttackStanceAnimationBit()
	{
	}

	@Override
	public void perform(EntityData entityData)
	{
		if (!(entityData instanceof PlayerData))
			return;
		if (!(entityData.getEntity() instanceof AbstractClientPlayer))
			return;

		PlayerData data = (PlayerData) entityData;
		AbstractClientPlayer player = (AbstractClientPlayer) data.getEntity();
		EnumHandSide primaryHand = player.getPrimaryHand();

		boolean mainHandSwitch = primaryHand == EnumHandSide.RIGHT;
		// Main Hand Direction Multiplier - it helps switch animation sides depending on
		// what is your main hand.
		float handDirMtp = mainHandSwitch ? 1 : -1;
		IModelPart mainArm = mainHandSwitch ? data.rightArm : data.leftArm;
		IModelPart offArm = mainHandSwitch ? data.leftArm : data.rightArm;
		IModelPart mainForeArm = mainHandSwitch ? data.rightForeArm : data.leftForeArm;
		IModelPart offForeArm = mainHandSwitch ? data.leftForeArm : data.rightForeArm;

		// ItemStack offHandItemStack = player.getHeldItemOffhand();

		if (!data.isOnGround())
		{
			return;
		}

		if (data.isStillHorizontally())
		{
			data.renderRotation.slideY(-30 * handDirMtp, 0.3F);

			Vector3f bodyRot = new Vector3f(0, 0, 0);

			bodyRot.x = 20.0F;

			data.body.rotation.slideTo(bodyRot, 0.3F);
			data.head.rotation.setY(data.getHeadYaw() - 30 * handDirMtp);
			data.head.rotation.setX(data.getHeadPitch());
			data.head.preRotation.slideX(-bodyRot.x, 0.3F);
			data.head.preRotation.slideY(-bodyRot.y, 0.3F);

			data.rightLeg.rotation.slideX(-30, 0.3F);
			data.leftLeg.rotation.slideX(-30, 0.3F);
			data.leftLeg.rotation.slideY(-25, 0.3F);
			data.rightLeg.rotation.slideZ(10);
			data.leftLeg.rotation.slideZ(-10);

			data.rightForeLeg.rotation.slideX(30, 0.3F);
			data.leftForeLeg.rotation.slideX(30, 0.3F);

			mainArm.getPreRotation().slideZ(60.0f * handDirMtp, 0.3F);
			mainArm.getRotation().slideX(60.0f, 0.3F);
			offArm.getRotation().slideZ(20 * handDirMtp, 0.3F);
			offArm.getPreRotation().slideZ(-80 * handDirMtp, 0.3F);

			/*
			 * if (offHandItemStack != null &&
			 * offHandItemStack.getItem().getItemUseAction(offHandItemStack) ==
			 * EnumAction.BLOCK) { offArm.getPreRotation().slideZ(-40 * handDirMtp, 0.3f); }
			 */

			mainForeArm.getRotation().slideX(-20, 0.3f);
			offForeArm.getRotation().slideX(-60, 0.3f);

			if (mainHandSwitch)
			{
				data.renderRightItemRotation.slideX(65, 0.3f);
			}
			else
			{
				data.renderLeftItemRotation.slideX(65, 0.3f);
			}
			data.renderOffset.slideY(-2.0F);
		}
		else
		{
			if (player.isSprinting())
			{
				data.body.rotation.slideY(20, 0.3F);

				data.head.rotation.setY(data.getHeadYaw() - 20);
				data.head.rotation.setX(data.getHeadPitch() - 15);

				data.rightLeg.rotation.slideY(0F);
				data.leftLeg.rotation.slideY(0F);

				mainArm.getRotation().slideX(60F, 0.3F);

				if (mainHandSwitch)
				{
					data.renderRightItemRotation.slideX(90, 0.3F);
				}
				else
				{
					data.renderLeftItemRotation.slideX(90, 0.3F);
				}
			}
		}

		BendsPack.animate(data, "player", "attack_stance");
	}

}
