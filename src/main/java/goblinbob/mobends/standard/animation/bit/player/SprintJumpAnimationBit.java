package goblinbob.mobends.standard.animation.bit.player;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.standard.data.PlayerData;
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
	public void perform(PlayerData data)
	{
		if (data.getPrevMotionY() < 0 && data.getMotionY() > 0)
		{
			/*
			 * Restarting the animation if the player is going back up again after falling
			 * down.
			 */
			this.onPlay(data);
		}
		
		AbstractClientPlayer player = data.getEntity();

		boolean sprintLegSwitch = data.getSprintJumpLeg();
		
		float legSwitchMtp = sprintLegSwitch ? 1 : -1;
		IModelPart mainArm = sprintLegSwitch ? data.rightArm : data.leftArm;
		IModelPart offArm = sprintLegSwitch ? data.leftArm : data.rightArm;
		IModelPart mainLeg = sprintLegSwitch ? data.rightLeg : data.leftLeg;
		IModelPart offLeg = sprintLegSwitch ? data.leftLeg : data.rightLeg;
		IModelPart mainForeLeg = sprintLegSwitch ? data.rightForeLeg : data.leftForeLeg;
		IModelPart offForeLeg = sprintLegSwitch ? data.leftForeLeg : data.rightForeLeg;
		
		float bodyRotationY = 20 * legSwitchMtp;
		float bodyLean = MathHelper.clamp((float) data.getMotionY(), -.2F, .2F);
		bodyLean = bodyLean * -100F + 20F;
		
		if (this.relax < 1F)
		{
			this.relax += DataUpdateHandler.ticksPerFrame * 0.1F;
			this.relax = Math.min(this.relax, 1F);
		}
		
		float relaxAngle = MathHelper.sqrt(MathHelper.sqrt(this.relax));
		
		data.centerRotation.setSmoothness(.3F).orientZero();
		
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
		
		data.head.rotation.orientX(data.headPitch.get() - 20);
		data.head.rotation.rotateY(data.headYaw.get() - bodyRotationY);
	}
}
