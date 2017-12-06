package net.gobbob.mobends.animation.bit.player;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.animation.layer.AnimationLayer;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.data.DataPlayer;
import net.gobbob.mobends.data.EntityData;

public class AnimationStand extends AnimationBit
{
	public AnimationStand(AnimationLayer layer) {
		super(layer);
	}
	
	@Override
	public void perform(EntityData entityData)
	{
		if(!(entityData instanceof DataPlayer))
			return;
		DataPlayer data = (DataPlayer) entityData;
		
		data.body.pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f), 0.5f);
		data.body.rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f), 0.5f);
		data.rightLeg.rotation.setSmoothZ(2, 0.2f);
		data.leftLeg.rotation.setSmoothZ(-2, 0.2f);
		data.rightLeg.rotation.setSmoothX(0.0F, 0.1f);
		data.leftLeg.rotation.setSmoothX(0.0F, 0.1f);
		
		data.leftLeg.rotation.setSmoothY(-5);
		data.rightLeg.rotation.setSmoothY(5);
		
		data.rightArm.pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		data.rightArm.rotation.setSmooth(new Vector3f(0.0F, 0.0F, 0.0F), 0.1f);
		data.leftArm.pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		data.leftArm.rotation.setSmooth(new Vector3f(0.0F, 0.0F, 0.0F), 0.1f);
		data.rightForeLeg.rotation.setSmoothX(4.0F, 0.1f);
		data.leftForeLeg.rotation.setSmoothX(4.0F, 0.1f);
		data.rightForeArm.rotation.setSmoothX(-4.0F, 0.1f);
		data.leftForeArm.rotation.setSmoothX(-4.0F, 0.1f);
		data.head.pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		data.head.rotation.set(data.getHeadPitch(), data.getHeadYaw(), 0);
		
		data.body.rotation.setSmoothX( (float) ((Math.cos(DataUpdateHandler.getTicks() / 10)-1.0)/2.0f)*-3);
		data.leftArm.rotation.setSmoothZ( -(float) ((Math.cos(DataUpdateHandler.getTicks() / 10+Math.PI/2)-1.0)/2.0f)*-5);
		data.rightArm.rotation.setSmoothZ(  -(float) ((Math.cos(DataUpdateHandler.getTicks() / 10+Math.PI/2)-1.0)/2.0f)*5);
	}
}
