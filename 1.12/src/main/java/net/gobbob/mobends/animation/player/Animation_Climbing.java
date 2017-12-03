package net.gobbob.mobends.animation.player;

import net.gobbob.mobends.animation.Animation;
import net.gobbob.mobends.client.event.EventHandlerRenderPlayer;
import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.data.Data_Player;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

import org.lwjgl.util.vector.Vector3f;

public class Animation_Climbing extends Animation{
	public String getName(){
		return "climbing";
	}

	@Override
	public void animate(EntityLivingBase argEntity, ModelBase argModel, EntityData argData) {
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
		
		((ModelRendererBends)model.bipedBody).pre_rotation.setSmooth(new Vector3f(0.0f,0.0f,0.0f),0.5f);
		((ModelRendererBends)model.bipedRightArm).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelRendererBends)model.bipedLeftArm).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelRendererBends)model.bipedRightForeArm).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelRendererBends)model.bipedLeftForeArm).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelRendererBends)model.bipedRightLeg).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelRendererBends)model.bipedLeftLeg).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelRendererBends)model.bipedRightForeLeg).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelRendererBends)model.bipedLeftForeLeg).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelRendererBends)model.bipedHead).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		
		((ModelRendererBends)model.bipedBody).rotation.setSmooth(new Vector3f(armSwingDouble*10.0f,0.0f,0.0f),0.5f);
		((ModelRendererBends)model.bipedRightArm).rotation.setSmooth(new Vector3f(-90.0F+armOrientX+armSwingRight*70.0f, 0.0F, 0.0F),0.5f);
		((ModelRendererBends)model.bipedLeftArm).rotation.setSmooth(new Vector3f(-90.0F+armOrientX+armSwingLeft*70.0f, 0.0F, 0.0f), 0.5f);
		((ModelRendererBends)model.bipedRightForeArm).rotation.setSmooth(new Vector3f(armSwingRight2*-80.0f, 0.0F, 0.0F),0.5f);
		((ModelRendererBends)model.bipedLeftForeArm).rotation.setSmooth(new Vector3f(armSwingLeft2*-80.0f, 0.0F, 0.0f), 0.5f);
		
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmooth(new Vector3f(-45.0f-legSwingRight*50.0f, 0.0F, 0.0F),0.5f);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmooth(new Vector3f(-45.0f-legSwingLeft*50.0f, 0.0F, 0.0f), 0.5f);
		((ModelRendererBends)model.bipedRightForeLeg).rotation.setSmooth(new Vector3f(20.0f+legSwingRight2*90.0f, 0.0F, 0.0F),0.5f);
		((ModelRendererBends)model.bipedLeftForeLeg).rotation.setSmooth(new Vector3f(20.0f+legSwingLeft2*90.0f, 0.0F, 0.0f), 0.5f);
		((ModelRendererBends)model.bipedHead).rotation.set(model.headRotationX, model.headRotationY, 0);
		
		float ledgeClimbStart = 0.6f;
		if(data.getLedgeHeight() >= ledgeClimbStart) {
        	float armRotX = data.getLedgeHeight()-ledgeClimbStart;
        	((ModelRendererBends)model.bipedBody).rotation.setSmooth(new Vector3f(armRotX*50.0f, 0, 0), 0.5f);
        	
        	((ModelRendererBends)model.bipedRightArm).rotation.setSmooth(new Vector3f(-100.0f+armRotX*40.0f, 0.0F, 0.0F),0.5f);
    		((ModelRendererBends)model.bipedLeftArm).rotation.setSmooth(new Vector3f(-100.0f+armRotX*40.0f, 0.0F, 0.0f), 0.5f);
    		((ModelRendererBends)model.bipedRightForeArm).rotation.setSmooth(new Vector3f(-10.0f, 0.0F, 0.0F),0.5f);
    		((ModelRendererBends)model.bipedLeftForeArm).rotation.setSmooth(new Vector3f(-10.0f, 0.0F, 0.0f), 0.5f);
		}
	}
}
