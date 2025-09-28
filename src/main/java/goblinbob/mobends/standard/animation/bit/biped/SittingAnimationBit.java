package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.standard.data.BipedEntityData;

public class SittingAnimationBit extends AnimationBit<BipedEntityData<?>>
{
	private static final String[] ACTIONS = new String[] { "sitting" };

	@Override
	public String[] getActions(BipedEntityData<?> entityData)
	{
		return ACTIONS;
	}

	@Override
	public void perform(BipedEntityData<?> data)
	{
		data.root.rotation.setSmoothness(.3F).orientZero();
		
		data.head.rotation.orientX(data.headPitch.get())
		  				  .rotateY(data.headYaw.get());
		data.body.rotation.orientY(0).setSmoothness(0.5F);
		
		data.leftLeg.rotation.orientX(-90.0F).rotateZ(-10.0F).rotateY(-15.0F);
		data.rightLeg.rotation.orientX(-90.0F).rotateZ(10.0F).rotateY(15.0F);
		data.leftShin.rotation.orientX(10.0F);
		data.rightShin.rotation.orientX(10.0F);
		
		data.leftArm.rotation.orientX(0.0F).rotateZ(-10F);
		data.leftForearm.rotation.orientX(-10.0F);
		data.rightArm.rotation.orientX(0.0F).rotateZ(10F);
		data.rightForearm.rotation.orientX(-10.0F);
		
		data.leftHeldItem.rotation.orientZero();
		data.rightHeldItem.rotation.orientZero();
	}
}
