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
import net.minecraft.util.math.MathHelper;

public class AttackStanceAnimationBit extends AnimationBit
{
	protected final float PI = (float) Math.PI;
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
		
		data.renderRotation.setSmoothness(.3F).orientY(-30 * handDirMtp);

		float bodyRotationX = 20.0F + breath0 * 2.0F;

		data.body.rotation.setSmoothness(0.3F).orientX(bodyRotationX);
		data.head.rotation.rotateY(-30 * handDirMtp);
		data.head.rotation.rotateX(-bodyRotationX);

		data.rightLeg.rotation.setSmoothness(0.3F).orientX(-30)
				.rotateZ(10);
		data.leftLeg.rotation.setSmoothness(0.3F).orientX(-30)
				.rotateY(-25)
				.rotateZ(-10);

		data.rightForeLeg.rotation.setSmoothness(0.3F).orientX(30);
		data.leftForeLeg.rotation.setSmoothness(0.3F).orientX(30);

		mainArm.getRotation().setSmoothness(0.3F).orientZ(60F * handDirMtp + breath0 * 5F)
				.rotateY(breath1 * 5F);
		offArm.getRotation().setSmoothness(0.3F).orientZ(-60 * handDirMtp + breath1 * 5F);

		mainForeArm.getRotation().setSmoothness(0.3F).orientX(-20);
		offForeArm.getRotation().setSmoothness(0.3F).orientX(-60);

		if (mainHandSwitch)
		{
			data.renderRightItemRotation.setSmoothness(.3F).orientX(65);
		}
		else
		{
			data.renderLeftItemRotation.setSmoothness(.3F).orientX(65);
		}
		data.renderOffset.slideY(-2.0F);
		
		if (this.legSpreadAnimation < 1.0F)
		{
			this.legSpreadAnimation += this.legSpreadSpeed * DataUpdateHandler.ticksPerFrame;
			if (this.legSpreadAnimation > 1.0F)
				this.legSpreadAnimation = 1.0F;
			
			mainLeg.getRotation().orientInstantX(MathHelper.sin(this.legSpreadAnimation * PI) * -20.0F - 40.0F);
			mainLeg.getRotation().rotateY((this.legSpreadAnimation * 10.0F - 5.0F) * handDirMtp);
			mainForeLeg.getRotation().orientX(MathHelper.sin(this.legSpreadAnimation * PI) * 40.0F + 40.0F);
		}
		
		float touchdown = Math.min(data.getTicksAfterTouchdown() * kneelDuration, 1.0F);
		if (touchdown < 1.0F)
		{
			data.body.rotation.setSmoothness(1F);
			data.body.rotation.orientX(5F * (1 - touchdown) + 15F);
			data.renderOffset.setY(-MathHelper.sin(touchdown * PI) * 2F - 2F);
		}
	}

}
