package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHandSide;

public class FistGuardAnimationBit extends AnimationBit<BipedEntityData<?>>
{
	private static final String[] ACTIONS = new String[] { "fist_guard" };
	
	@Override
	public String[] getActions(BipedEntityData<?> entityData)
	{
		return ACTIONS;
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

		if (!data.isStillHorizontally())
		{
			// Skipping the fist guard if the entity is moving around.
			return;
		}

		data.root.offset.slideY(-2.0F);
		data.root.rotation.setSmoothness(.3F).orientY(-20 * handDirMtp);
		
		data.rightArm.rotation.setSmoothness(.3F).orientX(-90F)
				.rotateZ(20F);
		data.rightForearm.rotation.setSmoothness(.3F).orientX(-80F);

		data.leftArm.rotation.setSmoothness(.3F).orientX(-90F)
				.rotateZ(-20F);
		data.leftForearm.rotation.setSmoothness(.3F).orientX(-80F);
		
		data.body.rotation.rotateX(10);

		data.rightLeg.rotation.setSmoothness(.3F).orientX(-30F)
				.rotateZ(10);
		data.leftLeg.rotation.setSmoothness(.3F).orientX(-30F)
				.rotateY(-25F)
				.rotateZ(-10);

		data.rightShin.rotation.setSmoothness(.3F).orientX(30);
		data.leftShin.rotation.setSmoothness(.3F).orientX(30);

		data.head.rotation.rotateX(-10);
		data.head.rotation.rotateY(-20 * handDirMtp);
	}
}
