package net.gobbob.mobends.data;

import java.util.ArrayList;
import java.util.List;

import net.gobbob.mobends.animation.controller.ControllerPlayer;
import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.client.renderer.SwordTrail;
import net.gobbob.mobends.configuration.SettingsManager;
import net.gobbob.mobends.configuration.Setting;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class DataPlayer extends EntityData
{
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
    
    boolean sprintJumpLeg = false;
    boolean sprintJumpLegSwitched = false;
    boolean fistPunchArm = false;
    int currentAttack = 0;
    
	public DataPlayer(int entityId)
	{
		super(entityId);
		this.controller = new ControllerPlayer();
	}
	
	public void syncModelInfo(ModelBendsPlayer model)
	{
		/*if(this.head == null) this.head = new ModelPart(model, false); this.head.sync((ModelPart)model.bipedHead);
		if(this.headwear == null) this.headwear = new ModelPart(model, false); this.headwear.sync((ModelPart)model.bipedHeadwear);
		if(this.body == null) this.body = new ModelPart(model, false); this.body.sync((ModelPart)model.bipedBody);
		if(this.rightArm == null) this.rightArm = new ModelPart(model, false); this.rightArm.sync((ModelPart)model.bipedRightArm);
		if(this.leftArm == null) this.leftArm = new ModelPart(model, false); this.leftArm.sync((ModelPart)model.bipedLeftArm);
		if(this.rightLeg == null) this.rightLeg = new ModelPart(model, false); this.rightLeg.sync((ModelPart)model.bipedRightLeg);
		if(this.leftLeg == null) this.leftLeg = new ModelPart(model, false); this.leftLeg.sync((ModelPart)model.bipedLeftLeg);
		if(this.rightForeArm == null) this.rightForeArm = new ModelPart(model, false); this.rightForeArm.sync((ModelPart)model.bipedRightForeArm);
		if(this.leftForeArm == null) this.leftForeArm = new ModelPart(model, false); this.leftForeArm.sync((ModelPart)model.bipedLeftForeArm);
		if(this.rightForeLeg == null) this.rightForeLeg = new ModelPart(model, false); this.rightForeLeg.sync((ModelPart)model.bipedRightForeLeg);
		if(this.leftForeLeg == null) this.leftForeLeg = new ModelPart(model, false); this.leftForeLeg.sync((ModelPart)model.bipedLeftForeLeg);
		*/
		this.renderOffset.set(model.renderOffset);
		this.renderRotation.set(model.renderRotation);
		this.renderRightItemRotation.set(model.renderRightItemRotation);
		this.renderLeftItemRotation.set(model.renderLeftItemRotation);
		
		this.swordTrail = model.swordTrail;
	}

	@Override
	public void update(float argPartialTicks) {
		super.update(argPartialTicks);
		
		if(ticksAfterPunch > 20){
			currentAttack = 0;
		}
		
		if(!sprintJumpLegSwitched && motion.y > 0) {
			sprintJumpLeg = !sprintJumpLeg;
			sprintJumpLegSwitched = true;
		}
		
		if(motion.y < 0) {
			sprintJumpLegSwitched = false;
		}
	}
	
	@Override
	public void onLiftoff() {
		super.onLiftoff();
		if(!sprintJumpLegSwitched) {
			sprintJumpLeg = !sprintJumpLeg;
			sprintJumpLegSwitched = true;
		}
	}
	
	@Override
	public void onPunch() {
		if(getEntity().getHeldItem(EnumHand.MAIN_HAND).getItem() != Items.AIR){
			if(this.ticksAfterPunch > 6.0f){
				if(this.currentAttack == 0){
					this.currentAttack = 1;
					this.ticksAfterPunch = 0;
				}
				else{
					if(this.ticksAfterPunch < 15.0f){
						if(this.currentAttack == 1) this.currentAttack = 2;
						else if(this.currentAttack == 2){
							this.currentAttack = (!SettingsManager.SPIN_ATTACK.isEnabled() || this.getEntity().isRiding()) ? 1 : 3;
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

	@Override
	public void initModelPose() {
		if(this.body == null) return;
		
		this.setInitialized(true);
	}
	
	public int getCurrentAttack() {
		return currentAttack;
	}
	
	public boolean getFistPunchArm() {
		return fistPunchArm;
	}
	
	public boolean getSprintJumpLeg() {
		return sprintJumpLeg;
	}
}