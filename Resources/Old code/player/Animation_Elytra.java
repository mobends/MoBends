package net.gobbob.mobends.animation.player;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.animation.Animation;
import net.gobbob.mobends.client.event.EventHandlerRenderPlayer;
import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.model.entity.IBendsModel;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.data.DataPlayer;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;

public class Animation_Elytra extends Animation{
	public String getName(){
		return "elytra";
	}

	@Override
	public void animate(EntityLivingBase argEntity, IBendsModel argModel, EntityData argData) {
		ModelBendsPlayer model = (ModelBendsPlayer) argModel;
		DataPlayer data = (DataPlayer) argData;
		
		((ModelPart)model.bipedBody).pre_rotation.setSmooth(new Vector3f(0.0f,0.0f,0.0f),0.5f);
		((ModelPart)model.bipedBody).rotation.setSmooth(new Vector3f(0.0f,0.0f,0.0f),0.5f);
		
		((ModelPart)model.bipedRightLeg).rotation.setSmoothY(5);
		((ModelPart)model.bipedLeftLeg).rotation.setSmoothY(-5);
		
		((ModelPart)model.bipedRightArm).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelPart)model.bipedLeftArm).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelPart)model.bipedRightForeLeg).rotation.setSmoothX(4.0F,0.1f);
		((ModelPart)model.bipedLeftForeLeg).rotation.setSmoothX(4.0F,0.1f);
		((ModelPart)model.bipedRightForeArm).rotation.setSmoothX(-4.0F,0.1f);
		((ModelPart)model.bipedLeftForeArm).rotation.setSmoothX(-4.0F,0.1f);
		//float var2 = (float)Math.cos(model.armSwing * 0.6662F)*-20;
		((ModelPart)model.bipedHead).pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f));
		((ModelPart)model.bipedHead).rotation.set(model.headRotationX*0.1f-70.0f, model.headRotationY*0.5f, 0);
		
		((ModelPart)model.bipedBody).rotation.setSmoothX( (float) ((Math.cos(data.ticks/10)-1.0)/2.0f)*-3 );
		
		float speed = data.getMovementSpeed();
		float ticks = argEntity.ticksExisted+EventHandlerRenderPlayer.partialTicks;
		float twitchX = (float) (Math.sin(ticks*1.0f)*speed)*1;
		float twitchZ = (float) (Math.cos(ticks*1.2f)*speed)*1;
		
		float f = 0.2617994F;
        float f1 = -0.2617994F;
        float f3 = 0.0F;
        
        float f4 = 1.0F;

        if (argEntity.motionY < 0.0D)
        {
            Vec3d vec3d = (new Vec3d(argEntity.motionX, argEntity.motionY, argEntity.motionZ)).normalize();
            f4 = 1.0F - (float)Math.pow(-vec3d.y, 1.5D);
        }

        f = f4 * 0.34906584F + (1.0F - f4) * f;
        f1 = f4 * -((float)Math.PI / 2F) + (1.0F - f4) * f1;
        
        ((ModelPart)model.bipedRightArm).rotation.setSmooth(new Vector3f(0.0f+twitchX, 0.0f, -37.0f*f1+twitchZ),0.5f);
        ((ModelPart)model.bipedLeftArm).rotation.setSmooth(new Vector3f(0.0f-twitchX, 0.0f, 37.0f*f1-twitchZ),0.5f);
		((ModelPart)model.bipedRightLeg).rotation.setSmooth(new Vector3f(0.0f-twitchX, 0.0f, (6.0f-10*f1)-twitchZ),0.5f);
		((ModelPart)model.bipedLeftLeg).rotation.setSmooth(new Vector3f(0.0f+twitchX, 0.0f, -(6.0f-10*f1)+twitchZ),0.5f);
	}
}
