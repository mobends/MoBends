package net.gobbob.mobends.animation.player;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.animation.Animation;
import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.model.entity.IBendsModel;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.data.Data_Player;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

public class Animation_Climbing extends Animation{
	public String getName(){
		return "climbing";
	}

	@Override
	public void animate(EntityLivingBase argEntity, IBendsModel argModel, EntityData argData) {
		EntityPlayer player = (EntityPlayer) argEntity;
		ModelBendsPlayer model = (ModelBendsPlayer) argModel;
		Data_Player data = (Data_Player) argData;
		
		float legAnimationOffset = (float)Math.PI;
		float progress = data.climbingCycle;
		float armSwingRight = (float) Math.sin(progress)*0.5f+0.5f;
		float armSwingLeft = (float) Math.sin(progress+Math.PI)*0.5f+0.5f;
		float armSwingRight2 = (float) Math.sin(progress-0.3f)*0.5f+0.5f;
		float armSwingLeft2 = (float) Math.sin(progress+Math.PI-0.3f)*0.5f+0.5f;
		float armSwingDouble = (float) Math.sin(progress*2)*0.5f+0.5f;
		float armSwingDouble2 = (float) Math.sin(progress*2-1.8f)*0.5f+0.5f;
		
		float legSwingRight = (float) Math.sin(progress+legAnimationOffset)*0.5f+0.5f;
		float legSwingLeft = (float) Math.sin(progress+legAnimationOffset+Math.PI)*0.5f+0.5f;
		float legSwingRight2 = (float) Math.sin(progress+legAnimationOffset+0.3f)*0.5f+0.5f;
		float legSwingLeft2 = (float) Math.sin(progress+legAnimationOffset+Math.PI+0.3f)*0.5f+0.5f;
		
		float armOrientX = -45.0f;
		
		model.renderRotation.set(0, -player.rotationYaw+MathHelper.wrapDegrees(model.headRotationY)+data.getClimbingRotation(), 0);
		model.renderOffset.setSmoothX(armSwingDouble2, 0.6f);
		
		((ModelPart)model.bipedBody).pre_rotation.setSmooth(new Vector3f(0.0f,0.0f,0.0f),0.5f);
		((ModelPart)model.bipedRightArm).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelPart)model.bipedLeftArm).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelPart)model.bipedRightForeArm).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelPart)model.bipedLeftForeArm).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelPart)model.bipedRightLeg).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelPart)model.bipedLeftLeg).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelPart)model.bipedRightForeLeg).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelPart)model.bipedLeftForeLeg).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelPart)model.bipedHead).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		
		((ModelPart)model.bipedBody).rotation.setSmooth(new Vector3f(armSwingDouble*10.0f,0.0f,0.0f),0.5f);
		((ModelPart)model.bipedRightArm).rotation.setSmooth(new Vector3f(-90.0F+armOrientX+armSwingRight*70.0f, 0.0F, 0.0F),0.5f);
		((ModelPart)model.bipedLeftArm).rotation.setSmooth(new Vector3f(-90.0F+armOrientX+armSwingLeft*70.0f, 0.0F, 0.0f), 0.5f);
		((ModelPart)model.bipedRightForeArm).rotation.setSmooth(new Vector3f(armSwingRight2*-80.0f, 0.0F, 0.0F),0.5f);
		((ModelPart)model.bipedLeftForeArm).rotation.setSmooth(new Vector3f(armSwingLeft2*-80.0f, 0.0F, 0.0f), 0.5f);
		
		((ModelPart)model.bipedRightLeg).rotation.setSmooth(new Vector3f(-45.0f-legSwingRight*50.0f, 0.0F, 0.0F),0.5f);
		((ModelPart)model.bipedLeftLeg).rotation.setSmooth(new Vector3f(-45.0f-legSwingLeft*50.0f, 0.0F, 0.0f), 0.5f);
		((ModelPart)model.bipedRightForeLeg).rotation.setSmooth(new Vector3f(20.0f+legSwingRight2*90.0f, 0.0F, 0.0F),0.5f);
		((ModelPart)model.bipedLeftForeLeg).rotation.setSmooth(new Vector3f(20.0f+legSwingLeft2*90.0f, 0.0F, 0.0f), 0.5f);
		((ModelPart)model.bipedHead).rotation.set(model.headRotationX, model.headRotationY, 0);
		
		float ledgeClimbStart = 0.6f;
		if(data.getLedgeHeight() >= ledgeClimbStart) {
        	float armRotX = data.getLedgeHeight()-ledgeClimbStart;
        	((ModelPart)model.bipedBody).rotation.setSmooth(new Vector3f(armRotX*50.0f, 0, 0), 0.5f);
        	
        	((ModelPart)model.bipedRightArm).rotation.setSmooth(new Vector3f(-100.0f+armRotX*40.0f, 0.0F, 0.0F),0.5f);
    		((ModelPart)model.bipedLeftArm).rotation.setSmooth(new Vector3f(-100.0f+armRotX*40.0f, 0.0F, 0.0f), 0.5f);
    		((ModelPart)model.bipedRightForeArm).rotation.setSmooth(new Vector3f(-10.0f, 0.0F, 0.0F),0.5f);
    		((ModelPart)model.bipedLeftForeArm).rotation.setSmooth(new Vector3f(-10.0f, 0.0F, 0.0f), 0.5f);
		}
	}
}
