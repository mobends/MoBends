package net.gobbob.mobends.animation.player;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.animation.Animation;
import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.model.entity.IBendsModel;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.client.mutators.RenderMutatorPlayer;
import net.gobbob.mobends.data.Data_Player;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.entity.EntityLivingBase;


public class Animation_Stand extends Animation{
	public String getName(){
		return "stand";
	}

	public String[] getAlterableList() {
		return new String[]{"stand"};
	}
	
	@Override
	public void animate(EntityLivingBase argEntity, IBendsModel argModel, EntityData argData) {
		RenderMutatorPlayer mutator = (RenderMutatorPlayer) argModel;
		Data_Player data = (Data_Player) argData;
		
		mutator.body.pre_rotation.setSmooth(new Vector3f(0.0f,0.0f,0.0f),0.5f);
		mutator.body.rotation.setSmooth(new Vector3f(0.0f,0.0f,0.0f),0.5f);
		mutator.rightLeg.rotation.setSmoothZ(2,0.2f);
		mutator.leftLeg.rotation.setSmoothZ(-2,0.2f);
		mutator.rightLeg.rotation.setSmoothX(0.0F,0.1f);
		mutator.leftLeg.rotation.setSmoothX(0.0F,0.1f);
		
		mutator.leftLeg.rotation.setSmoothY(-5);
		mutator.rightLeg.rotation.setSmoothY(5);
		
		mutator.rightArm.pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		mutator.rightArm.rotation.setSmooth(new Vector3f(0.0F, 0.0F, 0.0F),0.1f);
		mutator.leftArm.pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		mutator.leftArm.rotation.setSmooth(new Vector3f(0.0F, 0.0F, 0.0F), 0.1f);
		mutator.rightForeLeg.rotation.setSmoothX(4.0F,0.1f);
		mutator.leftForeLeg.rotation.setSmoothX(4.0F,0.1f);
		mutator.rightForeArm.rotation.setSmoothX(-4.0F,0.1f);
		mutator.leftForeArm.rotation.setSmoothX(-4.0F,0.1f);
		//float var2 = (float)Math.cos(model.armSwing * 0.6662F)*-20;
		mutator.head.pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		mutator.head.rotation.set(mutator.headPitch, mutator.headYaw, 0);
		
		mutator.body.rotation.setSmoothX( (float) ((Math.cos(data.ticks/10)-1.0)/2.0f)*-3 );
		mutator.leftArm.rotation.setSmoothZ( -(float) ((Math.cos(data.ticks/10+Math.PI/2)-1.0)/2.0f)*-5  );
		mutator.rightArm.rotation.setSmoothZ(  -(float) ((Math.cos(data.ticks/10+Math.PI/2)-1.0)/2.0f)*5  );
	}
}
