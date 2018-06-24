package net.gobbob.mobends.data;

import java.util.Random;

import net.gobbob.mobends.client.model.entity.ModelBendsSkeleton;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;

public class DataSkeleton extends BipedEntityData
{
    public SmoothVector3f renderOffset = new SmoothVector3f();
    public SmoothVector3f renderRotation = new SmoothVector3f();
    public SmoothVector3f renderRightItemRotation = new SmoothVector3f();
    public SmoothVector3f renderLeftItemRotation = new SmoothVector3f();
    
    public int currentWalkingState = 0;
    public float ticksBeforeStateChange = 0;
    public int currentAttack = 0;
    
	public DataSkeleton(Entity entity) {
		super(entity);
	}
	
	public void syncModelInfo(ModelBendsSkeleton argModel){
		this.renderOffset.set(argModel.renderOffset);
		this.renderRotation.set(argModel.renderRotation);
		this.renderRightItemRotation.set(argModel.renderRightItemRotation);
		this.renderLeftItemRotation.set(argModel.renderLeftItemRotation);
	}

	@Override
	public void update(float argPartialTicks) {
		super.update(argPartialTicks);
		if(this.getEntity() == null){
			return;
		}
		
		this.ticksBeforeStateChange-=argPartialTicks;
		
		if(this.ticksAfterAttack > 10){
			this.currentAttack = 0;
		}
		
		if(this.ticksBeforeStateChange <= 0){
			Random random = new Random();
			this.currentWalkingState = random.nextInt(2);
			this.ticksBeforeStateChange=80+random.nextInt(20);
		}
	}
	
	@Override
	public void onLiftoff(){
		super.onLiftoff();
	}
	
	@Override
	public void onPunch(){
		if(!(this.entity instanceof AbstractSkeleton))
			return;
		
		AbstractSkeleton entitySkeleton = (AbstractSkeleton) this.entity;
		
		if(entitySkeleton.getHeldItem(EnumHand.MAIN_HAND) != null){
			if(this.ticksAfterAttack > 6.0f)
			{
				if(this.currentAttack == 0)
				{
					this.currentAttack = 1;
					this.ticksAfterAttack = 0;
				}
				else
				{
					if(this.ticksAfterAttack < 15.0f)
					{
						if(this.currentAttack == 1)
							this.currentAttack = 2;
						else if(this.currentAttack == 2)
							this.currentAttack = 1;
						this.ticksAfterAttack = 0;
					}
				}
			}
		}
	}

	@Override
	public void onTicksRestart()
	{
	}
}