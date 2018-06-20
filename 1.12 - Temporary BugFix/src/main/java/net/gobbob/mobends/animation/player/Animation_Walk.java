package net.gobbob.mobends.animation.player;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.animation.Animation;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.data.Data_Player;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.util.GUtil;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

public class Animation_Walk extends Animation{
	public String getName(){
		return "walk";
	}

	@Override
	public void animate(EntityLivingBase argEntity, ModelBase argModel, EntityData argData) {
		ModelBendsPlayer model = (ModelBendsPlayer) argModel;
		Data_Player data = (Data_Player) argData;
		
		((ModelRendererBends)model.bipedRightArm).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelRendererBends)model.bipedRightArm).rotation.setSmoothX(0.5f*(float) ((MathHelper.cos(model.armSwing * 0.6662F + (float)Math.PI) * 2.0F * model.armSwingAmount * 0.5F ) / Math.PI * 180.0f));
		((ModelRendererBends)model.bipedLeftArm).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothX(0.5f*(float) ((MathHelper.cos(model.armSwing * 0.6662F) * 2.0F * model.armSwingAmount * 0.5F) / Math.PI * 180.0f));
		
		((ModelRendererBends)model.bipedRightArm).rotation.setSmoothY(0,0.3f);
		((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothY(0,0.3f);
		((ModelRendererBends)model.bipedRightArm).rotation.setSmoothZ(5,0.3f);
		((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothZ(-5,0.3f);
		
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothX(-5.0f+0.5f*(float) ((MathHelper.cos(model.armSwing * 0.6662F) * 1.4F * model.armSwingAmount) / Math.PI * 180.0f),1.0f);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothX(-5.0f+0.5f*(float) ((MathHelper.cos(model.armSwing * 0.6662F + (float)Math.PI) * 1.4F * model.armSwingAmount) / Math.PI * 180.0f),1.0f);
		
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothY(0.0f);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothY(0.0f);
		
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothZ(2,0.2f);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothZ(-2,0.2f);
		
		float var = (float) ((float) (model.armSwing * 0.6662F)/Math.PI)%2;
		((ModelRendererBends)model.bipedLeftForeLeg).rotation.setSmoothX( (var > 1 ? 45 : 0), 0.3f);
		((ModelRendererBends)model.bipedRightForeLeg).rotation.setSmoothX( (var > 1 ? 0 : 45), 0.3f);
		((ModelRendererBends)model.bipedLeftForeArm).rotation.setSmoothX( ((float) (Math.cos(model.armSwing * 0.6662F + Math.PI/2)+1.0f)/2.0f)*-20, 1.0f);
		((ModelRendererBends)model.bipedRightForeArm).rotation.setSmoothX( ((float) (Math.cos(model.armSwing * 0.6662F)+1.0f)/2.0f)*-20, 0.3f);
		
		float var2 = (float)Math.cos(model.armSwing * 0.6662F)*-20;
		float var3 = (float)(Math.cos(model.armSwing * 0.6662F * 2.0f)*0.5f+0.5f)*10-2;
		((ModelRendererBends)model.bipedBody).pre_rotation.setSmooth(new Vector3f(0.0f,0.0f,0.0f),0.5f);
		((ModelRendererBends)model.bipedBody).rotation.setSmoothY(var2,0.5f);
		((ModelRendererBends)model.bipedBody).rotation.setSmoothX(var3);
		((ModelRendererBends)model.bipedHead).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelRendererBends)model.bipedHead).rotation.setY(model.headRotationY - ((ModelRendererBends)model.bipedBody).rotation.getNextY(DataUpdateHandler.ticksPerFrame));
		((ModelRendererBends)model.bipedHead).rotation.setX(model.headRotationX - ((ModelRendererBends)model.bipedBody).rotation.getNextX(DataUpdateHandler.ticksPerFrame));
		
		float var10 = model.headRotationY*0.1f;
		var10 = GUtil.max(var10,10);
		var10 = GUtil.min(var10,-10);
		((ModelRendererBends)model.bipedBody).rotation.setSmoothZ(-var10, 0.3f);
	}
}
