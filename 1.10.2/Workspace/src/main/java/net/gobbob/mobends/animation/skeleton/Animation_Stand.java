package net.gobbob.mobends.animation.skeleton;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.animation.Animation;
import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.entity.ModelBendsSkeleton;
import net.gobbob.mobends.data.Data_Skeleton;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;

public class Animation_Stand extends Animation{
	
	public String getName(){
		return "stand";
	}

	@Override
	public void animate(EntityLivingBase argEntity, ModelBase argModel, EntityData argData) {
		EntitySkeleton skeleton = (EntitySkeleton) argEntity;
		ModelBendsSkeleton model = (ModelBendsSkeleton) argModel;
		Data_Skeleton data = (Data_Skeleton) argData;
		
		model.renderOffset.setSmoothY(0.0f);
		
		((ModelRendererBends)model.bipedBody).pre_rotation.setSmooth(new Vector3f(0.0f,0.0f,0.0f),0.5f);
		((ModelRendererBends)model.bipedBody).rotation.setSmooth(new Vector3f(0.0f,0.0f,0.0f),0.5f);
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothZ(2,0.2f);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothZ(-2,0.2f);
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothX(0.0F,0.1f);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothX(0.0F,0.1f);
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothY(0.0F,0.1f);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothY(0.0F,0.1f);
		
		((ModelRendererBends)model.bipedRightArm).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelRendererBends)model.bipedRightArm).rotation.setSmooth(new Vector3f(0.0F, 0.0F, 0.0F),0.1f);
		((ModelRendererBends)model.bipedLeftArm).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelRendererBends)model.bipedLeftArm).rotation.setSmooth(new Vector3f(0.0F, 0.0F, 0.0F), 0.1f);
		((ModelRendererBends)model.bipedRightForeLeg).rotation.setSmoothX(4.0F,0.1f);
		((ModelRendererBends)model.bipedLeftForeLeg).rotation.setSmoothX(4.0F,0.1f);
		((ModelRendererBends)model.bipedRightForeArm).rotation.setSmoothX(-4.0F,0.1f);
		((ModelRendererBends)model.bipedLeftForeArm).rotation.setSmoothX(-4.0F,0.1f);
		
		((ModelRendererBends)model.bipedHead).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelRendererBends)model.bipedHead).rotation.set(model.headRotationX, model.headRotationY, 0);
		
		((ModelRendererBends)model.bipedBody).rotation.setSmoothX( (float) ((Math.cos(data.ticks/10)-1.0)/2.0f)*-3 );
		((ModelRendererBends)model.bipedLeftArm).rotation.setSmoothZ( -(float) ((Math.cos(data.ticks/10+Math.PI/2)-1.0)/2.0f)*-5  );
		((ModelRendererBends)model.bipedRightArm).rotation.setSmoothZ(  -(float) ((Math.cos(data.ticks/10+Math.PI/2)-1.0)/2.0f)*5  );
	}
}
