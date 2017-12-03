package net.gobbob.mobends.animation.player;

import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.data.Data_Player;
import net.gobbob.mobends.util.GUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;

import org.lwjgl.util.vector.Vector3f;

public class Animation_Attack_Combo2 {
	public static void animate(EntityPlayer player,ModelBendsPlayer model,Data_Player data){
		boolean mainHandSwitch = model.primaryHand == EnumHandSide.RIGHT;
		float handDirMtp = mainHandSwitch ? 1 : -1; // Main Hand Direction Multiplier - it helps switch animation sides depending on what is your main hand.
		ModelRendererBends mainArmBox = mainHandSwitch ? ((ModelRendererBends) model.bipedRightArm) : ((ModelRendererBends) model.bipedLeftArm);
		ModelRendererBends offArmBox = mainHandSwitch ? ((ModelRendererBends) model.bipedLeftArm) : ((ModelRendererBends) model.bipedRightArm);
		ModelRendererBends mainForeArmBox = mainHandSwitch ? model.bipedRightForeArm : model.bipedLeftForeArm;
		ModelRendererBends offForeArmBox = mainHandSwitch ? model.bipedLeftForeArm : model.bipedRightForeArm;
		
		ItemStack offHandItemStack = player.getHeldItemOffhand();
		
		if(data.ticksAfterPunch < 0.5f){
			model.swordTrail.reset();
		}
		
		if(player.getHeldItem(EnumHand.MAIN_HAND) != null){
			if(player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemSword){
				model.swordTrail.add(model);
			}
		}
		
		
		float attackState = data.ticksAfterPunch/10.0f;
		float armSwing = attackState*2.0f;
		armSwing = Math.min(armSwing, 1.0f);
		
		float var5 = attackState*1.6f;
		var5 = Math.min(var5, 1.0f);
		
		float var = 50+360*var5;
		float var2 = 50+360*var5;
		
		while(var2 > 360.0f){
			var2-=360.0f;
		}
		
		model.renderRotation.vOld.y *= handDirMtp;
		model.renderRotation.vFinal.y *= handDirMtp;
		if(var > 360.0f){
			float var3 = (attackState-DataUpdateHandler.ticksPerFrame/10.0f)*2.0f;
			var3 = Math.min(var3, 1.0f);
			model.renderRotation.vOld.y = var2;
			model.renderRotation.vFinal.y = var2;
			model.renderRotation.completion.y = 0.0f;
		}else{
			model.renderRotation.setSmoothY(var,0.7f);
		}
		model.renderRotation.vOld.y *= handDirMtp;
		model.renderRotation.vFinal.y *= handDirMtp;
		
		Vector3f bodyRot = new Vector3f(0,0,0);
		
		bodyRot.x = 20.0f-attackState*20.0f;
		bodyRot.y = -40.0f*attackState*handDirMtp;
		
		((ModelRendererBends)model.bipedBody).rotation.setSmooth(bodyRot,0.9f);
		((ModelRendererBends)model.bipedHead).rotation.setY(model.headRotationY);
		((ModelRendererBends)model.bipedHead).rotation.setX(model.headRotationX);
		((ModelRendererBends)model.bipedHead).pre_rotation.setSmoothX(-bodyRot.x,0.9f);
		((ModelRendererBends)model.bipedHead).pre_rotation.setSmoothY(-bodyRot.y,0.9f);
		
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothX(-30,0.3f);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothX(-30,0.3f);
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothZ(10);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothZ(-10);
		
		((ModelRendererBends)model.bipedRightForeLeg).rotation.setSmoothX(30,0.3f);
		((ModelRendererBends)model.bipedLeftForeLeg).rotation.setSmoothX(30,0.3f);
		
		mainArmBox.pre_rotation.setSmoothZ(-(-60.0f-var5*80)*handDirMtp,0.3f);
		mainArmBox.rotation.setSmoothX(-20+armSwing*70,3.0f);
		mainArmBox.rotation.setSmoothY(0.0f,0.3f);
		mainArmBox.rotation.setSmoothZ(0.0f,0.9f);
		
		offArmBox.rotation.setSmoothZ(20*handDirMtp,0.3f);
		offArmBox.pre_rotation.setSmoothZ(-80*handDirMtp,0.3f);
		
		if(offHandItemStack != null && offHandItemStack.getItem().getItemUseAction(offHandItemStack) == EnumAction.BLOCK){
			offArmBox.pre_rotation.setSmoothZ(-40*handDirMtp,0.3f);
		}
		
		mainForeArmBox.rotation.setSmoothX(-20,0.3f);
		offForeArmBox.rotation.setSmoothX(-60,0.3f);
		
		if(mainHandSwitch){
			model.renderRightItemRotation.setSmoothX(90*attackState, 0.9f);
		}else{
			model.renderLeftItemRotation.setSmoothX(90*attackState, 0.9f);
		}
		
		float var61 = data.ticksAfterPunch*5;
		float var62 = data.ticksAfterPunch*5;
		
		var61 = (MathHelper.cos(var61*0.0625f)+1.0f)/2.0f*20;
		var62 = (MathHelper.cos(var62*0.0625f)+1.0f)/2.0f*20;
		
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothY(0,0.9f);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothY(-25,0.9f);
		
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothZ(var61);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothZ(-var61);
		model.renderOffset.setSmoothY(-2.0f);
	}
}