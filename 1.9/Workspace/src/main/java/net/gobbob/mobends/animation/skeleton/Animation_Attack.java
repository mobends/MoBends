package net.gobbob.mobends.animation.skeleton;

import net.gobbob.mobends.animation.Animation;
import net.gobbob.mobends.client.model.entity.ModelBendsSkeleton;
import net.gobbob.mobends.data.Data_Skeleton;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.pack.BendsPack;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;

public class Animation_Attack extends Animation{
	public String getName(){
		return "attack";
	}

	@Override
	public void animate(EntityLivingBase argEntity, ModelBase argModel, EntityData argData) {
		ModelBendsSkeleton model = (ModelBendsSkeleton) argModel;
		Data_Skeleton data = (Data_Skeleton) argData;
		EntitySkeleton skeleton = (EntitySkeleton) argEntity;
		String bendsTargetName = skeleton.getSkeletonType() == 1 ? "witherSkeleton" : "skeleton";
		
		if(skeleton.getHeldItem(EnumHand.MAIN_HAND) != null){
			
			if(data.ticksAfterPunch < 10){
				if(data.currentAttack == 1){
					Animation_Attack_Combo0.animate(skeleton,model,data);
					BendsPack.animate(model,bendsTargetName,"attack");
					BendsPack.animate(model,bendsTargetName,"attack_0");
				}else if(data.currentAttack == 2){
					Animation_Attack_Combo1.animate(skeleton,model,data);
					BendsPack.animate(model,bendsTargetName,"attack");
					BendsPack.animate(model,bendsTargetName,"attack_1");
				}
			}
		}
	}
}
