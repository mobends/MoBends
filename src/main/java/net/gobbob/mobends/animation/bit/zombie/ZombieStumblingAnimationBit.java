package net.gobbob.mobends.animation.bit.zombie;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.ZombieData;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.math.MathHelper;

public class ZombieStumblingAnimationBit extends AnimationBit
{
	@Override
	public String[] getActions(EntityData entityData)
	{
		return new String[] { "stumbling" };
	}

	@Override
	public void perform(EntityData entityData)
	{
		if (!(entityData instanceof ZombieData))
			return;
		if (!(entityData.getEntity() instanceof EntityZombie))
			return;

		ZombieData data = (ZombieData) entityData;

		final float PI = (float) Math.PI;
		float limbSwing = data.getLimbSwing() * 0.6662F;
		limbSwing += Math.cos(limbSwing * 2.0F) * 0.3F;
		float swingAmount = 45F * data.getLimbSwingAmount();

		/*data.rightLeg.rotation.slideX((MathHelper.cos(limbSwing) * swingAmount), 1F);
		data.leftLeg.rotation.slideX((MathHelper.cos(limbSwing + PI) * swingAmount), 1F);
		data.rightArm.rotation.slideX((MathHelper.cos(limbSwing + PI) * swingAmount), 1F);
		data.leftArm.rotation.slideX((MathHelper.cos(limbSwing) * swingAmount), 1F);
		data.body.rotation.slideY((MathHelper.cos(limbSwing + PI) * swingAmount), 0.5f);
		
		float heavyStompValue = Math.min((limbSwing % PI) / PI, 1F);
		float heavyStompValueInv = 1F - Math.min((limbSwing % PI) / PI, 1F);
		data.body.rotation.addX(heavyStompValue * 40F);
		data.body.rotation.slideZ(MathHelper.cos(limbSwing) * 10F);
		
		// Head tilt
		data.head.preRotation.slideZ(-40F + heavyStompValue * 20.0F);*/
	}
}
