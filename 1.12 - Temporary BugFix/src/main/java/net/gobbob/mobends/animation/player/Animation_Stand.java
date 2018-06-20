package net.gobbob.mobends.animation.player;

import net.gobbob.mobends.animation.Animation;
import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.data.Data_Player;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;

import org.lwjgl.util.vector.Vector3f;


public class Animation_Stand extends Animation{
	public String getName(){
		return "stand";
	}

	public String[] getAlterableList() {
		return new String[]{"stand"};
	}
	
	@Override
	public void animate(EntityLivingBase argEntity, ModelBase argModel, EntityData argData) {
		ModelBendsPlayer model = (ModelBendsPlayer) argModel;
		Data_Player data = (Data_Player) argData;
		
		((ModelRendererBends)model.bipedBody).pre_rotation.setSmooth(new Vector3f(0.0f,0.0f,0.0f),0.5f);
		((ModelRendererBends)model.bipedBody).rotation.setSmooth(new Vector3f(0.0f,0.0f,0.0f),0.5f);
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothZ(2,0.2f);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothZ(-2,0.2f);
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothX(0.0F,0.1f);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothX(0.0F,0.1f);
		
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothY(5);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothY(-5);
		
		((ModelRendererBends)model.bipedRightArm).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelRendererBends)model.bipedRightArm).rotation.setSmooth(new Vector3f(0.0F, 0.0F, 0.0F),0.1f);
		((ModelRendererBends)model.bipedLeftArm).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelRendererBends)model.bipedLeftArm).rotation.setSmooth(new Vector3f(0.0F, 0.0F, 0.0F), 0.1f);
		((ModelRendererBends)model.bipedRightForeLeg).rotation.setSmoothX(4.0F,0.1f);
		((ModelRendererBends)model.bipedLeftForeLeg).rotation.setSmoothX(4.0F,0.1f);
		((ModelRendererBends)model.bipedRightForeArm).rotation.setSmoothX(-4.0F,0.1f);
		((ModelRendererBends)model.bipedLeftForeArm).rotation.setSmoothX(-4.0F,0.1f);
		//float var2 = (float)Math.cos(model.armSwing * 0.6662F)*-20;
		((ModelRendererBends)model.bipedHead).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelRendererBends)model.bipedHead).rotation.set(model.headRotationX, model.headRotationY, 0);
		
		((ModelRendererBends)model.bipedBody).rotation.setSmoothX( (float) ((Math.cos(data.ticks/10)-1.0)/2.0f)*-3 );
		((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothZ( -(float) ((Math.cos(data.ticks/10+Math.PI/2)-1.0)/2.0f)*-5  );
		((ModelRendererBends)model.bipedRightArm).rotation.setSmoothZ(  -(float) ((Math.cos(data.ticks/10+Math.PI/2)-1.0)/2.0f)*5  );
	}
}
