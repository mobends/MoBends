package net.gobbob.mobends.animation.player;

import net.gobbob.mobends.animation.Animation;
import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.model.entity.IBendsModel;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

public class Animation_Sneak extends Animation{
	public String getName(){
		return "sneak";
	}

	@Override
	public void animate(EntityLivingBase argEntity, IBendsModel argModel, EntityData argData) {
		ModelBendsPlayer model = (ModelBendsPlayer) argModel;
		
		//model.body.rotation.setSmoothX(20f,0.3f);
		
		float var = (float) ((float) (model.armSwing * 0.6662F)/Math.PI)%2;
		((ModelPart) model.bipedRightLeg).rotation.setSmoothX(-5.0f+1.1f*(float) ((MathHelper.cos(model.armSwing * 0.6662F) * 1.4F * model.armSwingAmount) / Math.PI * 180.0f),1.0f);
		((ModelPart) model.bipedLeftLeg).rotation.setSmoothX(-5.0f+1.1f*(float) ((MathHelper.cos(model.armSwing * 0.6662F + (float)Math.PI) * 1.4F * model.armSwingAmount) / Math.PI * 180.0f),1.0f);
		((ModelPart) model.bipedRightLeg).rotation.setSmoothZ(10);
		((ModelPart) model.bipedLeftLeg).rotation.setSmoothZ(-10);
		
		((ModelPart) model.bipedRightArm).rotation.setSmoothX(-20+20f*(float) (MathHelper.cos(model.armSwing * 0.6662F + (float)Math.PI)));
		((ModelPart) model.bipedLeftArm).rotation.setSmoothX(-20+20f*(float) (MathHelper.cos(model.armSwing * 0.6662F)));
		
		((ModelPart) model.bipedLeftForeLeg).rotation.setSmoothX( (var > 1 ? 45 : 10), 0.3f);
		((ModelPart) model.bipedRightForeLeg).rotation.setSmoothX( (var > 1 ? 10 : 45), 0.3f);
		((ModelPart) model.bipedLeftForeArm).rotation.setSmoothX( (var > 1 ? -10 : -45), 0.01f);
		((ModelPart) model.bipedRightForeArm).rotation.setSmoothX( (var > 1 ? -45 : -10), 0.01f);
		
		float var2 = 25.0f+(float)Math.cos(model.armSwing * 0.6662F * 2.0f)*5;
		((ModelPart) model.bipedBody).rotation.setSmoothX(var2);
		((ModelPart) model.bipedHead).rotation.setX(model.headRotationX - ((ModelPart) model.bipedBody).rotation.getX());
	}
}
