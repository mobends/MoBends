package net.gobbob.mobends.animation.skeleton;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.animation.Animation;
import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.model.entity.IBendsModel;
import net.gobbob.mobends.client.model.entity.ModelBendsSkeleton;
import net.gobbob.mobends.data.Data_Skeleton;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.AbstractSkeleton;

public class Animation_Stand extends Animation{
	
	public String getName(){
		return "stand";
	}

	@Override
	public void animate(EntityLivingBase argEntity, IBendsModel argModel, EntityData argData) {
		AbstractSkeleton skeleton = (AbstractSkeleton) argEntity;
		ModelBendsSkeleton model = (ModelBendsSkeleton) argModel;
		Data_Skeleton data = (Data_Skeleton) argData;
		
		model.renderOffset.setSmoothY(0.0f);
		
		((ModelPart)model.bipedBody).pre_rotation.setSmooth(new Vector3f(0.0f,0.0f,0.0f),0.5f);
		((ModelPart)model.bipedBody).rotation.setSmooth(new Vector3f(0.0f,0.0f,0.0f),0.5f);
		((ModelPart)model.bipedRightLeg).rotation.setSmoothZ(2,0.2f);
		((ModelPart)model.bipedLeftLeg).rotation.setSmoothZ(-2,0.2f);
		((ModelPart)model.bipedRightLeg).rotation.setSmoothX(0.0F,0.1f);
		((ModelPart)model.bipedLeftLeg).rotation.setSmoothX(0.0F,0.1f);
		((ModelPart)model.bipedRightLeg).rotation.setSmoothY(0.0F,0.1f);
		((ModelPart)model.bipedLeftLeg).rotation.setSmoothY(0.0F,0.1f);
		
		((ModelPart)model.bipedRightArm).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelPart)model.bipedRightArm).rotation.setSmooth(new Vector3f(0.0F, 0.0F, 0.0F),0.1f);
		((ModelPart)model.bipedLeftArm).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelPart)model.bipedLeftArm).rotation.setSmooth(new Vector3f(0.0F, 0.0F, 0.0F), 0.1f);
		((ModelPart)model.bipedRightForeLeg).rotation.setSmoothX(4.0F,0.1f);
		((ModelPart)model.bipedLeftForeLeg).rotation.setSmoothX(4.0F,0.1f);
		((ModelPart)model.bipedRightForeArm).rotation.setSmoothX(-4.0F,0.1f);
		((ModelPart)model.bipedLeftForeArm).rotation.setSmoothX(-4.0F,0.1f);
		
		((ModelPart)model.bipedHead).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelPart)model.bipedHead).rotation.set(model.headRotationX, model.headRotationY, 0);
		
		((ModelPart)model.bipedBody).rotation.setSmoothX( (float) ((Math.cos(data.ticks/10)-1.0)/2.0f)*-3 );
		((ModelPart)model.bipedLeftArm).rotation.setSmoothZ( -(float) ((Math.cos(data.ticks/10+Math.PI/2)-1.0)/2.0f)*-5  );
		((ModelPart)model.bipedRightArm).rotation.setSmoothZ(  -(float) ((Math.cos(data.ticks/10+Math.PI/2)-1.0)/2.0f)*5  );
	}
}
