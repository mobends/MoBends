package net.gobbob.mobends.data;

import net.gobbob.mobends.animation.controller.ControllerPlayer;
import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.mutators.PlayerMutator;
import net.gobbob.mobends.client.renderer.SwordTrail;
import net.gobbob.mobends.main.ModConfig;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;

public class DataPlayer extends DataBiped
{
    public ModelPart ears;
    public ModelPart cloak;
	
    public SmoothVector3f renderOffset = new SmoothVector3f();
    public SmoothVector3f renderRotation = new SmoothVector3f();
    public SmoothVector3f renderRightItemRotation = new SmoothVector3f();
    public SmoothVector3f renderLeftItemRotation = new SmoothVector3f();
    
    public SwordTrail swordTrail = new SwordTrail();
    
    boolean sprintJumpLeg = false;
    boolean sprintJumpLegSwitched = false;
    boolean fistPunchArm = false;
    int currentAttack = 0;
    
	public DataPlayer(Entity entity)
	{
		super(entity);
		this.controller = new ControllerPlayer();
	}
	
	@Override
	public void initModelPose() {
		super.initModelPose();
		
		Render render = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(this.entity);
		
		PlayerMutator mutator = PlayerMutator.getMutatorForRenderer(render);
		if(mutator == null) return;
		
		if(mutator.smallArms) {
			this.rightArm.position.set(-5F, -9.5F, 0F);
			this.leftArm.position.set(5F, -9.5F, 0F);
		}
	}
	
	@Override
	public void update(float partialTicks) {
		super.update(partialTicks);
		
		if(ticksAfterAttack > 20){
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
		if(!(this.entity instanceof AbstractClientPlayer))
			return;
		
		AbstractClientPlayer entityPlayer = (AbstractClientPlayer) this.entity;
		
		if(entityPlayer.getHeldItem(EnumHand.MAIN_HAND).getItem() != Items.AIR)
		{
			if(this.ticksAfterAttack > 6.0f){
				if(this.currentAttack == 0){
					this.currentAttack = 1;
					this.ticksAfterAttack = 0;
				}
				else
				{
					if(this.ticksAfterAttack < 15.0f){
						if(this.currentAttack == 1) this.currentAttack = 2;
						else if(this.currentAttack == 2){
							this.currentAttack = (!ModConfig.performSpinAttack || this.getEntity().isRiding()) ? 1 : 3;
						}
						else if(this.currentAttack == 3) this.currentAttack = 1;
						this.ticksAfterAttack = 0;
					}
				}
			}
		}else{
			this.fistPunchArm = !this.fistPunchArm;
			this.ticksAfterAttack = 0;
		}
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

	@Override
	public void onTicksRestart()
	{
	}
}