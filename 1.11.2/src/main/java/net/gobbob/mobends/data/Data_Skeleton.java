package net.gobbob.mobends.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.entity.ModelBendsSkeleton;
import net.gobbob.mobends.client.model.entity.ModelBendsZombieVillager;
import net.gobbob.mobends.client.renderer.SwordTrail;
import net.gobbob.mobends.settings.SettingsNode;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;

public class Data_Skeleton extends EntityData{
	public static List<Data_Skeleton> dataList = new ArrayList<Data_Skeleton>(); 
	
	public ModelRendererBends head;
    public ModelRendererBends headwear;
    public ModelRendererBends body;
    public ModelRendererBends rightArm;
    public ModelRendererBends leftArm;
    public ModelRendererBends rightLeg;
    public ModelRendererBends leftLeg;
    public ModelRendererBends ears;
    public ModelRendererBends cloak;

    public ModelRendererBends rightForeArm;
    public ModelRendererBends leftForeArm;
    public ModelRendererBends rightForeLeg;
    public ModelRendererBends leftForeLeg;
	
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
		if(this.head == null) this.head = new ModelRendererBends(argModel, false); this.head.sync((ModelRendererBends)argModel.bipedHead);
		if(this.headwear == null) this.headwear = new ModelRendererBends(argModel, false); this.headwear.sync((ModelRendererBends)argModel.bipedHeadwear);
		if(this.body == null) this.body = new ModelRendererBends(argModel, false); this.body.sync((ModelRendererBends)argModel.bipedBody);
		if(this.rightArm == null) this.rightArm = new ModelRendererBends(argModel, false); this.rightArm.sync((ModelRendererBends)argModel.bipedRightArm);
		if(this.leftArm == null) this.leftArm = new ModelRendererBends(argModel, false); this.leftArm.sync((ModelRendererBends)argModel.bipedLeftArm);
		if(this.rightLeg == null) this.rightLeg = new ModelRendererBends(argModel, false); this.rightLeg.sync((ModelRendererBends)argModel.bipedRightLeg);
		if(this.leftLeg == null) this.leftLeg = new ModelRendererBends(argModel, false); this.leftLeg.sync((ModelRendererBends)argModel.bipedLeftLeg);
		if(this.rightForeArm == null) this.rightForeArm = new ModelRendererBends(argModel, false); this.rightForeArm.sync((ModelRendererBends)argModel.bipedRightForeArm);
		if(this.leftForeArm == null) this.leftForeArm = new ModelRendererBends(argModel, false); this.leftForeArm.sync((ModelRendererBends)argModel.bipedLeftForeArm);
		if(this.rightForeLeg == null) this.rightForeLeg = new ModelRendererBends(argModel, false); this.rightForeLeg.sync((ModelRendererBends)argModel.bipedRightForeLeg);
		if(this.leftForeLeg == null) this.leftForeLeg = new ModelRendererBends(argModel, false); this.leftForeLeg.sync((ModelRendererBends)argModel.bipedLeftForeLeg);
		
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