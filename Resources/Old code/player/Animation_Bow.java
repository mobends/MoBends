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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;

public class Animation_Bow extends Animation{
	
	public String getName(){
		return "bow";
	}

	@Override
	public void animate(EntityLivingBase argEntity, IBendsModel argModel, EntityData argData) {
		ModelBendsPlayer model = (ModelBendsPlayer) argModel;
		DataPlayer data = (DataPlayer) argData;
		EntityPlayer player = (EntityPlayer) argEntity;
		
		boolean primaryHand = model.primaryHand == EnumHandSide.RIGHT;
		float directionMultiplier = model.rightArmPose == ArmPose.BOW_AND_ARROW ? 1.0f : -1.0f;
		ModelPart mainArmBox = model.rightArmPose == ArmPose.BOW_AND_ARROW ? ((ModelPart) model.bipedRightArm) : ((ModelPart) model.bipedLeftArm);
		ModelPart offArmBox = model.rightArmPose == ArmPose.BOW_AND_ARROW ? ((ModelPart) model.bipedLeftArm) : ((ModelPart) model.bipedRightArm);
		ModelPart mainForeArmBox = model.rightArmPose == ArmPose.BOW_AND_ARROW ? model.bipedRightForeArm : model.bipedLeftForeArm;
		ModelPart offForeArmBox = model.rightArmPose == ArmPose.BOW_AND_ARROW ? model.bipedLeftForeArm : model.bipedRightForeArm;
		
		ItemStack offHandItemStack = model.rightArmPose == ArmPose.BOW_AND_ARROW ? (primaryHand ? player.getHeldItemOffhand() : player.getHeldItemMainhand()) : (primaryHand ? player.getHeldItemMainhand() : player.getHeldItemOffhand());
		
		float aimedBowDuration = 0;
		
    	if(player != null){
    		aimedBowDuration = player.getItemInUseMaxCount();
    	}
    	
    	if(aimedBowDuration > 15){
    		aimedBowDuration = 15;
    	}
    	
    	if(aimedBowDuration < 10){
    		mainArmBox.pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f), 0.3f);
    		offArmBox.pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f), 0.3f);
    		mainArmBox.rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f), 0.3f);
    		offArmBox.rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f), 0.3f);
    		
    		((ModelPart)model.bipedBody).rotation.setSmoothX(30,0.3f);
    		((ModelPart)model.bipedBody).rotation.setSmoothY(0,0.3f);
    		
    		mainArmBox.rotation.setSmoothZ(0);
    		mainArmBox.rotation.setSmoothX(-30);
    		offArmBox.rotation.setSmoothX(-0-30);
    		
    		offArmBox.rotation.setSmoothY(80.0f * directionMultiplier);
    		
    		float var = (aimedBowDuration/10.0f);
    		offForeArmBox.rotation.setSmoothX(var*-50.0f);
    		
    		((ModelPart)model.bipedHead).rotation.setSmoothX(model.headRotationX-30,0.3f);
    	}else{
    		mainArmBox.pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f), 0.3f);
    		offArmBox.pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f), 0.3f);
    		mainArmBox.rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f), 0.3f);
    		offArmBox.rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f), 0.3f);
    		
    		float var1 = 20-(((aimedBowDuration-10))/5.0f)*20;
    		((ModelPart)model.bipedBody).rotation.setSmoothX(var1,0.3f);
    		float var = (((aimedBowDuration-10)/5.0f)*-25) * directionMultiplier;
    		((ModelPart)model.bipedBody).rotation.setSmoothY(var+model.headRotationY,0.3f);
    		
    		if(data.isClimbing()){
    			((ModelPart)model.bipedBody).rotation.setSmoothY(var+model.headRotationY*1.5f,0.3f);
    		}
    		
    		mainForeArmBox.rotation.setSmoothX(0.0f, 1.0f);
    		
    		mainArmBox.rotation.setSmoothX(-90-var1,0.3f);
    		offArmBox.rotation.setSmoothX(-0-30);
    		
    		offArmBox.rotation.setSmoothY(80.0f * directionMultiplier);
    		
    		float var2 = (aimedBowDuration/10.0f);
    		offForeArmBox.rotation.setSmoothX(var2*-30.0f);
    		
    		mainArmBox.pre_rotation.setSmoothY(var);
    		
    		float var5 = -90+model.headRotationX;
    		var5 = Math.max(var5, -120);
    		offArmBox.pre_rotation.setSmoothX(var5,0.3f);
    		
    		mainArmBox.rotation.setSmoothX(model.headRotationX-90.0f);
    		
    		((ModelPart)model.bipedHead).rotation.setY(-var);
    		((ModelPart)model.bipedHead).pre_rotation.setSmoothX(-var1,0.3f);
    		((ModelPart)model.bipedHead).rotation.setX(model.headRotationX);
    		
    		if(offHandItemStack != null && offHandItemStack.getItem().getItemUseAction(offHandItemStack) == EnumAction.BLOCK){
        		if(model.rightArmPose == ArmPose.BOW_AND_ARROW){
	        		model.renderLeftItemRotation.setSmooth(new Vector3f(45.0f,70.0f,40), 0.8f);
        		}else{
	        		model.renderRightItemRotation.setSmooth(new Vector3f(45.0f,-70.0f,-40), 0.8f);
        		}
    		}
    	}
	}
}
