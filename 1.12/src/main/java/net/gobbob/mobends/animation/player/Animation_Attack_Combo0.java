package net.gobbob.mobends.animation.player;

import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.data.Data_Player;
import net.gobbob.mobends.util.GUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;

import org.lwjgl.util.vector.Vector3f;

public class Animation_Attack_Combo0 {
	public static void animate(EntityPlayer player,ModelBendsPlayer model,Data_Player data){
		boolean mainHandSwitch = model.primaryHand == EnumHandSide.RIGHT;
		float handDirMtp = mainHandSwitch ? 1 : -1; // Main Hand Direction Multiplier - it helps switch animation sides depending on what is your main hand.
		ModelRendererBends mainArmBox = mainHandSwitch ? ((ModelRendererBends) model.bipedRightArm) : ((ModelRendererBends) model.bipedLeftArm);
		ModelRendererBends offArmBox = mainHandSwitch ? ((ModelRendererBends) model.bipedLeftArm) : ((ModelRendererBends) model.bipedRightArm);
		ModelRendererBends mainForeArmBox = mainHandSwitch ? model.bipedRightForeArm : model.bipedLeftForeArm;
		ModelRendererBends offForeArmBox = mainHandSwitch ? model.bipedLeftForeArm : model.bipedRightForeArm;
		
		ItemStack offHandItemStack = player.getHeldItemOffhand();
		
		if(data.ticksAfterPunch < 0.5f){
			model.swordTrail.reset();
		}
		
		if(player.getHeldItem(EnumHand.MAIN_HAND) != null){
			if(data.ticksAfterPunch < 4f & player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemSword){
				model.swordTrail.add(model);
			}
		}
		
		float attackState = data.ticksAfterPunch/10.0f;
		float armSwing = attackState*3.0f;
		armSwing = Math.min(armSwing, 1.0f);
		
		if(!player.isRiding()){
			model.renderRotation.setSmoothY(30*handDirMtp,0.7f);
		}
		
		Vector3f bodyRot = new Vector3f(0,0,0);
		
		bodyRot.x = 20.0f-armSwing*20.0f;
		bodyRot.y = -90.0f*armSwing*handDirMtp;
		
		((ModelRendererBends) model.bipedBody).rotation.setSmooth(bodyRot,0.9f);
		((ModelRendererBends) model.bipedHead).rotation.setY(model.headRotationY);
		((ModelRendererBends) model.bipedHead).rotation.setX(model.headRotationX);
		((ModelRendererBends)model.bipedHead).pre_rotation.setSmoothX(-model.bipedBody.rotateAngleX,0.9f);
		((ModelRendererBends)model.bipedHead).pre_rotation.setSmoothY(-model.bipedBody.rotateAngleY-30*handDirMtp,0.9f);
		
		mainArmBox.pre_rotation.setSmoothZ(60.0f*handDirMtp,0.3f);
		
		mainArmBox.rotation.setSmoothX(-20+armSwing*100,3.0f);
		mainArmBox.rotation.setSmoothX(60.0f-armSwing*180,3.0f);
		mainArmBox.rotation.setSmoothY(0.0f,0.9f);
		mainArmBox.rotation.setSmoothZ(0.0f,0.9f);
		offArmBox.rotation.setSmoothZ(20*handDirMtp,0.3f);
		offArmBox.pre_rotation.setSmoothZ(-80*handDirMtp,0.3f);
		
		if(offHandItemStack != null && offHandItemStack.getItem().getItemUseAction(offHandItemStack) == EnumAction.BLOCK){
			offArmBox.pre_rotation.setSmoothZ(-40*handDirMtp,0.3f);
		}
		
		mainForeArmBox.rotation.setSmoothX(-20,0.3f);
		offForeArmBox.rotation.setSmoothX(-60,0.3f);
		
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
			((ModelRendererBends) model.bipedBody).rotation.setSmoothY(-70.0f*armSwing*handDirMtp,0.9f);
		}
		
		if(mainHandSwitch){
			model.renderRightItemRotation.setSmoothX(180, 0.9f);
		}else{
			model.renderLeftItemRotation.setSmoothX(180, 0.9f);
		}
	}
}
