package net.gobbob.mobends.animation.skeleton;

import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.animation.Animation;
import net.gobbob.mobends.client.model.entity.IBendsModel;
import net.gobbob.mobends.client.model.entity.ModelBendsSkeleton;
import net.gobbob.mobends.data.Data_Skeleton;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.pack.BendsPack;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.util.EnumHand;

public class Animation_Attack extends Animation{
	public String getName(){
		return "attack";
	}

	public String[] getAlterableList() {
		return new String[]{
			getName(),
			"attack_0",
			"attack_1"
		};
	}
	
	@Override
	public void animate(EntityLivingBase argEntity, IBendsModel argModel, EntityData argData) {
		ModelBendsSkeleton model = (ModelBendsSkeleton) argModel;
		Data_Skeleton data = (Data_Skeleton) argData;
		AbstractSkeleton skeleton = (AbstractSkeleton) argEntity;
		AnimatedEntity aEntity = AnimatedEntity.getByEntity(argEntity);
		
		if(skeleton.getHeldItem(EnumHand.MAIN_HAND) != null){
			if(data.ticksAfterPunch < 10){
				if(data.currentAttack == 1){
					Animation_Attack_Combo0.animate(skeleton,model,data);
					BendsPack.animate(model,aEntity.getName(),"attack");
					BendsPack.animate(model,aEntity.getName(),"attack_0");
				}else if(data.currentAttack == 2){
					Animation_Attack_Combo1.animate(skeleton,model,data);
					BendsPack.animate(model,aEntity.getName(),"attack");
					BendsPack.animate(model,aEntity.getName(),"attack_1");
				}
			}
		}
	}
}
