package net.gobbob.mobends.standard.animation.bit.pigzombie;

import net.gobbob.mobends.standard.data.BipedEntityData;
import net.gobbob.mobends.standard.data.PigZombieData;
import net.gobbob.mobends.standard.data.ZombieData;
import net.minecraft.util.math.MathHelper;

public class StandAnimationBit extends net.gobbob.mobends.standard.animation.bit.biped.StandAnimationBit<PigZombieData>
{
	@Override
	public void perform(PigZombieData data)
	{
		super.perform(data);
		
		data.renderOffset.slideY(-3F);
		
		data.body.rotation.localRotateX(20F)
						  .rotateZ(-10F);
		data.head.rotation.rotateX(-20F);
		
		data.rightArm.rotation.rotateX(-20F)
						      .rotateZ(10F);
		data.leftArm.rotation.rotateX(-20F)
	    					 .rotateZ(10F);
		
		data.rightLeg.rotation.rotateZ(10F);
		data.leftLeg.rotation.rotateZ(-10F);
		
		data.rightLeg.rotation.rotateX(-30F);
		data.leftLeg.rotation.rotateX(-10F)
							 .rotateY(-10F);
		
		data.rightForeLeg.rotation.rotateX(25);
		data.leftForeLeg.rotation.rotateX(25);
	}
}
