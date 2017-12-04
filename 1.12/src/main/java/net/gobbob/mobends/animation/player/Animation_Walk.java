package net.gobbob.mobends.animation.player;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.animation.Animation;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.model.entity.IBendsModel;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.data.Data_Player;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

public class Animation_Walk extends Animation{
	public String getName(){
		return "walk";
	}

	@Override
	public void animate(EntityLivingBase argEntity, IBendsModel argModel, EntityData argData) {
		ModelBendsPlayer model = (ModelBendsPlayer) argModel;
		Data_Player data = (Data_Player) argData;
		
		((ModelPart)model.bipedRightArm).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelPart)model.bipedRightArm).rotation.setSmoothX(0.5f*(float) ((MathHelper.cos(model.armSwing * 0.6662F + (float)Math.PI) * 2.0F * model.armSwingAmount * 0.5F ) / Math.PI * 180.0f));
		((ModelPart)model.bipedLeftArm).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelPart)model.bipedLeftArm).rotation.setSmoothX(0.5f*(float) ((MathHelper.cos(model.armSwing * 0.6662F) * 2.0F * model.armSwingAmount * 0.5F) / Math.PI * 180.0f));
		
		((ModelPart)model.bipedRightArm).rotation.setSmoothY(0,0.3f);
		((ModelPart)model.bipedLeftArm).rotation.setSmoothY(0,0.3f);
		((ModelPart)model.bipedRightArm).rotation.setSmoothZ(5,0.3f);
		((ModelPart)model.bipedLeftArm).rotation.setSmoothZ(-5,0.3f);
		
		((ModelPart)model.bipedRightLeg).rotation.setSmoothX(-5.0f+0.5f*(float) ((MathHelper.cos(model.armSwing * 0.6662F) * 1.4F * model.armSwingAmount) / Math.PI * 180.0f),1.0f);
		((ModelPart)model.bipedLeftLeg).rotation.setSmoothX(-5.0f+0.5f*(float) ((MathHelper.cos(model.armSwing * 0.6662F + (float)Math.PI) * 1.4F * model.armSwingAmount) / Math.PI * 180.0f),1.0f);
		
		((ModelPart)model.bipedRightLeg).rotation.setSmoothY(0.0f);
		((ModelPart)model.bipedLeftLeg).rotation.setSmoothY(0.0f);
		
		((ModelPart)model.bipedRightLeg).rotation.setSmoothZ(2,0.2f);
		((ModelPart)model.bipedLeftLeg).rotation.setSmoothZ(-2,0.2f);
		
		float var = (float) ((float) (model.armSwing * 0.6662F)/Math.PI)%2;
		((ModelPart)model.bipedLeftForeLeg).rotation.setSmoothX( (var > 1 ? 45 : 0), 0.3f);
		((ModelPart)model.bipedRightForeLeg).rotation.setSmoothX( (var > 1 ? 0 : 45), 0.3f);
		((ModelPart)model.bipedLeftForeArm).rotation.setSmoothX( ((float) (Math.cos(model.armSwing * 0.6662F + Math.PI/2)+1.0f)/2.0f)*-20, 1.0f);
		((ModelPart)model.bipedRightForeArm).rotation.setSmoothX( ((float) (Math.cos(model.armSwing * 0.6662F)+1.0f)/2.0f)*-20, 0.3f);
		
		float var2 = (float)Math.cos(model.armSwing * 0.6662F)*-20;
		float var3 = (float)(Math.cos(model.armSwing * 0.6662F * 2.0f)*0.5f+0.5f)*10-2;
		((ModelPart)model.bipedBody).pre_rotation.setSmooth(new Vector3f(0.0f,0.0f,0.0f),0.5f);
		((ModelPart)model.bipedBody).rotation.setSmoothY(var2,0.5f);
		((ModelPart)model.bipedBody).rotation.setSmoothX(var3);
		((ModelPart)model.bipedHead).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelPart)model.bipedHead).rotation.setY(model.headRotationY - ((ModelPart)model.bipedBody).rotation.getNextY(DataUpdateHandler.ticksPerFrame));
		((ModelPart)model.bipedHead).rotation.setX(model.headRotationX - ((ModelPart)model.bipedBody).rotation.getNextX(DataUpdateHandler.ticksPerFrame));
		
		float var10 = model.headRotationY*0.1f;
		var10 = Math.min(var10,10);
		var10 = Math.max(var10,-10);
		((ModelPart)model.bipedBody).rotation.setSmoothZ(-var10, 0.3f);
	}
}
