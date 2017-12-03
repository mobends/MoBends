package net.gobbob.mobends.data;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.client.renderer.SwordTrail;
import net.gobbob.mobends.settings.SettingsNode;
import net.gobbob.mobends.util.BendsLogger;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.client.Minecraft;

public class Data_Player extends EntityData{
	public static List<Data_Player> dataList = new ArrayList<Data_Player>(); 
	
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
    public SmoothVector3f renderItemRotation = new SmoothVector3f();
    
    public SwordTrail swordTrail = new SwordTrail();
    
    public boolean sprintJumpLeg = false;
    public boolean fistPunchArm = false;
    public int currentAttack = 0;
    
	public Data_Player(int argEntityID) {
		super(argEntityID);
	}
	
	public void syncModelInfo(ModelBendsPlayer argModel){
		if(this.head == null) this.head = new ModelRendererBends(argModel); this.head.sync((ModelRendererBends)argModel.bipedHead);
		if(this.headwear == null) this.headwear = new ModelRendererBends(argModel); this.headwear.sync((ModelRendererBends)argModel.bipedHeadwear);
		if(this.body == null) this.body = new ModelRendererBends(argModel); this.body.sync((ModelRendererBends)argModel.bipedBody);
		if(this.rightArm == null) this.rightArm = new ModelRendererBends(argModel); this.rightArm.sync((ModelRendererBends)argModel.bipedRightArm);
		if(this.leftArm == null) this.leftArm = new ModelRendererBends(argModel); this.leftArm.sync((ModelRendererBends)argModel.bipedLeftArm);
		if(this.rightLeg == null) this.rightLeg = new ModelRendererBends(argModel); this.rightLeg.sync((ModelRendererBends)argModel.bipedRightLeg);
		if(this.leftLeg == null) this.leftLeg = new ModelRendererBends(argModel); this.leftLeg.sync((ModelRendererBends)argModel.bipedLeftLeg);
		if(this.rightForeArm == null) this.rightForeArm = new ModelRendererBends(argModel); this.rightForeArm.sync((ModelRendererBends)argModel.bipedRightForeArm);
		if(this.leftForeArm == null) this.leftForeArm = new ModelRendererBends(argModel); this.leftForeArm.sync((ModelRendererBends)argModel.bipedLeftForeArm);
		if(this.rightForeLeg == null) this.rightForeLeg = new ModelRendererBends(argModel); this.rightForeLeg.sync((ModelRendererBends)argModel.bipedRightForeLeg);
		if(this.leftForeLeg == null) this.leftForeLeg = new ModelRendererBends(argModel); this.leftForeLeg.sync((ModelRendererBends)argModel.bipedLeftForeLeg);
		
		this.renderOffset.set(argModel.renderOffset);
		this.renderRotation.set(argModel.renderRotation);
		this.renderItemRotation.set(argModel.renderItemRotation);
	
		this.swordTrail = argModel.swordTrail;
	}
	
	public static void add(Data_Player argData){
		dataList.add((Data_Player) argData);
	}
	
	public static Data_Player get(int argEntityID){
		for(int i = 0;i < dataList.size();i++){
			if(dataList.get(i).entityID == argEntityID){
				return dataList.get(i);
			}
		}
		
		Data_Player newData = new Data_Player(argEntityID);
		
		if(Minecraft.getMinecraft().theWorld.getEntityByID(argEntityID) != null){
			dataList.add((Data_Player) newData);
		}
		
		return newData;
	}

	@Override
	public void update(float argPartialTicks) {
		super.update(argPartialTicks);
		
		if(this.ticksAfterPunch > 20){
			this.currentAttack = 0;
		}
		
		if(this.ticksAfterThrowup-this.ticksPerFrame == 0.0f){
			this.sprintJumpLeg = !this.sprintJumpLeg;
		}
		
		//BendsLogger.log("" + (this.motion.y-this.motion_prev.y),BendsLogger.DEBUG);
	}
	
	@Override
	public void onLiftoff(){
		super.onLiftoff();
		//sprintJumpLeg = !sprintJumpLeg;
	}
	
	@Override
	public void onPunch(){
		if(getEntity().getHeldItem() != null){
			if(this.ticksAfterPunch > 6.0f){
				if(this.currentAttack == 0){
					this.currentAttack = 1;
					this.ticksAfterPunch = 0;
				}
				else{
					if(this.ticksAfterPunch < 15.0f){
						if(this.currentAttack == 1) this.currentAttack = 2;
						else if(this.currentAttack == 2){
							this.currentAttack = (!SettingsNode.getSetting("thirdSwordAttack").getBoolean() || this.getEntity().isRiding()) ? 1 : 3;
						}
						else if(this.currentAttack == 3) this.currentAttack = 1;
						this.ticksAfterPunch = 0;
					}
				}
			}
		}else{
			this.fistPunchArm = !this.fistPunchArm;
			this.ticksAfterPunch = 0;
		}
	}
}