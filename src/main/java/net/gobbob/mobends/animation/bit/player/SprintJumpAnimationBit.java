package net.gobbob.mobends.animation.bit.player;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.client.model.IModelPart;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.PlayerData;
import net.gobbob.mobends.util.GUtil;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;

public class SprintJumpAnimationBit extends AnimationBit<PlayerData>
{
	private float relax = 0F;
	
	@Override
	public String[] getActions(PlayerData entityData)
	{
		return new String[] { "sprint_jump" };
	}
	
	@Override
	public void onPlay(PlayerData entityData)
	{
		this.relax = 0F;
	}
	
	@Override
	public void perform(PlayerData entityData)
	{
		if (!(entityData instanceof PlayerData))
			return;
		
		if (entityData.getPreviousMotion().y < 0 && entityData.getMotion().y > 0)
		{
			/*
			 * Restarting the animation if the player is going back up again after falling
			 * down.
			 */
			this.onPlay(entityData);
		}
		
		PlayerData data = (PlayerData) entityData;
		AbstractClientPlayer player = (AbstractClientPlayer) data.getEntity();
		EnumHandSide primaryHand = player.getPrimaryHand();

		boolean sprintLegSwitch = data.getSprintJumpLeg();
		
		float legSwitchMtp = sprintLegSwitch ? 1 : -1;
		IModelPart mainArm = sprintLegSwitch ? data.rightArm : data.leftArm;
		IModelPart offArm = sprintLegSwitch ? data.leftArm : data.rightArm;
		IModelPart mainForeArm = sprintLegSwitch ? data.rightForeArm : data.leftForeArm;
		IModelPart offForeArm = sprintLegSwitch ? data.leftForeArm : data.rightForeArm;
		IModelPart mainLeg = sprintLegSwitch ? data.rightLeg : data.leftLeg;
		IModelPart offLeg = sprintLegSwitch ? data.leftLeg : data.rightLeg;
		IModelPart mainForeLeg = sprintLegSwitch ? data.rightForeLeg : data.leftForeLeg;
		IModelPart offForeLeg = sprintLegSwitch ? data.leftForeLeg : data.rightForeLeg;
		
		float bodyRotationY = 20 * legSwitchMtp;
		float bodyLean = GUtil.clamp(data.getMotion().y, -.2F, .2F);
		bodyLean = bodyLean * -100F + 20F;
		
		if (this.relax < 1F)
		{
			this.relax += DataUpdateHandler.ticksPerFrame * 0.1F;
			this.relax = Math.min(this.relax, 1F);
		}
		
		float relaxAngle = MathHelper.sqrt(MathHelper.sqrt(this.relax));
		
		data.body.rotation.setSmoothness(.3F).orientX(bodyLean)
				.rotateY(bodyRotationY);
		data.rightLeg.rotation.setSmoothness(.8F).orientZ(5);
		data.leftLeg.rotation.setSmoothness(.8F).orientZ(-5);
		data.rightArm.rotation.setSmoothness(.3F).orientZ(10);
		data.leftArm.rotation.setSmoothness(.3F).orientZ(-10);

		mainLeg.getRotation().rotateX(-45);
		offLeg.getRotation().rotateX(45);
		mainArm.getRotation().rotateX(50);
		offArm.getRotation().rotateX(-50);
		
		mainForeLeg.getRotation().orientX(80F - relaxAngle * 80F);
		offForeLeg.getRotation().orientX(relaxAngle * 70F);
		
		data.head.rotation.orientInstantX(data.getHeadPitch() - 20);
		data.head.rotation.rotateY(data.getHeadYaw() - bodyRotationY);
	}
}
