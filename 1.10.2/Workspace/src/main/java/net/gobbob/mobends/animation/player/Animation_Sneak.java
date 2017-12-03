package net.gobbob.mobends.animation.player;

import net.gobbob.mobends.animation.Animation;
import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

public class Animation_Sneak extends Animation{
	public String getName(){
		return "sneak";
	}

	@Override
	public void animate(EntityLivingBase argEntity, ModelBase argModel, EntityData argData) {
		ModelBendsPlayer model = (ModelBendsPlayer) argModel;
		
		//model.body.rotation.setSmoothX(20f,0.3f);
		
		float var = (float) ((float) (model.armSwing * 0.6662F)/Math.PI)%2;
		((ModelRendererBends) model.bipedRightLeg).rotation.setSmoothX(-5.0f+1.1f*(float) ((MathHelper.cos(model.armSwing * 0.6662F) * 1.4F * model.armSwingAmount) / Math.PI * 180.0f),1.0f);
		((ModelRendererBends) model.bipedLeftLeg).rotation.setSmoothX(-5.0f+1.1f*(float) ((MathHelper.cos(model.armSwing * 0.6662F + (float)Math.PI) * 1.4F * model.armSwingAmount) / Math.PI * 180.0f),1.0f);
		((ModelRendererBends) model.bipedRightLeg).rotation.setSmoothZ(10);
		((ModelRendererBends) model.bipedLeftLeg).rotation.setSmoothZ(-10);
		
		((ModelRendererBends) model.bipedRightArm).rotation.setSmoothX(-20+20f*(float) (MathHelper.cos(model.armSwing * 0.6662F + (float)Math.PI)));
		((ModelRendererBends) model.bipedLeftArm).rotation.setSmoothX(-20+20f*(float) (MathHelper.cos(model.armSwing * 0.6662F)));
		
		((ModelRendererBends) model.bipedLeftForeLeg).rotation.setSmoothX( (var > 1 ? 45 : 10), 0.3f);
		((ModelRendererBends) model.bipedRightForeLeg).rotation.setSmoothX( (var > 1 ? 10 : 45), 0.3f);
		((ModelRendererBends) model.bipedLeftForeArm).rotation.setSmoothX( (var > 1 ? -10 : -45), 0.01f);
		((ModelRendererBends) model.bipedRightForeArm).rotation.setSmoothX( (var > 1 ? -45 : -10), 0.01f);
		
		float var2 = 25.0f+(float)Math.cos(model.armSwing * 0.6662F * 2.0f)*5;
		((ModelRendererBends) model.bipedBody).rotation.setSmoothX(var2);
		((ModelRendererBends) model.bipedHead).rotation.setX(model.headRotationX - ((ModelRendererBends) model.bipedBody).rotation.getX());
	}
}
