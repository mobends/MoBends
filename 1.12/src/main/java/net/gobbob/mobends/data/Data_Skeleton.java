package net.gobbob.mobends.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.model.entity.ModelBendsSkeleton;
import net.gobbob.mobends.client.model.entity.ModelBendsZombieVillager;
import net.gobbob.mobends.client.renderer.SwordTrail;
import net.gobbob.mobends.configuration.Setting;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;

public class Data_Skeleton extends EntityData{
	public static List<Data_Skeleton> dataList = new ArrayList<Data_Skeleton>(); 
	
	public ModelPart head;
    public ModelPart headwear;
    public ModelPart body;
    public ModelPart rightArm;
    public ModelPart leftArm;
    public ModelPart rightLeg;
    public ModelPart leftLeg;
    public ModelPart ears;
    public ModelPart cloak;

    public ModelPart rightForeArm;
    public ModelPart leftForeArm;
    public ModelPart rightForeLeg;
    public ModelPart leftForeLeg;
	
    public SmoothVector3f renderOffset = new SmoothVector3f();
    public SmoothVector3f renderRotation = new SmoothVector3f();
    public SmoothVector3f renderRightItemRotation = new SmoothVector3f();
    public SmoothVector3f renderLeftItemRotation = new SmoothVector3f();
    
    public SwordTrail swordTrail = new SwordTrail();
    
    public int currentWalkingState = 0;
    public float ticksBeforeStateChange = 0;
    public int currentAttack = 0;
    
	public Data_Skeleton(int argEntityID) {
		super(argEntityID);
	}
	
	public void syncModelInfo(ModelBendsSkeleton argModel){
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
		
		if(this.ticksAfterPunch > 10){
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
		if(getEntity().getHeldItem(EnumHand.MAIN_HAND) != null){
			if(this.ticksAfterPunch > 6.0f){
				if(this.currentAttack == 0){
					this.currentAttack = 1;
					this.ticksAfterPunch = 0;
				}
				else{
					if(this.ticksAfterPunch < 15.0f){
						if(this.currentAttack == 1) this.currentAttack = 2;
						else if(this.currentAttack == 2){
							this.currentAttack = 1;
						}
						this.ticksAfterPunch = 0;
					}
				}
			}
		}
	}

	@Override
	public void initModelPose() {
		if(this.body == null) return;
		
		this.setInitialized(true);
	}
}