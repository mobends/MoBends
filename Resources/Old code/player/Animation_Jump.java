package net.gobbob.mobends.animation.player;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.animation.Animation;
import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.model.entity.IBendsModel;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.data.DataPlayer;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

public class Animation_Jump extends Animation{
	
	public String getName(){
		return "jump";
	}

	@Override
	public void animate(EntityLivingBase argEntity, IBendsModel argModel, EntityData argData) {
		ModelBendsPlayer model = (ModelBendsPlayer) argModel;
		DataPlayer data = (DataPlayer) argData;
		
		((ModelPart)model.bipedBody).pre_rotation.setSmooth(new Vector3f(0.0f,0.0f,0.0f),0.5f);
		((ModelPart)model.bipedBody).rotation.setSmoothX(0,0.05f);
		((ModelPart)model.bipedBody).rotation.setSmoothY(0,0.1f);
		((ModelPart)model.bipedBody).rotation.setSmoothZ(0.0f, 0.3f);
		((ModelPart)model.bipedRightArm).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelPart)model.bipedRightArm).rotation.setSmooth(new Vector3f(0.0F, 0.0F, 0.0F),0.5f);
		((ModelPart)model.bipedRightArm).rotation.setSmoothZ(45,0.05f);
		((ModelPart)model.bipedLeftArm).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelPart)model.bipedLeftArm).rotation.setSmooth(new Vector3f(0.0F, 0.0F, 0.0F),0.5f);
		((ModelPart)model.bipedLeftArm).rotation.setSmoothZ(-45,0.05f);
		((ModelPart)model.bipedRightArm).rotation.setSmoothX(0.0f,0.5f);
		((ModelPart)model.bipedLeftArm).rotation.setSmoothX(0.0f,0.5f);
		((ModelPart)model.bipedRightLeg).rotation.setSmoothZ(10, 0.1f);
		((ModelPart)model.bipedLeftLeg).rotation.setSmoothZ(-10, 0.1f);
		((ModelPart)model.bipedRightLeg).rotation.setSmoothX(-20, 0.1f);
		((ModelPart)model.bipedLeftLeg).rotation.setSmoothX(-20, 0.1f);
		
		((ModelPart)model.bipedRightLeg).rotation.setSmoothX(-45,0.1f);
		((ModelPart)model.bipedLeftLeg).rotation.setSmoothX(-45,0.1f);
		((ModelPart)model.bipedRightForeLeg).rotation.setSmoothX(50,0.1f);
		((ModelPart)model.bipedLeftForeLeg).rotation.setSmoothX(50,0.1f);
		
		((ModelPart)model.bipedRightForeArm).rotation.setSmoothX(0,0.3f);
		((ModelPart)model.bipedLeftForeArm).rotation.setSmoothX(0,0.3f);
		
		((ModelPart)model.bipedHead).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelPart)model.bipedHead).rotation.setY(model.headRotationY);
		((ModelPart)model.bipedHead).rotation.setX(model.headRotationX-model.bipedBody.rotateAngleX);
		
		if(data.ticksAfterLiftoff < 2.0f){
			((ModelPart)model.bipedBody).rotation.setSmoothX(20.0f,2f);
			((ModelPart)model.bipedRightLeg).rotation.setSmoothX(0.0f,2f);
			((ModelPart)model.bipedLeftLeg).rotation.setSmoothX(0.0f,2f);
			((ModelPart)model.bipedRightForeLeg).rotation.setSmoothX(0.0f,2f);
			((ModelPart)model.bipedLeftForeLeg).rotation.setSmoothX(0.0f,2f);
			((ModelPart)model.bipedRightArm).rotation.setSmoothZ(2,2f);
			((ModelPart)model.bipedLeftArm).rotation.setSmoothZ(-2,2f);
			((ModelPart)model.bipedRightForeArm).rotation.setSmoothX(-20,2f);
			((ModelPart)model.bipedLeftForeArm).rotation.setSmoothX(-20,2f);
		}
		
		if(argData.motion.x != 0 | argData.motion.z != 0){
			if(argEntity.isSprinting()){
				float bodyRot = 0.0f;
				float bodyLean = argData.motion.y;
				if(bodyLean < -0.2f) bodyLean = -0.2f;
				if(bodyLean > 0.2f) bodyLean = 0.2f;
				bodyLean=bodyLean*-100.0f+20;
				
				((ModelPart)model.bipedBody).rotation.setSmoothX(bodyLean, 0.3f);
				((ModelPart)model.bipedRightLeg).rotation.setSmoothZ(5,0.3f);
				((ModelPart)model.bipedLeftLeg).rotation.setSmoothZ(-5,0.3f);
				((ModelPart)model.bipedRightArm).rotation.setSmoothZ(10,0.3f);
				((ModelPart)model.bipedLeftArm).rotation.setSmoothZ(-10,0.3f);
				
				if(data.getSprintJumpLeg()){
					((ModelPart)model.bipedRightLeg).rotation.setSmoothX(-45,0.4f);
					((ModelPart)model.bipedLeftLeg).rotation.setSmoothX(45,0.4f);
					((ModelPart)model.bipedRightArm).rotation.setSmoothX(50,0.3f);
					((ModelPart)model.bipedLeftArm).rotation.setSmoothX(-50,0.3f);
					bodyRot = 20;
				}else{
					((ModelPart)model.bipedRightLeg).rotation.setSmoothX(45,0.4f);
					((ModelPart)model.bipedLeftLeg).rotation.setSmoothX(-45,0.4f);
					((ModelPart)model.bipedRightArm).rotation.setSmoothX(-50,0.3f);
					((ModelPart)model.bipedLeftArm).rotation.setSmoothX(50,0.3f);
					bodyRot = -20;
				}
				
				((ModelPart)model.bipedBody).rotation.setSmoothY(bodyRot,0.3f);
				
				((ModelPart)model.bipedHead).rotation.setY(model.headRotationY-bodyRot);
				((ModelPart)model.bipedHead).rotation.setX(model.headRotationX-20);
				
				float var = model.bipedRightLeg.rotateAngleX;
				((ModelPart)model.bipedLeftForeLeg).rotation.setSmoothX( (var < 0 ? 45 : 2), 0.3f);
				((ModelPart)model.bipedRightForeLeg).rotation.setSmoothX( (var < 0 ? 2 : 45), 0.3f);
			}else{
				((ModelPart)model.bipedRightLeg).rotation.setSmoothX(-5.0f+0.5f*(float) ((MathHelper.cos(model.armSwing * 0.6662F) * 1.4F * model.armSwingAmount) / Math.PI * 180.0f),1.0f);
				((ModelPart)model.bipedLeftLeg).rotation.setSmoothX(-5.0f+0.5f*(float) ((MathHelper.cos(model.armSwing * 0.6662F + (float)Math.PI) * 1.4F * model.armSwingAmount) / Math.PI * 180.0f),1.0f);
				
				float var = (float) ((float) (model.armSwing * 0.6662F)/Math.PI)%2;
				((ModelPart)model.bipedLeftForeLeg).rotation.setSmoothX( (var > 1 ? 45 : 0), 0.3f);
				((ModelPart)model.bipedRightForeLeg).rotation.setSmoothX( (var > 1 ? 0 : 45), 0.3f);
				((ModelPart)model.bipedLeftForeArm).rotation.setSmoothX( ((float) (Math.cos(model.armSwing * 0.6662F + Math.PI/2)+1.0f)/2.0f)*-20, 1.0f);
				((ModelPart)model.bipedRightForeArm).rotation.setSmoothX( ((float) (Math.cos(model.armSwing * 0.6662F)+1.0f)/2.0f)*-20, 0.3f);
			}
		}
	}
}
