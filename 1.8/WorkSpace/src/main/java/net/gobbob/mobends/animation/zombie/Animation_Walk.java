package net.gobbob.mobends.animation.zombie;

import net.gobbob.mobends.animation.Animation;
import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.entity.ModelBendsZombie;
import net.gobbob.mobends.data.Data_Zombie;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.MathHelper;

public class Animation_Walk extends Animation{
	public String getName(){
		return "walk";
	}

	@Override
	public void animate(EntityLivingBase argEntity, ModelBase argModel, EntityData argData) {
		EntityZombie zombie = (EntityZombie) argEntity;
		ModelBendsZombie model = (ModelBendsZombie) argModel;
		Data_Zombie data = (Data_Zombie) argData;
		
		model.renderOffset.setSmoothY(-3.0f);
		
		float var2 = 30.0f+(MathHelper.cos((model.armSwing * 0.6662F)*2)*10.0f);
		((ModelRendererBends)model.bipedBody).rotation.setSmoothX(var2,0.3f);

		((ModelRendererBends)model.bipedRightArm).rotation.setSmoothX(0.9f*(float) ((MathHelper.cos(model.armSwing * 0.6662F + (float)Math.PI) * 2.0F * model.armSwingAmount * 0.5F ) / Math.PI * 180.0f));
		((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothX(0.9f*(float) ((MathHelper.cos(model.armSwing * 0.6662F) * 2.0F * model.armSwingAmount * 0.5F) / Math.PI * 180.0f));
		
		((ModelRendererBends)model.bipedRightArm).rotation.setSmoothZ(5,0.3f);
		((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothZ(-5,0.3f);
		
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothX(-5.0f+0.9f*(float) ((MathHelper.cos(model.armSwing * 0.6662F) * 1.4F * model.armSwingAmount) / Math.PI * 180.0f),1.0f);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothX(-5.0f+0.9f*(float) ((MathHelper.cos(model.armSwing * 0.6662F + (float)Math.PI) * 1.4F * model.armSwingAmount) / Math.PI * 180.0f),1.0f);
		
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothY(0.0f);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothY(0.0f);
		
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothZ(10,0.2f);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothZ(-10,0.2f);
		
		
		
		float var = (float) ((float) (model.armSwing * 0.6662F)/Math.PI)%2;
		((ModelRendererBends)model.bipedLeftForeLeg).rotation.setSmoothX( (var > 1 ? 45 : 0), 0.3f);
		((ModelRendererBends)model.bipedRightForeLeg).rotation.setSmoothX( (var > 1 ? 0 : 45), 0.3f);
		((ModelRendererBends)model.bipedLeftForeArm).rotation.setSmoothX( ((float) (Math.cos(model.armSwing * 0.6662F + Math.PI/2)+1.0f)/2.0f)*-20, 1.0f);
		((ModelRendererBends)model.bipedRightForeArm).rotation.setSmoothX( ((float) (Math.cos(model.armSwing * 0.6662F)+1.0f)/2.0f)*-20, 0.3f);
		
		float var1 = ((MathHelper.cos(model.armSwing * 0.6662F + (float)Math.PI)/(float)Math.PI*180.0f))*0.5f;
		((ModelRendererBends)model.bipedBody).rotation.setSmoothY(((MathHelper.cos(model.armSwing * 0.6662F + (float)Math.PI)/(float)Math.PI*180.0f))*0.5f,0.3f);
		
		if(data.currentWalkingState == 1){
			((ModelRendererBends)model.bipedRightArm).rotation.setSmoothX(-90-30.0f);
			((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothX(-90-30.0f);
		}
		
		((ModelRendererBends)model.bipedHead).rotation.setX(model.headRotationX-30);
		((ModelRendererBends)model.bipedHead).rotation.setY(model.headRotationY-var1);
	}
}
