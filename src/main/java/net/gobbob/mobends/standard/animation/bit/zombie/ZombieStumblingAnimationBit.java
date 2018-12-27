package net.gobbob.mobends.animation.bit.zombie;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.core.EntityData;
import net.gobbob.mobends.core.animation.bit.AnimationBit;
import net.gobbob.mobends.data.ZombieData;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.math.MathHelper;

public class ZombieStumblingAnimationBit extends AnimationBit<ZombieData>
{
	@Override
	public String[] getActions(ZombieData data)
	{
		return new String[] { "stumbling" };
	}

	@Override
	public void perform(ZombieData data)
	{
		final float PI = (float) Math.PI;
		float limbSwing = data.getLimbSwing() * 0.6662F;
		limbSwing += Math.cos(limbSwing * 2.0F) * 0.3F;
		float swingAmount = 45F * data.getLimbSwingAmount();

		data.rightLeg.rotation.setSmoothness(1F).orientX((MathHelper.cos(limbSwing) * swingAmount));
		data.leftLeg.rotation.setSmoothness(1F).orientX((MathHelper.cos(limbSwing + PI) * swingAmount));
		data.rightArm.rotation.setSmoothness(1F).orientX((MathHelper.cos(limbSwing + PI) * swingAmount));
		data.leftArm.rotation.setSmoothness(1F).orientX((MathHelper.cos(limbSwing) * swingAmount));
		data.body.rotation.setSmoothness(.5F).orientY((MathHelper.cos(limbSwing + PI) * swingAmount));
		
		float heavyStompValue = Math.min((limbSwing % PI) / PI, 1F);
		float heavyStompValueInv = 1F - Math.min((limbSwing % PI) / PI, 1F);
		data.body.rotation.rotateX(heavyStompValueInv * 40F);
		data.body.rotation.rotateZ(MathHelper.cos(limbSwing) * 10F);
		
		data.head.rotation.rotateX(-heavyStompValueInv * 40F);
		// Head tilt
		data.head.rotation.rotateZ(-40F + heavyStompValue * 20.0F);
	}
}
