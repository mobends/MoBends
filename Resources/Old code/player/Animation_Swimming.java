package net.gobbob.mobends.animation.player;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.animation.Animation;
import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.model.entity.IBendsModel;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.data.DataPlayer;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.client.model.ModelBiped.ArmPose;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

public class Animation_Swimming extends Animation{
	public String getName(){
		return "swimming";
	}

	@Override
	public void animate(EntityLivingBase argEntity, IBendsModel argModel, EntityData argData) {
		ModelBendsPlayer model = (ModelBendsPlayer) argModel;
		DataPlayer data = (DataPlayer) argData;
		
		float armSway = (MathHelper.cos(data.ticks*0.1625f)+1.0f)/2.0f;
		float armSway2 = (-MathHelper.sin(data.ticks*0.1625f)+1.0f)/2.0f;
		float legFlap = MathHelper.cos(data.ticks*0.4625f);
		float foreArmSway = ((float) ((data.ticks*0.1625f)%(Math.PI*2)))/((float)Math.PI*2);
		float foreArmStretch = (armSway)*2.0f;
		foreArmStretch-=1.0f;
		foreArmStretch = Math.max(foreArmStretch, 0.0f);
		
		((ModelPart)model.bipedLeftLeg).rotation.setSmoothY(0,0.3f);
		((ModelPart)model.bipedRightLeg).rotation.setSmoothY(0,0.3f);
		((ModelPart)model.bipedBody).pre_rotation.setSmooth(new Vector3f(0, 0, 0), 0.5f);
		
		if((data.motion.x == 0 & data.motion.z == 0) || model.leftArmPose == ArmPose.BOW_AND_ARROW || model.rightArmPose == ArmPose.BOW_AND_ARROW || data.ticksAfterPunch < 10){
			armSway = (MathHelper.cos(data.ticks*0.0825f)+1.0f)/2.0f;
			armSway2 = (-MathHelper.sin(data.ticks*0.0825f)+1.0f)/2.0f;
			legFlap = MathHelper.cos(data.ticks*0.2625f);
			
			((ModelPart)model.bipedHead).pre_rotation.setSmooth(new Vector3f(0, 0, 0),0.3f);
			((ModelPart)model.bipedLeftArm).pre_rotation.setSmooth(new Vector3f(0, 0, 0), 0.5f);
			((ModelPart)model.bipedLeftArm).rotation.setSmooth(new Vector3f(armSway2*30-15, 0, -armSway*30),0.3f);
			((ModelPart)model.bipedRightArm).pre_rotation.setSmooth(new Vector3f(0, 0, 0), 0.5f);
			((ModelPart)model.bipedRightArm).rotation.setSmooth(new Vector3f(armSway2*30-15, 0, armSway*30),0.3f);
			((ModelPart)model.bipedLeftForeArm).rotation.setSmoothX(armSway2*-40,0.3f);
			((ModelPart)model.bipedRightForeArm).rotation.setSmoothX(armSway2*-40,0.3f);
			((ModelPart)model.bipedLeftLeg).rotation.setSmoothX((legFlap)*40,0.3f);
			((ModelPart)model.bipedRightLeg).rotation.setSmoothX((-legFlap)*40,0.3f);
			((ModelPart)model.bipedLeftForeLeg).rotation.setSmoothX(5,0.4f);
			((ModelPart)model.bipedRightForeLeg).rotation.setSmoothX(5,0.4f);
			((ModelPart)model.bipedBody).rotation.setSmoothX(armSway*10);
			((ModelPart)model.bipedBody).rotation.setSmoothY(0, 0.5f);
			((ModelPart)model.bipedBody).pre_rotation.setSmooth(new Vector3f(0, 0, 0), 0.5f);
			((ModelPart)model.bipedHead).rotation.setSmoothX(model.headRotationX);
			((ModelPart)model.bipedHead).rotation.setSmoothY(model.headRotationY);
		}else{
			((ModelPart)model.bipedHead).pre_rotation.setSmooth(new Vector3f(-70-armSway*-20, 0, 0),0.3f);
			model.renderRotation.setSmoothX(70.0f,0.3f);
			model.renderRotation.setSmoothY(0.0f, 0.3f);
			model.renderOffset.setSmoothZ(10,0.3f);
			
			((ModelPart)model.bipedLeftArm).pre_rotation.setSmooth(new Vector3f(0, 90, armSway*-20),0.3f);
			((ModelPart)model.bipedLeftArm).rotation.setSmooth(new Vector3f(armSway*-120 - 45, 0, 0), 0.3f);
			((ModelPart)model.bipedRightArm).pre_rotation.setSmooth(new Vector3f(0, -90, armSway*20),0.3f);
			((ModelPart)model.bipedRightArm).rotation.setSmooth(new Vector3f(armSway*-120 - 45, 0, 0), 0.3f);
			
			((ModelPart)model.bipedLeftForeArm).rotation.setSmoothX((foreArmSway < 0.55f | foreArmSway > 0.9) ? foreArmStretch*-60.0f : -60,0.3f);
			((ModelPart)model.bipedRightForeArm).rotation.setSmoothX((foreArmSway < 0.55f | foreArmSway > 0.9) ? foreArmStretch*-60.0f : -60,0.3f);
			
			((ModelPart)model.bipedLeftLeg).rotation.setSmoothX((legFlap)*40,0.3f);
			((ModelPart)model.bipedRightLeg).rotation.setSmoothX((-legFlap)*40,0.3f);
			
			((ModelPart)model.bipedLeftForeLeg).rotation.setSmoothX(5,0.4f);
			((ModelPart)model.bipedRightForeLeg).rotation.setSmoothX(5,0.4f);
			
			((ModelPart)model.bipedBody).rotation.setSmoothY(0, 0.5f);
			((ModelPart)model.bipedBody).rotation.setSmoothX(armSway*-20);
			
			((ModelPart)model.bipedHead).rotation.setSmoothX(model.headRotationX);
			((ModelPart)model.bipedHead).rotation.setSmoothY(model.headRotationY);
			
			model.renderRightItemRotation.setSmoothX(armSway*120,0.3f);
		}
	}
}
