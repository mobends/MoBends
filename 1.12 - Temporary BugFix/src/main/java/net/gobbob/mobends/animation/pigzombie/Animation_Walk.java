package net.gobbob.mobends.animation.pigzombie;

import net.gobbob.mobends.animation.Animation;
import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.entity.ModelBendsZombie;
import net.gobbob.mobends.client.model.entity.ModelBendsZombieVillager;
import net.gobbob.mobends.data.Data_PigZombie;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.util.math.MathHelper;

public class Animation_Walk extends Animation
{
	public String getName()
	{
		return "walk";
	}

	@Override
	public void animate(EntityLivingBase argEntity, ModelBase argModel, EntityData argData)
	{
		ModelBendsZombie model = (ModelBendsZombie) argModel;
		EntityPigZombie zombie = (EntityPigZombie) argEntity;
		Data_PigZombie data = (Data_PigZombie) argData;
		
		SmoothVector3f renderOffset;
		ModelRendererBends modelRightForeArm;
		ModelRendererBends modelLeftForeArm;
		ModelRendererBends modelRightForeLeg;
		ModelRendererBends modelLeftForeLeg;
		float headRotationX = 0;
		float headRotationY = 0;
		float armSwing = 0;
		float armSwingAmount = 0;
		
		modelRightForeArm = ((ModelBendsZombie) model).bipedRightForeArm;
		modelLeftForeArm = ((ModelBendsZombie) model).bipedLeftForeArm;
		modelRightForeLeg = ((ModelBendsZombie) model).bipedRightForeLeg;
		modelLeftForeLeg = ((ModelBendsZombie) model).bipedLeftForeLeg;
		renderOffset = ((ModelBendsZombie) model).renderOffset;
		headRotationX = ((ModelBendsZombie) model).headRotationX;
		headRotationY = ((ModelBendsZombie) model).headRotationY;
		armSwing = ((ModelBendsZombie) model).armSwing;
		armSwingAmount = ((ModelBendsZombie) model).armSwingAmount;
		
		renderOffset.setSmoothY(-3.0f);
		
		float var2 = 30.0f+(MathHelper.cos((armSwing * 0.6662F)*2)*10.0f);
		((ModelRendererBends)model.bipedBody).rotation.setSmoothX(var2,0.3f);

		((ModelRendererBends)model.bipedRightArm).rotation.setSmoothX(0.9f * (float) ((MathHelper.cos(armSwing * 0.6662F + (float)Math.PI) * 2.0F * armSwingAmount * 0.5F ) / Math.PI * 180.0f));
		((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothX(0.9f * (float) ((MathHelper.cos(armSwing * 0.6662F) * 2.0F * armSwingAmount * 0.5F) / Math.PI * 180.0f));
		
		((ModelRendererBends)model.bipedRightArm).rotation.setSmoothZ(5, 0.3f);
		((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothZ(-5, 0.3f);
		
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothX(-5.0f + 0.9f*(float) ((MathHelper.cos(armSwing * 0.6662F) * 1.4F * armSwingAmount) / Math.PI * 180.0f),1.0f);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothX(-5.0f + 0.9f*(float) ((MathHelper.cos(armSwing * 0.6662F + (float)Math.PI) * 1.4F * armSwingAmount) / Math.PI * 180.0f),1.0f);
		
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothY(0.0f);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothY(0.0f);
		
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothZ(10, 0.2f);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothZ(-10, 0.2f);
		
		float var = (float) ((float) (armSwing * 0.6662F)/Math.PI)%2;
		modelLeftForeLeg.rotation.setSmoothX( (var > 1 ? 45 : 0), 0.3f);
		modelRightForeLeg.rotation.setSmoothX( (var > 1 ? 0 : 45), 0.3f);
		modelLeftForeArm.rotation.setSmoothX( ((float) (Math.cos(armSwing * 0.6662F + Math.PI/2)+1.0f)/2.0f)*-20, 1.0f);
		modelRightForeArm.rotation.setSmoothX( ((float) (Math.cos(armSwing * 0.6662F)+1.0f)/2.0f) * -20, 0.3f);
		
		float var1 = ((MathHelper.cos(armSwing * 0.6662F + (float)Math.PI)/(float)Math.PI*180.0f)) * 0.5f;
		((ModelRendererBends)model.bipedBody).rotation.setSmoothY(((MathHelper.cos(armSwing * 0.6662F + (float)Math.PI)/(float)Math.PI*180.0f))*0.5f,0.3f);
		
		((ModelRendererBends)model.bipedHead).rotation.setX(headRotationX - 30);
		((ModelRendererBends)model.bipedHead).rotation.setY(headRotationY - var1);
	}
}
