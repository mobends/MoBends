package net.gobbob.mobends.animation.player;

import net.gobbob.mobends.animation.Animation;
import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.data.Data_Player;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.util.GUtil;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

public class Animation_Swimming extends Animation{
	public String getName(){
		return "swimming";
	}

	@Override
	public void animate(EntityLivingBase argEntity, ModelBase argModel, EntityData argData) {
		ModelBendsPlayer model = (ModelBendsPlayer) argModel;
		Data_Player data = (Data_Player) argData;
		
		float armSway = (MathHelper.cos(data.ticks*0.1625f)+1.0f)/2.0f;
		float armSway2 = (-MathHelper.sin(data.ticks*0.1625f)+1.0f)/2.0f;
		float legFlap = MathHelper.cos(data.ticks*0.4625f);
		float foreArmSway = ((float) ((data.ticks*0.1625f)%(Math.PI*2)))/((float)Math.PI*2);
		float foreArmStretch = (armSway)*2.0f;
		foreArmStretch-=1.0f;
		foreArmStretch = GUtil.min(foreArmStretch, 0.0f);
		
		if(data.motion.x == 0 & data.motion.z == 0){
			armSway = (MathHelper.cos(data.ticks*0.0825f)+1.0f)/2.0f;
			armSway2 = (-MathHelper.sin(data.ticks*0.0825f)+1.0f)/2.0f;
			legFlap = MathHelper.cos(data.ticks*0.2625f);
			
			((ModelRendererBends)model.bipedHead).pre_rotation.setSmoothX(0,0.3f);
			
			((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothX(armSway2*30-15,0.3f);
			((ModelRendererBends)model.bipedRightArm).rotation.setSmoothX(armSway2*30-15,0.3f);
			
			((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothZ(-armSway*30);
			((ModelRendererBends)model.bipedRightArm).rotation.setSmoothZ(armSway*30);
			
			((ModelRendererBends)model.bipedLeftForeArm).rotation.setSmoothX(armSway2*-40,0.3f);
			((ModelRendererBends)model.bipedRightForeArm).rotation.setSmoothX(armSway2*-40,0.3f);
			
			((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothX((legFlap)*40,0.3f);
			((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothX((-legFlap)*40,0.3f);
			
			((ModelRendererBends)model.bipedLeftForeLeg).rotation.setSmoothX(5,0.4f);
			((ModelRendererBends)model.bipedRightForeLeg).rotation.setSmoothX(5,0.4f);
			
			((ModelRendererBends)model.bipedBody).rotation.setSmoothX(armSway*10);
			
			((ModelRendererBends)model.bipedHead).rotation.setSmoothX(model.headRotationX);
			((ModelRendererBends)model.bipedHead).rotation.setSmoothY(model.headRotationY);
		}else{
			((ModelRendererBends)model.bipedHead).pre_rotation.setSmoothX(-70-armSway*-20,0.3f);
			model.renderRotation.setSmoothX(70.0f,0.3f);
			model.renderOffset.setSmoothZ(10,0.3f);
			
			((ModelRendererBends)model.bipedLeftArm).pre_rotation.setSmoothY(90,0.3f);
			((ModelRendererBends)model.bipedRightArm).pre_rotation.setSmoothY(-90,0.3f);
			
			((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothX(armSway*-120 - 45,0.3f);
			((ModelRendererBends)model.bipedRightArm).rotation.setSmoothX(armSway*-120 - 45,0.3f);
			
			((ModelRendererBends)model.bipedLeftArm).pre_rotation.setSmoothZ((armSway*-20));
			((ModelRendererBends)model.bipedRightArm).pre_rotation.setSmoothZ(-(armSway*-20));
			
			((ModelRendererBends)model.bipedLeftForeArm).rotation.setSmoothX((foreArmSway < 0.55f | foreArmSway > 0.9) ? foreArmStretch*-60.0f : -60,0.3f);
			((ModelRendererBends)model.bipedRightForeArm).rotation.setSmoothX((foreArmSway < 0.55f | foreArmSway > 0.9) ? foreArmStretch*-60.0f : -60,0.3f);
			
			((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothX((legFlap)*40,0.3f);
			((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothX((-legFlap)*40,0.3f);
			
			((ModelRendererBends)model.bipedLeftForeLeg).rotation.setSmoothX(5,0.4f);
			((ModelRendererBends)model.bipedRightForeLeg).rotation.setSmoothX(5,0.4f);
			
			((ModelRendererBends)model.bipedBody).rotation.setSmoothX(armSway*-20);
			
			((ModelRendererBends)model.bipedHead).rotation.setSmoothX(model.headRotationX);
			((ModelRendererBends)model.bipedHead).rotation.setSmoothY(model.headRotationY);
			
			model.renderItemRotation.setSmoothX(armSway*120,0.3f);
		}
	}
}
