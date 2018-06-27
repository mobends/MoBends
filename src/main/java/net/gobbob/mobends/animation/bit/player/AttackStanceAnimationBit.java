package net.gobbob.mobends.animation.bit.player;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.animation.layer.AnimationLayer;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.client.model.IModelPart;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.PlayerData;
import net.gobbob.mobends.pack.BendsPack;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.EnumHandSide;

public class AttackStanceAnimationBit extends AnimationBit
{
	protected final float kneelDuration = 0.15F;
	protected final float legSpreadSpeed = 0.1F;
	protected float legSpreadAnimation = 0F;
	
	@Override
	public String[] getActions(EntityData entityData)
	{
		return new String[] { "attack_stance" };
	}
	
	@Override
	public void onPlay(EntityData entityData)
	{
		this.legSpreadAnimation = 0F;
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
		IModelPart mainLeg = mainHandSwitch ? data.rightLeg : data.leftLeg;
		IModelPart offLeg = mainHandSwitch ? data.leftLeg : data.rightLeg;
		IModelPart mainForeLeg = mainHandSwitch ? data.rightForeLeg : data.leftForeLeg;
		IModelPart offForeLeg = mainHandSwitch ? data.leftForeLeg : data.rightForeLeg;

		// ItemStack offHandItemStack = player.getHeldItemOffhand();

		float breath0 = (float) Math.sin(DataUpdateHandler.getTicks() / 5.0);
		float breath1 = (float) Math.cos(DataUpdateHandler.getTicks() / 5.7);
		
		data.renderRotation.slideY(-30 * handDirMtp, 0.3F);

		Vector3f bodyRot = new Vector3f(0, 0, 0);

		bodyRot.x = 20.0F + breath0 * 2.0F;

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

		mainArm.getPreRotation().slideZ(60.0f * handDirMtp + breath0 * 5.0F, 0.3F);
		mainArm.getRotation().slideX(60.0f, 0.3F);
		mainArm.getRotation().slideY(breath1 * 5.0F, 0.3F);
		offArm.getRotation().slideZ(20 * handDirMtp + breath1 * 5.0F, 0.3F);
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
		
		if (this.legSpreadAnimation < 1.0F)
		{
			this.legSpreadAnimation += this.legSpreadSpeed * DataUpdateHandler.ticksPerFrame;
			if (this.legSpreadAnimation > 1.0F)
				this.legSpreadAnimation = 1.0F;
			
			mainLeg.getRotation().setY((this.legSpreadAnimation * 10.0F - 5.0F) * handDirMtp);
			mainLeg.getRotation().setX((float) Math.sin(this.legSpreadAnimation * Math.PI) * -20.0F - 40.0F);
			mainForeLeg.getRotation().setX((float) Math.sin(this.legSpreadAnimation * Math.PI) * 40.0F + 40.0F);
		}
		
		float touchdown = Math.min(data.getTicksAfterTouchdown() * kneelDuration, 1.0F);
		if (touchdown < 1.0F)
		{
			data.body.rotation.setX(5.0F * (1 - touchdown) + 15.0F);
			data.renderOffset.setY((float) -Math.sin(touchdown * Math.PI) * 2.0F - 2.0F);
		}
	}

}
