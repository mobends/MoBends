package net.gobbob.mobends.animation.player;

import net.gobbob.mobends.animation.Animation;
import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.model.entity.IBendsModel;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.data.Data_Player;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

public class Animation_Mining extends Animation{
	
	public String getName(){
		return "mining";
	}

	@Override
	public void animate(EntityLivingBase argEntity, IBendsModel argModel, EntityData argData) {
		ModelBendsPlayer model = (ModelBendsPlayer) argModel;
		Data_Player data = (Data_Player) argData;
		EntityPlayer player = (EntityPlayer) argEntity;
		
		((ModelPart)model.bipedRightLeg).rotation.setSmoothZ(10,0.3f);
		((ModelPart)model.bipedLeftLeg).rotation.setSmoothZ(-10,0.3f);
		model.renderOffset.setSmoothY(-1.5f,0.3f);
		
		if(player.isSwingInProgress){
			float speed = 1.8f;
			
			float progress = (player.ticksExisted*speed/20.0f)%1;
			float progress2 = ((player.ticksExisted-2)*speed/20.0f)%1;
			
			float armSwing = (MathHelper.cos(progress*(float)Math.PI*2)+1)/2*-60-30+model.headRotationX*0.5f-30.0f;
			
			
			float armYRot = 30.0f+MathHelper.cos((armSwing-90)/180.0f*3.14f)*-5.0f;
			
			((ModelPart)model.bipedRightArm).rotation.setSmoothX(armSwing,0.7f);
			((ModelPart)model.bipedRightArm).rotation.setSmoothY(-armYRot,0.7f);
			model.renderRightItemRotation.setSmoothZ(-30.0f,0.3f);
			
			((ModelPart)model.bipedBody).rotation.setSmoothY(MathHelper.sin(progress2*(float)Math.PI*2)*-20);
			((ModelPart)model.bipedHead).rotation.setSmoothX(model.headRotationX-model.bipedBody.rotateAngleX);
			((ModelPart)model.bipedHead).rotation.setSmoothY(model.headRotationY-model.bipedBody.rotateAngleY);
		}
	}
}
