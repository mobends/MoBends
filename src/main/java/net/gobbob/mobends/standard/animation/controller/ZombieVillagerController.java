package net.gobbob.mobends.standard.animation.controller;

import net.gobbob.mobends.core.animation.bit.AnimationBit;
import net.gobbob.mobends.core.animation.controller.IAnimationController;
import net.gobbob.mobends.core.animation.layer.HardAnimationLayer;
import net.gobbob.mobends.standard.data.ZombieVillagerData;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is an animation controller for a zombie villager instance.
 * It's a part of the EntityData structure.
 * 
 * @author Iwo Plaza
 *
 */
public class ZombieVillagerController implements IAnimationController<ZombieVillagerData>
{
	
	protected HardAnimationLayer<ZombieVillagerData> layerBase;
	protected HardAnimationLayer<ZombieVillagerData> layerSet;
	protected AnimationBit<ZombieVillagerData> bitStand, bitWalk, bitJump;
	protected AnimationBit<ZombieVillagerData>[] bitAnimationSet;
	
	public ZombieVillagerController()
	{
		this.layerBase = new HardAnimationLayer<>();
		this.layerSet = new HardAnimationLayer<>();
		this.bitStand = new net.gobbob.mobends.standard.animation.bit.biped.StandAnimationBit<>();
		this.bitWalk = new net.gobbob.mobends.standard.animation.bit.biped.WalkAnimationBit<>();
		this.bitJump = new net.gobbob.mobends.standard.animation.bit.biped.JumpAnimationBit<>();
		this.bitAnimationSet = new AnimationBit[] {
			new net.gobbob.mobends.standard.animation.bit.zombie_base.ZombieLeanAnimationBit(),
			new net.gobbob.mobends.standard.animation.bit.zombie_base.ZombieStumblingAnimationBit()
		};
	}
	
	@Override
	public Collection<String> perform(ZombieVillagerData zombieData)
	{
		EntityZombie zombie = zombieData.getEntity();
		EnumHandSide primaryHand = zombie.getPrimaryHand();
		EnumHandSide offHand = primaryHand == EnumHandSide.RIGHT ? EnumHandSide.LEFT : EnumHandSide.RIGHT;
		ItemStack itemstack = zombie.getHeldItemMainhand();
        ItemStack itemstack1 = zombie.getHeldItemOffhand();
        ModelBiped.ArmPose armPoseMain = ModelBiped.ArmPose.EMPTY;
        ModelBiped.ArmPose armPoseOff = ModelBiped.ArmPose.EMPTY;

        if (!itemstack.isEmpty())
        {
            armPoseMain = ModelBiped.ArmPose.ITEM;

            if (zombie.getItemInUseCount() > 0)
            {
                EnumAction enumaction = itemstack.getItemUseAction();

                if (enumaction == EnumAction.BLOCK)
                {
                    armPoseMain = ModelBiped.ArmPose.BLOCK;
                }
                else if (enumaction == EnumAction.BOW)
                {
                    armPoseMain = ModelBiped.ArmPose.BOW_AND_ARROW;
                }
            }
        }
		
        if (!itemstack1.isEmpty())
        {
            armPoseOff = ModelBiped.ArmPose.ITEM;

            if (zombie.getItemInUseCount() > 0)
            {
                EnumAction enumaction1 = itemstack1.getItemUseAction();

                if (enumaction1 == EnumAction.BLOCK)
                {
                    armPoseOff = ModelBiped.ArmPose.BLOCK;
                }
                else if (enumaction1 == EnumAction.BOW)
                {
                    armPoseOff = ModelBiped.ArmPose.BOW_AND_ARROW;
                }
            }
        }

		if (!zombieData.isOnGround() || zombieData.getTicksAfterTouchdown() < 1)
		{
			this.layerBase.playOrContinueBit(bitJump, zombieData);
		}
		else
		{
			if (zombieData.isStillHorizontally())
			{
				this.layerBase.playOrContinueBit(bitStand, zombieData);
			}
			else
			{
				this.layerBase.playOrContinueBit(bitWalk, zombieData);
			}
		}
		
		this.layerSet.playOrContinueBit(bitAnimationSet[zombieData.getAnimationSet()], zombieData);
		
		List<String> actions = new ArrayList<String>();
		this.layerBase.perform(zombieData, actions);
		this.layerSet.perform(zombieData, actions);
		return actions;
	}
	
}
