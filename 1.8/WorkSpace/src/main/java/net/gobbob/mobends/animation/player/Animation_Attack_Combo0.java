package net.gobbob.mobends.animation.player;

import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.data.Data_Player;
import net.gobbob.mobends.util.GUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;

import org.lwjgl.util.vector.Vector3f;

public class Animation_Attack_Combo0 {
	public static void animate(EntityPlayer player,ModelBendsPlayer model,Data_Player data){
		if(data.ticksAfterPunch < 0.5f){
			model.swordTrail.reset();
		}
		
		if(player.getCurrentEquippedItem() != null){
			if(data.ticksAfterPunch < 4f & player.getCurrentEquippedItem().getItem() instanceof ItemSword){
				model.swordTrail.add(model);
			}
		}
		
		float attackState = data.ticksAfterPunch/10.0f;
		float armSwing = attackState*3.0f;
		armSwing = GUtil.max(armSwing, 1.0f);
		
		if(!player.isRiding()){
			model.renderRotation.setSmoothY(30,0.7f);
		}
		
		Vector3f bodyRot = new Vector3f(0,0,0);
		
		bodyRot.x = 20.0f-armSwing*20.0f;
		bodyRot.y = -90.0f*armSwing;
		
		((ModelRendererBends) model.bipedBody).rotation.setSmooth(bodyRot,0.9f);
		((ModelRendererBends) model.bipedHead).rotation.setY(model.headRotationY);
		((ModelRendererBends) model.bipedHead).rotation.setX(model.headRotationX);
		((ModelRendererBends)model.bipedHead).pre_rotation.setSmoothX(-model.bipedBody.rotateAngleX,0.9f);
		((ModelRendererBends)model.bipedHead).pre_rotation.setSmoothY(-model.bipedBody.rotateAngleY-30,0.9f);
		
		((ModelRendererBends)model.bipedRightArm).pre_rotation.setSmoothZ(60.0f,0.3f);
		
		((ModelRendererBends) model.bipedRightArm).rotation.setSmoothX(-20+armSwing*100,3.0f);
		((ModelRendererBends) model.bipedRightArm).rotation.setSmoothX(60.0f-armSwing*180,3.0f);
		((ModelRendererBends) model.bipedRightArm).rotation.setSmoothY(0.0f,0.9f);
		((ModelRendererBends) model.bipedRightArm).rotation.setSmoothZ(0.0f,0.9f);
		((ModelRendererBends) model.bipedLeftArm).rotation.setSmoothZ(20,0.3f);
		((ModelRendererBends) model.bipedLeftArm).pre_rotation.setSmoothZ(-80,0.3f);
		
		((ModelRendererBends) model.bipedRightForeArm).rotation.setSmoothX(-20,0.3f);
		((ModelRendererBends) model.bipedLeftForeArm).rotation.setSmoothX(-60,0.3f);
		
		if(data.motion.x == 0 & data.motion.z == 0){
			((ModelRendererBends) model.bipedRightLeg).rotation.setSmoothX(-30,0.3f);
			((ModelRendererBends) model.bipedLeftLeg).rotation.setSmoothX(-30,0.3f);
			((ModelRendererBends) model.bipedRightLeg).rotation.setSmoothY(0,0.3f);
			((ModelRendererBends) model.bipedLeftLeg).rotation.setSmoothY(-25,0.3f);
			((ModelRendererBends) model.bipedRightLeg).rotation.setSmoothZ(10);
			((ModelRendererBends) model.bipedLeftLeg).rotation.setSmoothZ(-10);
			
			((ModelRendererBends) model.bipedRightForeLeg).rotation.setSmoothX(30,0.3f);
			((ModelRendererBends) model.bipedLeftForeLeg).rotation.setSmoothX(30,0.3f);
			
			if(!player.isRiding()){
				model.renderOffset.setSmoothY(-2.0f);
			}
		}else{
			((ModelRendererBends) model.bipedBody).rotation.setSmoothY(-70.0f*armSwing,0.9f);
		}
		
		model.renderItemRotation.setSmoothX(180, 0.9f);
	}
}
