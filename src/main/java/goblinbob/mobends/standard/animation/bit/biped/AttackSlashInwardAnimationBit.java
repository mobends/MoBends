package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.core.math.SmoothOrientation;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.util.vector.Vector3f;

public class AttackSlashInwardAnimationBit extends AnimationBit<BipedEntityData<?>>
{

	private static final String[] ACTIONS = new String[] { "attack", "attack_slash_inward" };
	
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
		data.localOffset.slideToZero(0.3F);

		final EntityLivingBase living = data.getEntity();
		final EnumHandSide primaryHand = living.getPrimaryHand();

		boolean mainHandSwitch = primaryHand == EnumHandSide.RIGHT;
		// Main Hand Direction Multiplier - it helps switch animation sides depending on
		// what is your main hand.
		float handDirMtp = mainHandSwitch ? 1 : -1;
		IModelPart mainArm = mainHandSwitch ? data.rightArm : data.leftArm;
		IModelPart offArm = mainHandSwitch ? data.leftArm : data.rightArm;
		IModelPart mainForeArm = mainHandSwitch ? data.rightForeArm : data.leftForeArm;
		IModelPart offForeArm = mainHandSwitch ? data.leftForeArm : data.rightForeArm;
		SmoothOrientation mainItemRotation = mainHandSwitch ? data.renderRightItemRotation : data.renderLeftItemRotation;
		
		if (data.getTicksAfterAttack() < 4F
				&& living.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemSword)
		{
			data.swordTrail.add(data);
		}

		float attackState = data.getTicksAfterAttack() / 10F;
		float armSwing = attackState * 3F;
		armSwing = Math.min(armSwing, 1F);

		Vector3f bodyRot = new Vector3f(0, 0, 0);
		bodyRot.x = 20F - armSwing * 20F;
		bodyRot.y = -70F * armSwing * handDirMtp;

		data.body.rotation.setSmoothness(.9F).orientX(bodyRot.x)
				.orientY(bodyRot.y);
		data.head.rotation.setSmoothness(.9F).orientX(MathHelper.wrapDegrees(data.headPitch.get()) - bodyRot.x)
						  .rotateY(MathHelper.wrapDegrees(data.headYaw.get()) - bodyRot.y);

		mainArm.getRotation().setSmoothness(.9F).orientZ(90F * handDirMtp)
				.rotateY((60F - armSwing * 180F) * handDirMtp);
		
		offArm.getRotation().setSmoothness(.3F).orientZ(-20 * handDirMtp);

		mainForeArm.getRotation().setSmoothness(.3F).orientX(-10);
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
			data.globalOffset.slideY(-1.0F);
		}

		mainItemRotation.setSmoothness(.9F).orientInstantX(50.0F);
	}

}
