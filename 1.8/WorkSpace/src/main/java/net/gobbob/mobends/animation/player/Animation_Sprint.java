package net.gobbob.mobends.animation.player;

import net.gobbob.mobends.animation.Animation;
import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.util.GUtil;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

public class Animation_Sprint extends Animation{
	public String getName(){
		return "sprint";
	}

	@Override
	public void animate(EntityLivingBase argEntity, ModelBase argModel, EntityData argData) {
		ModelBendsPlayer model = (ModelBendsPlayer) argModel;
		
		((ModelRendererBends)model.bipedRightArm).rotation.setSmoothX(1.1f*(float) ((MathHelper.cos(model.armSwing * 0.6662F + (float)Math.PI) * 2.0F * model.armSwingAmount * 0.5F ) / Math.PI * 180.0f));
		((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothX(1.1f*(float) ((MathHelper.cos(model.armSwing * 0.6662F) * 2.0F * model.armSwingAmount * 0.5F) / Math.PI * 180.0f));
		
		((ModelRendererBends)model.bipedRightArm).rotation.setSmoothZ(5,0.3f);
		((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothZ(-5,0.3f);
		
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothX(-5.0f+0.9f*(float) ((MathHelper.cos(model.armSwing * 0.6662F) * 1.4F * model.armSwingAmount) / Math.PI * 180.0f),1.0f);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothX(-5.0f+0.9f*(float) ((MathHelper.cos(model.armSwing * 0.6662F + (float)Math.PI) * 1.4F * model.armSwingAmount) / Math.PI * 180.0f),1.0f);
		
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothY(0.0f);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothY(0.0f);
		
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothZ(2,0.2f);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothZ(-2,0.2f);
		
		float var = (float) ((float) (model.armSwing * 0.6662F)/Math.PI)%2;
		((ModelRendererBends)model.bipedLeftForeLeg).rotation.setSmoothX( (var > 1 ? 45 : 0), 0.3f);
		((ModelRendererBends)model.bipedRightForeLeg).rotation.setSmoothX( (var > 1 ? 0 : 45), 0.3f);
		((ModelRendererBends)model.bipedLeftForeArm).rotation.setSmoothX( (var > 1 ? -10 : -45), 0.3f);
		((ModelRendererBends)model.bipedRightForeArm).rotation.setSmoothX( (var > 1 ? -45 : -10), 0.3f);
		
		float var2 = (float)Math.cos(model.armSwing * 0.6662F)*-40;
		float var3 = (float)(Math.cos(model.armSwing * 0.6662F * 2.0f)*0.5f+0.5f)*20;
		((ModelRendererBends)model.bipedBody).rotation.setSmoothY(var2,0.5f);
		((ModelRendererBends)model.bipedBody).rotation.setSmoothX(var3);
		((ModelRendererBends)model.bipedHead).rotation.setSmoothY(model.headRotationY - var2,0.5f);
		((ModelRendererBends)model.bipedHead).rotation.setSmoothX(model.headRotationX - var3);
		
		float var10 = model.headRotationY*0.3f;
		var10 = GUtil.max(var10,20);
		var10 = GUtil.min(var10,-20);
		((ModelRendererBends)model.bipedBody).rotation.setSmoothZ(-var10, 0.3f);
		
		model.renderOffset.setSmoothY(var3*0.15f,0.9f);
	}
}
