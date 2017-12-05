package net.gobbob.mobends.animation.player;

import net.gobbob.mobends.animation.Animation;
import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.model.entity.IBendsModel;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.data.DataPlayer;
import net.gobbob.mobends.data.EntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped.ArmPose;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHandSide;

import org.lwjgl.util.vector.Vector3f;

public class Animation_Guard extends Animation{
	@Override
	public void animate(EntityLivingBase argEntity, IBendsModel argModel, EntityData argData) {
		ModelBendsPlayer model = (ModelBendsPlayer) argModel;
		DataPlayer data = (DataPlayer) argData;
		EntityPlayer player = (EntityPlayer) argEntity;
		
		boolean handInUse = model.rightArmPose == ArmPose.BLOCK;
		float handDirMtp = handInUse ? 1 : -1; // Main Hand Direction Multiplier - it helps switch animation sides depending on what is your main hand.
		ModelPart mainArmBox = handInUse ? ((ModelPart) model.bipedRightArm) : ((ModelPart) model.bipedLeftArm);
		ModelPart offArmBox = handInUse ? ((ModelPart) model.bipedLeftArm) : ((ModelPart) model.bipedRightArm);
		ModelPart mainForeArmBox = handInUse ? model.bipedRightForeArm : model.bipedLeftForeArm;
		ModelPart offForeArmBox = handInUse ? model.bipedLeftForeArm : model.bipedRightForeArm;
		
		/*if(!data.isOnGround()){
			return;
		}*/
		
		((ModelPart)model.bipedBody).pre_rotation.setSmooth(new Vector3f(0.0f, model.renderRotation.getY(), 0.0f), 0.7f);
		((ModelPart)model.bipedBody).rotation.setSmoothY(-30.0f*handDirMtp, 0.7f);
		mainArmBox.pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f), 0.7f);
		mainArmBox.rotation.setSmooth(new Vector3f(-20.0f, -45.0f*handDirMtp, 0.0f), 0.7f);
		offArmBox.pre_rotation.setSmooth(new Vector3f(0.0f, 0.0f, 0.0f), 0.7f);
		
		((ModelPart)model.bipedHead).rotation.setSmoothY(model.headRotationY+30.0f*handDirMtp, 0.5f);
	}

	@Override
	public String getName() {
		return "guard";
	}
}
