package net.gobbob.mobends.animation.zombie;

import net.gobbob.mobends.animation.Animation;
import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.model.entity.IBendsModel;
import net.gobbob.mobends.client.model.entity.ModelBendsZombie;
import net.gobbob.mobends.client.model.entity.ModelBendsZombieVillager;
import net.gobbob.mobends.data.Data_Zombie;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.math.MathHelper;

public class Animation_Walk extends Animation{
	public String getName(){
		return "walk";
	}

	@Override
	public void animate(EntityLivingBase argEntity, IBendsModel argModel, EntityData argData) {
		EntityZombie zombie = (EntityZombie) argEntity;
		Data_Zombie data = (Data_Zombie) argData;
		
		SmoothVector3f renderOffset;
		ModelPart modelRightForeArm;
		ModelPart modelLeftForeArm;
		ModelPart modelRightForeLeg;
		ModelPart modelLeftForeLeg;
		float headRotationX = 0;
		float headRotationY = 0;
		float armSwing = 0;
		float armSwingAmount = 0;
		
		ModelBiped model;
		if(argModel instanceof ModelBendsZombie) {
			model = (ModelBendsZombie) argModel;
			modelRightForeArm = ((ModelBendsZombie) model).bipedRightForeArm;
			modelLeftForeArm = ((ModelBendsZombie) model).bipedLeftForeArm;
			modelRightForeLeg = ((ModelBendsZombie) model).bipedRightForeLeg;
			modelLeftForeLeg = ((ModelBendsZombie) model).bipedLeftForeLeg;
			renderOffset = ((ModelBendsZombie) model).renderOffset;
			headRotationX = ((ModelBendsZombie) model).headRotationX;
			headRotationY = ((ModelBendsZombie) model).headRotationY;
			armSwing = ((ModelBendsZombie) model).armSwing;
			armSwingAmount = ((ModelBendsZombie) model).armSwingAmount;
		}else {
			model = (ModelBendsZombieVillager) argModel;
			modelRightForeArm = ((ModelBendsZombieVillager) model).bipedRightForeArm;
			modelLeftForeArm = ((ModelBendsZombieVillager) model).bipedLeftForeArm;
			modelRightForeLeg = ((ModelBendsZombieVillager) model).bipedRightForeLeg;
			modelLeftForeLeg = ((ModelBendsZombieVillager) model).bipedLeftForeLeg;
			renderOffset = ((ModelBendsZombieVillager) model).renderOffset;
			headRotationX = ((ModelBendsZombieVillager) model).headRotationX;
			headRotationY = ((ModelBendsZombieVillager) model).headRotationY;
			armSwing = ((ModelBendsZombieVillager) model).armSwing;
			armSwingAmount = ((ModelBendsZombieVillager) model).armSwingAmount;
		}
		
		renderOffset.setSmoothY(-3.0f);
		
		float var2 = 30.0f+(MathHelper.cos((armSwing * 0.6662F)*2)*10.0f);
		((ModelPart)model.bipedBody).rotation.setSmoothX(var2,0.3f);

		((ModelPart)model.bipedRightArm).rotation.setSmoothX(0.9f*(float) ((MathHelper.cos(armSwing * 0.6662F + (float)Math.PI) * 2.0F * armSwingAmount * 0.5F ) / Math.PI * 180.0f));
		((ModelPart)model.bipedLeftArm).rotation.setSmoothX(0.9f*(float) ((MathHelper.cos(armSwing * 0.6662F) * 2.0F * armSwingAmount * 0.5F) / Math.PI * 180.0f));
		
		((ModelPart)model.bipedRightArm).rotation.setSmoothZ(5,0.3f);
		((ModelPart)model.bipedLeftArm).rotation.setSmoothZ(-5,0.3f);
		
		((ModelPart)model.bipedRightLeg).rotation.setSmoothX(-5.0f+0.9f*(float) ((MathHelper.cos(armSwing * 0.6662F) * 1.4F * armSwingAmount) / Math.PI * 180.0f),1.0f);
		((ModelPart)model.bipedLeftLeg).rotation.setSmoothX(-5.0f+0.9f*(float) ((MathHelper.cos(armSwing * 0.6662F + (float)Math.PI) * 1.4F * armSwingAmount) / Math.PI * 180.0f),1.0f);
		
		((ModelPart)model.bipedRightLeg).rotation.setSmoothY(0.0f);
		((ModelPart)model.bipedLeftLeg).rotation.setSmoothY(0.0f);
		
		((ModelPart)model.bipedRightLeg).rotation.setSmoothZ(10,0.2f);
		((ModelPart)model.bipedLeftLeg).rotation.setSmoothZ(-10,0.2f);
		
		
		
		float var = (float) ((float) (armSwing * 0.6662F)/Math.PI)%2;
		modelLeftForeLeg.rotation.setSmoothX( (var > 1 ? 45 : 0), 0.3f);
		modelRightForeLeg.rotation.setSmoothX( (var > 1 ? 0 : 45), 0.3f);
		modelLeftForeArm.rotation.setSmoothX( ((float) (Math.cos(armSwing * 0.6662F + Math.PI/2)+1.0f)/2.0f)*-20, 1.0f);
		modelRightForeArm.rotation.setSmoothX( ((float) (Math.cos(armSwing * 0.6662F)+1.0f)/2.0f)*-20, 0.3f);
		
		float var1 = ((MathHelper.cos(armSwing * 0.6662F + (float)Math.PI)/(float)Math.PI*180.0f))*0.5f;
		((ModelPart)model.bipedBody).rotation.setSmoothY(((MathHelper.cos(armSwing * 0.6662F + (float)Math.PI)/(float)Math.PI*180.0f))*0.5f,0.3f);
		
		if(data.currentWalkingState == 1){
			((ModelPart)model.bipedRightArm).rotation.setSmoothX(-90-30.0f);
			((ModelPart)model.bipedLeftArm).rotation.setSmoothX(-90-30.0f);
		}
		
		((ModelPart)model.bipedHead).rotation.setX(headRotationX-30);
		((ModelPart)model.bipedHead).rotation.setY(headRotationY-var1);
	}
}
