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

public class Animation_Stand extends Animation{
	
	public String getName(){
		return "stand";
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
		}else {
			model = (ModelBendsZombieVillager) argModel;
			modelRightForeArm = ((ModelBendsZombieVillager) model).bipedRightForeArm;
			modelLeftForeArm = ((ModelBendsZombieVillager) model).bipedLeftForeArm;
			modelRightForeLeg = ((ModelBendsZombieVillager) model).bipedRightForeLeg;
			modelLeftForeLeg = ((ModelBendsZombieVillager) model).bipedLeftForeLeg;
			renderOffset = ((ModelBendsZombieVillager) model).renderOffset;
			headRotationX = ((ModelBendsZombieVillager) model).headRotationX;
			headRotationY = ((ModelBendsZombieVillager) model).headRotationY;
		}
		
		renderOffset.setSmoothY(-3.0f);
		
		((ModelPart)model.bipedBody).rotation.setSmoothX(30.0f,0.3f);
		
		((ModelPart)model.bipedRightArm).rotation.setSmoothX(-30.0f,0.3f);
		((ModelPart)model.bipedLeftArm).rotation.setSmoothX(-30.0f,0.3f);
		
		((ModelPart)model.bipedRightLeg).rotation.setSmoothZ(10f,0.3f);
		((ModelPart)model.bipedLeftLeg).rotation.setSmoothZ(-10f,0.3f);
		
		((ModelPart)model.bipedRightLeg).rotation.setSmoothX(-20,0.3f);
		((ModelPart)model.bipedLeftLeg).rotation.setSmoothX(-20,0.3f);
		
		modelRightForeLeg.rotation.setSmoothX(25,0.3f);
		modelLeftForeLeg.rotation.setSmoothX(25,0.3f);
		
		((ModelPart)model.bipedHead).rotation.setX(headRotationX-30);
		((ModelPart)model.bipedHead).rotation.setY(headRotationY);
	}
}
