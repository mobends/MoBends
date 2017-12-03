package net.gobbob.mobends.animation.player;

import net.gobbob.mobends.animation.Animation;
import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.data.Data_Player;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

public class Animation_Riding extends Animation{
	public String getName(){
		return "riding";
	}

	@Override
	public void animate(EntityLivingBase argEntity, ModelBase argModel, EntityData argData) {
		ModelBendsPlayer model = (ModelBendsPlayer) argModel;
		Data_Player data = (Data_Player) argData;
		EntityPlayer player = (EntityPlayer) argEntity;
		
		model.renderOffset.setSmoothY(1.5f,0.3f);
		
		((ModelRendererBends)model.bipedBody).rotation.setSmoothY(0,0.3f);
		((ModelRendererBends)model.bipedBody).rotation.setSmoothZ(0.0f, 0.3f);
		
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothX(-85.0f,0.3f);
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothY(45,0.3f);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothX(-85.0f,0.3f);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothY(-45,0.3f);
		
		((ModelRendererBends)model.bipedRightForeLeg).rotation.setSmoothX(60.0f);
		((ModelRendererBends)model.bipedLeftForeLeg).rotation.setSmoothX(60.0f);
		
		if(argData.motion.x == 0 & argData.motion.z == 0){
			((ModelRendererBends)model.bipedRightArm).rotation.setSmoothX(-10.0f,0.3f);
			((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothX(-10.0f,0.3f);
			
			((ModelRendererBends)model.bipedRightArm).rotation.setSmoothY(-10.0f,0.3f);
			((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothY(10.0f,0.3f);
			
			((ModelRendererBends)model.bipedBody).rotation.setSmoothX(0,0.3f);
			
			((ModelRendererBends)model.bipedRightForeArm).rotation.setSmoothX(-20.0f,0.3f);
			((ModelRendererBends)model.bipedLeftForeArm).rotation.setSmoothX(-20.0f,0.3f);
		}else{
			float jiggle = MathHelper.cos(player.ticksExisted*0.6f)*model.armSwingAmount;
			float jiggle_hard = MathHelper.cos(player.ticksExisted*0.3f)*model.armSwingAmount;
			if(jiggle_hard < 0) jiggle_hard = -jiggle_hard;
			model.renderOffset.setSmoothY(1.5f+jiggle_hard*20,0.7f);
			
			((ModelRendererBends)model.bipedBody).rotation.setSmoothX(40+jiggle*300,0.3f);
			
			((ModelRendererBends)model.bipedRightArm).rotation.setSmoothX(-45.0f,0.3f);
			((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothX(-45.0f,0.3f);
			
			((ModelRendererBends)model.bipedRightArm).rotation.setSmoothY(-10.0f,0.3f);
			((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothY(10.0f,0.3f);
			
			((ModelRendererBends)model.bipedRightForeArm).rotation.setSmoothX(-30.0f,0.3f);
			((ModelRendererBends)model.bipedLeftForeArm).rotation.setSmoothX(-30.0f,0.3f);
		}
		
		((ModelRendererBends)model.bipedHead).rotation.setSmoothY(model.headRotationY,0.3f);
		((ModelRendererBends)model.bipedHead).rotation.setSmoothX(model.headRotationX-(model.bipedBody.rotateAngleX/(float)Math.PI*180.0f),0.3f);
	}
}
