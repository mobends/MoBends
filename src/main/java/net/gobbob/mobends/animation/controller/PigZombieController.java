package net.gobbob.mobends.animation.controller;

import java.util.ArrayList;
import java.util.List;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.animation.layer.HardAnimationLayer;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.PigZombieData;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.pack.variable.BendsVariable;
import net.minecraft.entity.monster.EntityZombie;

/**
 * This is an animation controller for a zombie instance.
 * It's a part of the EntityData structure.
 * 
 * @author Iwo Plaza
 *
 */
public class PigZombieController extends Controller
{
	protected String animationTarget = "pig_zombie";
	protected HardAnimationLayer layerBase;
	protected AnimationBit bitStand, bitWalk, bitJump;
	
	public PigZombieController()
	{
		this.layerBase = new HardAnimationLayer();
		this.bitStand = new net.gobbob.mobends.animation.bit.pigzombie.StandAnimationBit();
		this.bitWalk = new net.gobbob.mobends.animation.bit.biped.WalkAnimationBit();
		this.bitJump = new net.gobbob.mobends.animation.bit.biped.JumpAnimationBit();
	}
	
	@Override
	public void perform(EntityData entityData)
	{
		if (!(entityData instanceof PigZombieData))
			return;
		if (!(entityData.getEntity() instanceof EntityZombie))
			return;

		PigZombieData pigZombieData = (PigZombieData) entityData;
		BendsVariable.tempData = pigZombieData;
		
		if (!pigZombieData.isOnGround() || pigZombieData.getTicksAfterTouchdown() < 1)
		{
			this.layerBase.playOrContinueBit(bitJump, entityData);
		}
		else
		{
			if (pigZombieData.isStillHorizontally())
			{
				this.layerBase.playOrContinueBit(bitStand, entityData);
			}
			else
			{
				this.layerBase.playOrContinueBit(bitWalk, entityData);
			}
		}
		
		List<String> actions = new ArrayList<String>();
		this.layerBase.perform(entityData, actions);
		
		BendsPack.animate(entityData, this.animationTarget, actions);
	}

}
