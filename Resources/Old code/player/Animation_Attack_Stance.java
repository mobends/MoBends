package net.gobbob.mobends.animation.player;

import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.data.DataPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped.ArmPose;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;

import org.lwjgl.util.vector.Vector3f;

public class Animation_Attack_Stance {
	public static void animate(EntityPlayer player,ModelBendsPlayer model,DataPlayer data){
		boolean mainHandSwitch = model.primaryHand == EnumHandSide.RIGHT;
		float handDirMtp = mainHandSwitch ? 1 : -1; // Main Hand Direction Multiplier - it helps switch animation sides depending on what is your main hand.
		ModelPart mainArmBox = mainHandSwitch ? ((ModelPart) model.bipedRightArm) : ((ModelPart) model.bipedLeftArm);
		ModelPart offArmBox = mainHandSwitch ? ((ModelPart) model.bipedLeftArm) : ((ModelPart) model.bipedRightArm);
		ModelPart mainForeArmBox = mainHandSwitch ? model.bipedRightForeArm : model.bipedLeftForeArm;
		ModelPart offForeArmBox = mainHandSwitch ? model.bipedLeftForeArm : model.bipedRightForeArm;
		
		ItemStack offHandItemStack = player.getHeldItemOffhand();
		
		if(!data.isOnGround()){
			return;
		}
		
		//model.swordTrail.reset();
		
		if(data.motion.x == 0 & data.motion.z == 0){
			model.renderRotation.setSmoothY(30*handDirMtp,0.3f);
			
			Vector3f bodyRot = new Vector3f(0,0,0);
			
			bodyRot.x = 20.0f;
			
			((ModelPart)model.bipedBody).rotation.setSmooth(bodyRot,0.3f);
			((ModelPart)model.bipedHead).rotation.setY(model.headRotationY-30*handDirMtp);
			((ModelPart)model.bipedHead).rotation.setX(model.headRotationX);
			((ModelPart)model.bipedHead).pre_rotation.setSmoothX(-bodyRot.x,0.3f);
			((ModelPart)model.bipedHead).pre_rotation.setSmoothY(-bodyRot.y,0.3f);
			
			((ModelPart)model.bipedRightLeg).rotation.setSmoothX(-30,0.3f);
			((ModelPart)model.bipedLeftLeg).rotation.setSmoothX(-30,0.3f);
			((ModelPart)model.bipedLeftLeg).rotation.setSmoothY(-25,0.3f);
			((ModelPart)model.bipedRightLeg).rotation.setSmoothZ(10);
			((ModelPart)model.bipedLeftLeg).rotation.setSmoothZ(-10);
			
			((ModelPart)model.bipedRightForeLeg).rotation.setSmoothX(30,0.3f);
			((ModelPart)model.bipedLeftForeLeg).rotation.setSmoothX(30,0.3f);
			
			mainArmBox.pre_rotation.setSmoothZ(60.0f*handDirMtp,0.3f);
			mainArmBox.rotation.setSmoothX(60.0f,0.3f);
			offArmBox.rotation.setSmoothZ(20*handDirMtp,0.3f);
			offArmBox.pre_rotation.setSmoothZ(-80*handDirMtp,0.3f);
			
			if(offHandItemStack != null && offHandItemStack.getItem().getItemUseAction(offHandItemStack) == EnumAction.BLOCK){
				offArmBox.pre_rotation.setSmoothZ(-40*handDirMtp,0.3f);
			}
			
			mainForeArmBox.rotation.setSmoothX(-20,0.3f);
			offForeArmBox.rotation.setSmoothX(-60,0.3f);
			
			if(mainHandSwitch){
				model.renderRightItemRotation.setSmoothX(65,0.3f);
			}else{
				model.renderLeftItemRotation.setSmoothX(65,0.3f);
			}
			model.renderOffset.setSmoothY(-2.0f);
		}else{
			if(player.isSprinting()){
				((ModelPart)model.bipedBody).rotation.setSmoothY(20,0.3f);
				
				((ModelPart)model.bipedHead).rotation.setY(model.headRotationY-20);
				((ModelPart)model.bipedHead).rotation.setX(model.headRotationX-15);
				
				((ModelPart)model.bipedRightLeg).rotation.setSmoothY(0.0f);
				((ModelPart)model.bipedLeftLeg).rotation.setSmoothY(0.0f);
				
				//((ModelRendererBends)model.bipedRightArm).pre_rotation.setSmoothZ(-10.0f,0.3f);
				mainArmBox.rotation.setSmoothX(60.0f,0.3f);
				
				if(mainHandSwitch){
					model.renderRightItemRotation.setSmoothX(90,0.3f);
				}else{
					model.renderLeftItemRotation.setSmoothX(90,0.3f);
				}
			}
		}
	}
}
