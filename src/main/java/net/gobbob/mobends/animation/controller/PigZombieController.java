package net.gobbob.mobends.animation.controller;

import java.util.ArrayList;
import java.util.List;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.animation.layer.HardAnimationLayer;
import net.gobbob.mobends.data.BipedEntityData;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.PigZombieData;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.pack.variable.BendsVariable;
import net.minecraft.entity.monster.EntityPigZombie;

/**
 * This is an animation controller for a zombie instance.
 * It's a part of the EntityData structure.
 * 
 * @author Iwo Plaza
 *
 */
public class PigZombieController extends Controller<PigZombieData>
{
	final String animationTarget = "pig_zombie";
	protected HardAnimationLayer<? super BipedEntityData> layerBase;
	protected HardAnimationLayer<? super BipedEntityData> layerAction;
	protected AnimationBit<? extends BipedEntityData> bitStand, bitWalk, bitJump;
	protected AnimationBit<? extends BipedEntityData> bitAttack;
	
	public PigZombieController()
	{
		this.layerBase = new HardAnimationLayer<>();
		this.layerAction = new HardAnimationLayer<>();
		this.bitStand = new net.gobbob.mobends.animation.bit.pigzombie.StandAnimationBit();
		this.bitWalk = new net.gobbob.mobends.animation.bit.pigzombie.WalkAnimationBit();
		this.bitJump = new net.gobbob.mobends.animation.bit.biped.JumpAnimationBit<>();
		this.bitAttack = new net.gobbob.mobends.animation.bit.biped.AttackSlashUpAnimationBit();
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
		
		System.out.println(pigZombie.swingProgress);
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
		
		BendsPack.animate(pigZombieData, this.animationTarget, actions);
	}

}
