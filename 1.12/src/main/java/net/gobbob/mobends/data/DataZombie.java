package net.gobbob.mobends.data;

import java.util.Random;

import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.client.model.entity.ModelBendsZombie;
import net.gobbob.mobends.client.model.entity.ModelBendsZombieVillager;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.entity.Entity;

public class DataZombie extends BipedEntityData {
    public SmoothVector3f renderOffset = new SmoothVector3f();
    public SmoothVector3f renderRotation = new SmoothVector3f();
    
    public int currentWalkingState = 0;
    public float ticksBeforeStateChange = 0;
    
	public DataZombie(Entity entity) {
		super(entity);
	}
	
	public void syncModelInfo(ModelBendsZombie argModel){
		/*if(this.head == null) this.head = new ModelPart(argModel, false); this.head.sync((ModelPart)argModel.bipedHead);
		if(this.headwear == null) this.headwear = new ModelPart(argModel, false); this.headwear.sync((ModelPart)argModel.bipedHeadwear);
		if(this.body == null) this.body = new ModelPart(argModel, false); this.body.sync((ModelPart)argModel.bipedBody);
		if(this.rightArm == null) this.rightArm = new ModelPart(argModel, false); this.rightArm.sync((ModelPart)argModel.bipedRightArm);
		if(this.leftArm == null) this.leftArm = new ModelPart(argModel, false); this.leftArm.sync((ModelPart)argModel.bipedLeftArm);
		if(this.rightLeg == null) this.rightLeg = new ModelPart(argModel, false); this.rightLeg.sync((ModelPart)argModel.bipedRightLeg);
		if(this.leftLeg == null) this.leftLeg = new ModelPart(argModel, false); this.leftLeg.sync((ModelPart)argModel.bipedLeftLeg);
		if(this.rightForeArm == null) this.rightForeArm = new ModelPart(argModel, false); this.rightForeArm.sync((ModelPart)argModel.bipedRightForeArm);
		if(this.leftForeArm == null) this.leftForeArm = new ModelPart(argModel, false); this.leftForeArm.sync((ModelPart)argModel.bipedLeftForeArm);
		if(this.rightForeLeg == null) this.rightForeLeg = new ModelPart(argModel, false); this.rightForeLeg.sync((ModelPart)argModel.bipedRightForeLeg);
		if(this.leftForeLeg == null) this.leftForeLeg = new ModelPart(argModel, false); this.leftForeLeg.sync((ModelPart)argModel.bipedLeftForeLeg);
		*/
		this.renderOffset.set(argModel.renderOffset);
		this.renderRotation.set(argModel.renderRotation);
	}
	
	public void syncModelInfo(ModelBendsZombieVillager argModel){
		/*if(this.head == null) this.head = new ModelPart(argModel, false); this.head.sync((ModelPart)argModel.bipedHead);
		if(this.headwear == null) this.headwear = new ModelPart(argModel, false); this.headwear.sync((ModelPart)argModel.bipedHeadwear);
		if(this.body == null) this.body = new ModelPart(argModel, false); this.body.sync((ModelPart)argModel.bipedBody);
		if(this.rightArm == null) this.rightArm = new ModelPart(argModel, false); this.rightArm.sync((ModelPart)argModel.bipedRightArm);
		if(this.leftArm == null) this.leftArm = new ModelPart(argModel, false); this.leftArm.sync((ModelPart)argModel.bipedLeftArm);
		if(this.rightLeg == null) this.rightLeg = new ModelPart(argModel, false); this.rightLeg.sync((ModelPart)argModel.bipedRightLeg);
		if(this.leftLeg == null) this.leftLeg = new ModelPart(argModel, false); this.leftLeg.sync((ModelPart)argModel.bipedLeftLeg);
		if(this.rightForeArm == null) this.rightForeArm = new ModelPart(argModel, false); this.rightForeArm.sync((ModelPart)argModel.bipedRightForeArm);
		if(this.leftForeArm == null) this.leftForeArm = new ModelPart(argModel, false); this.leftForeArm.sync((ModelPart)argModel.bipedLeftForeArm);
		if(this.rightForeLeg == null) this.rightForeLeg = new ModelPart(argModel, false); this.rightForeLeg.sync((ModelPart)argModel.bipedRightForeLeg);
		if(this.leftForeLeg == null) this.leftForeLeg = new ModelPart(argModel, false); this.leftForeLeg.sync((ModelPart)argModel.bipedLeftForeLeg);
		*/
		this.renderOffset.set(argModel.renderOffset);
		this.renderRotation.set(argModel.renderRotation);
	}

	@Override
	public void update(float partialTicks) {
		super.update(partialTicks);
		
		this.ticksBeforeStateChange -= DataUpdateHandler.ticksPerFrame;
		
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
	public void onTicksRestart()
	{
	}
}