package net.gobbob.mobends.standard.animation.controller;

import java.util.ArrayList;
import java.util.List;

import net.gobbob.mobends.core.animation.bit.AnimationBit;
import net.gobbob.mobends.core.animation.controller.IAnimationController;
import net.gobbob.mobends.core.animation.layer.HardAnimationLayer;
import net.gobbob.mobends.core.pack.BendsPack;
import net.gobbob.mobends.core.pack.variable.BendsVariable;
import net.gobbob.mobends.standard.DefaultAddon;
import net.gobbob.mobends.standard.animation.bit.biped.AttackSlashUpAnimationBit;
import net.gobbob.mobends.standard.data.BipedEntityData;
import net.gobbob.mobends.standard.data.PigZombieData;
import net.minecraft.entity.monster.EntityPigZombie;

/**
 * This is an animation controller for a zombie instance.
 * It's a part of the EntityData structure.
 * 
 * @author Iwo Plaza
 *
 */
public class PigZombieController implements IAnimationController<PigZombieData>
{
	
	protected HardAnimationLayer<BipedEntityData<EntityPigZombie>> layerBase;
	protected HardAnimationLayer<BipedEntityData<?>> layerAction;
	protected AnimationBit<? extends BipedEntityData<EntityPigZombie>> bitStand, bitWalk, bitJump;
	protected AttackSlashUpAnimationBit bitAttack;
	
	public PigZombieController()
	{
		this.layerBase = new HardAnimationLayer<>();
		this.layerAction = new HardAnimationLayer<>();
		this.bitStand = new net.gobbob.mobends.standard.animation.bit.pigzombie.StandAnimationBit();
		this.bitWalk = new net.gobbob.mobends.standard.animation.bit.pigzombie.WalkAnimationBit();
		this.bitJump = new net.gobbob.mobends.standard.animation.bit.biped.JumpAnimationBit<>();
		this.bitAttack = new net.gobbob.mobends.standard.animation.bit.biped.AttackSlashUpAnimationBit();
	}
	
	@Override
	public void perform(PigZombieData pigZombieData)
	{
		BendsVariable.tempData = pigZombieData;
		EntityPigZombie pigZombie =  pigZombieData.getEntity();
		
		if (!pigZombieData.isOnGround() || pigZombieData.getTicksAfterTouchdown() < 1)
		{
			this.layerBase.playOrContinueBit(bitJump, pigZombieData);
		}
		else
		{
			if (pigZombieData.isStillHorizontally())
			{
				this.layerBase.playOrContinueBit(bitStand, pigZombieData);
			}
			else
			{
				this.layerBase.playOrContinueBit(bitWalk, pigZombieData);
			}
		}
		
		if (pigZombie.swingProgress > 0)
		{
			this.layerAction.playOrContinueBit(this.bitAttack, pigZombieData);
		}
		else
		{
			this.layerAction.clearAnimation();
		}
		
		List<String> actions = new ArrayList<String>();
		this.layerBase.perform(pigZombieData, actions);
		this.layerAction.perform(pigZombieData, actions);
		
		BendsPack.animate(pigZombieData, DefaultAddon.pigZombieKey, actions);
	}

}
