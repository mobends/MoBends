package net.gobbob.mobends.animation.bit.player;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.animation.layer.AnimationLayer;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.data.PlayerData;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.util.math.MathHelper;

public class WalkAnimationBit extends AnimationBit
{
	public WalkAnimationBit(AnimationLayer layer) {
		super(layer);
	}
	
	@Override
	public void perform(EntityData entityData)
	{
		if(!(entityData instanceof PlayerData))
			return;
		PlayerData data = (PlayerData) entityData;
		
		data.rightArm.pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		data.rightArm.rotation.setSmoothX(0.5f*(float) ((MathHelper.cos(data.getLimbSwing() * 0.6662F + (float)Math.PI) * 2.0F * data.getLimbSwingAmount() * 0.5F ) / Math.PI * 180.0f));
		data.leftArm.pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		data.leftArm.rotation.setSmoothX(0.5f*(float) ((MathHelper.cos(data.getLimbSwing() * 0.6662F) * 2.0F * data.getLimbSwingAmount() * 0.5F) / Math.PI * 180.0f));
		
		data.rightArm.rotation.setSmoothY(0,0.3f);
		data.leftArm.rotation.setSmoothY(0,0.3f);
		data.rightArm.rotation.setSmoothZ(5,0.3f);
		data.leftArm.rotation.setSmoothZ(-5,0.3f);
		
		data.rightLeg.rotation.setSmoothX(-5.0f+0.5f*(float) ((MathHelper.cos(data.getLimbSwing() * 0.6662F) * 1.4F * data.getLimbSwingAmount()) / Math.PI * 180.0f),1.0f);
		data.leftLeg.rotation.setSmoothX(-5.0f+0.5f*(float) ((MathHelper.cos(data.getLimbSwing() * 0.6662F + (float)Math.PI) * 1.4F * data.getLimbSwingAmount()) / Math.PI * 180.0f),1.0f);
		
		data.rightLeg.rotation.setSmoothY(0.0f);
		data.leftLeg.rotation.setSmoothY(0.0f);
		
		data.rightLeg.rotation.setSmoothZ(2,0.2f);
		data.leftLeg.rotation.setSmoothZ(-2,0.2f);
		
		float var = (float) ((float) (data.getLimbSwing() * 0.6662F)/Math.PI)%2;
		data.leftForeLeg.rotation.setSmoothX( (var > 1 ? 45 : 0), 0.3f);
		data.rightForeLeg.rotation.setSmoothX( (var > 1 ? 0 : 45), 0.3f);
		data.leftForeArm.rotation.setSmoothX( ((float) (Math.cos(data.getLimbSwing() * 0.6662F + Math.PI/2)+1.0f)/2.0f)*-20, 1.0f);
		data.rightForeArm.rotation.setSmoothX( ((float) (Math.cos(data.getLimbSwing() * 0.6662F)+1.0f)/2.0f)*-20, 0.3f);
		
		float var2 = (float)Math.cos(data.getLimbSwing() * 0.6662F)*-20;
		float var3 = (float)(Math.cos(data.getLimbSwing() * 0.6662F * 2.0f)*0.5f+0.5f)*10-2;
		data.body.pre_rotation.setSmooth(new Vector3f(0.0f,0.0f,0.0f),0.5f);
		data.body.rotation.setSmoothY(var2,0.5f);
		data.body.rotation.setSmoothX(var3);
		data.head.pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		data.head.rotation.setY(data.getHeadYaw() - data.body.rotation.getNextY(DataUpdateHandler.ticksPerFrame));
		data.head.rotation.setX(data.getHeadPitch() - data.body.rotation.getNextX(DataUpdateHandler.ticksPerFrame));
		
		float var10 = data.getHeadYaw() * .1F;
		var10 = Math.min(var10, 10);
		var10 = Math.max(var10, -10);
		data.body.rotation.setSmoothZ(-var10, 0.3f);
	}
}
