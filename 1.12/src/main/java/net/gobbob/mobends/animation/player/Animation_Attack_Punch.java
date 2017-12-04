package net.gobbob.mobends.animation.player;

import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.data.Data_Player;
import net.minecraft.entity.player.EntityPlayer;

public class Animation_Attack_Punch{
	
	public static void animate(EntityPlayer player,ModelBendsPlayer model,Data_Player data){
		if(data.motion.x == 0 & data.motion.z == 0){
			model.renderRotation.setSmoothY(20.0f);
			model.renderOffset.setSmoothY(-2.0f);
			
			((ModelPart)model.bipedRightArm).rotation.setSmoothX(-90,0.3f);
			((ModelPart)model.bipedRightForeArm).rotation.setSmoothX(-80,0.3f);
			
			((ModelPart)model.bipedLeftArm).rotation.setSmoothX(-90,0.3f);
			((ModelPart)model.bipedLeftForeArm).rotation.setSmoothX(-80,0.3f);
			
			((ModelPart)model.bipedRightArm).rotation.setSmoothZ(20,0.3f);
			((ModelPart)model.bipedLeftArm).rotation.setSmoothZ(-20,0.3f);
			
			((ModelPart)model.bipedBody).rotation.setSmoothX(10,0.3f);
			
			((ModelPart)model.bipedRightLeg).rotation.setSmoothX(-30,0.3f);
			((ModelPart)model.bipedLeftLeg).rotation.setSmoothX(-30,0.3f);
			((ModelPart)model.bipedLeftLeg).rotation.setSmoothY(-25,0.3f);
			((ModelPart)model.bipedRightLeg).rotation.setSmoothZ(10);
			((ModelPart)model.bipedLeftLeg).rotation.setSmoothZ(-10);
			
			((ModelPart)model.bipedRightForeLeg).rotation.setSmoothX(30,0.3f);
			((ModelPart)model.bipedLeftForeLeg).rotation.setSmoothX(30,0.3f);
			
			if(data.getFistPunchArm()){
				((ModelPart)model.bipedRightArm).pre_rotation.setSmoothZ(100,0.9f);
				
				((ModelPart)model.bipedRightArm).rotation.setSmoothX(-90,0.9f);
				((ModelPart)model.bipedRightArm).pre_rotation.setSmoothX(model.headRotationX,0.9f);
				((ModelPart)model.bipedRightForeArm).rotation.setSmoothX(0,0.9f);
				
				/*model.leftArm.rotation.setSmoothX(-90,0.3f);
				model.leftForeArm.rotation.setSmoothX(0,0.3f);
				*/
				((ModelPart)model.bipedBody).rotation.setSmoothY(-20.0f,0.6f);
				
				((ModelPart)model.bipedHead).rotation.setY(model.headRotationY-((ModelPart)model.bipedBody).rotation.getY()-20);
				((ModelPart)model.bipedHead).rotation.setX(model.headRotationX-10);
			}else{
				((ModelPart)model.bipedLeftArm).pre_rotation.setSmoothZ(-100,0.9f);
				((ModelPart)model.bipedLeftArm).pre_rotation.setSmoothY(30,0.9f);
				
				((ModelPart)model.bipedLeftArm).rotation.setSmoothX(-90,0.9f);
				((ModelPart)model.bipedLeftArm).pre_rotation.setSmoothX(model.headRotationX,0.9f);
				((ModelPart)model.bipedLeftForeArm).rotation.setSmoothX(0,0.9f);
				
				((ModelPart)model.bipedBody).rotation.setSmoothY(20.0f,0.6f);
				
				((ModelPart)model.bipedHead).rotation.setY(model.headRotationY-20-((ModelPart)model.bipedBody).rotation.getY());
				((ModelPart)model.bipedHead).rotation.setX(model.headRotationX-10);
			}
		}else{
			((ModelPart)model.bipedRightArm).rotation.setSmoothX(-90,0.3f);
			((ModelPart)model.bipedRightForeArm).rotation.setSmoothX(-80,0.3f);
			
			((ModelPart)model.bipedLeftArm).rotation.setSmoothX(-90,0.3f);
			((ModelPart)model.bipedLeftForeArm).rotation.setSmoothX(-80,0.3f);
			
			((ModelPart)model.bipedRightArm).rotation.setSmoothZ(20,0.3f);
			((ModelPart)model.bipedLeftArm).rotation.setSmoothZ(-20,0.3f);
			
			((ModelPart)model.bipedBody).rotation.setSmoothX(10,0.3f);
			
			((ModelPart)model.bipedBody).rotation.setSmoothY(0,0.3f);
			
			if(data.getFistPunchArm()){
				((ModelPart)model.bipedRightArm).pre_rotation.setSmoothZ(100,0.9f);
				
				((ModelPart)model.bipedRightArm).rotation.setSmoothX(-90,0.9f);
				((ModelPart)model.bipedRightArm).pre_rotation.setSmoothX(-20.0f+model.headRotationX,0.9f);
				((ModelPart)model.bipedRightForeArm).rotation.setSmoothX(0,0.9f);
				
				/*model.leftArm.rotation.setSmoothX(-90,0.3f);
				model.leftForeArm.rotation.setSmoothX(0,0.3f);
				*/
				((ModelPart)model.bipedBody).rotation.setSmoothY(-20.0f,0.6f);
				
				((ModelPart)model.bipedHead).rotation.setY(model.headRotationY+20);
				((ModelPart)model.bipedHead).rotation.setX(model.headRotationX-10);
			}else{
				((ModelPart)model.bipedLeftArm).pre_rotation.setSmoothZ(-100,0.9f);
				((ModelPart)model.bipedLeftArm).pre_rotation.setSmoothY(-15,0.9f);
				
				((ModelPart)model.bipedLeftArm).rotation.setSmoothX(-90,0.9f);
				((ModelPart)model.bipedLeftArm).pre_rotation.setSmoothX(-20.0f+model.headRotationX,0.9f);
				((ModelPart)model.bipedLeftForeArm).rotation.setSmoothX(0,0.9f);
				
				((ModelPart)model.bipedBody).rotation.setSmoothY(20.0f,0.6f);
				
				((ModelPart)model.bipedHead).rotation.setY(model.headRotationY-20);
				((ModelPart)model.bipedHead).rotation.setX(model.headRotationX-10);
			}
		}
	}
}
