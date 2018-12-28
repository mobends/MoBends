package net.gobbob.mobends.standard.animation.controller;

import java.util.ArrayList;
import java.util.List;

import net.gobbob.mobends.core.EntityData;
import net.gobbob.mobends.core.animation.bit.AnimationBit;
import net.gobbob.mobends.core.animation.controller.Controller;
import net.gobbob.mobends.core.animation.layer.HardAnimationLayer;
import net.gobbob.mobends.core.pack.BendsPack;
import net.gobbob.mobends.core.pack.variable.BendsVariable;
import net.gobbob.mobends.standard.DefaultAddon;
import net.gobbob.mobends.standard.data.ZombieData;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;

/**
 * This is an animation controller for a zombie instance.
 * It's a part of the EntityData structure.
 * 
 * @author Iwo Plaza
 *
 */
public class ZombieController extends Controller<ZombieData>
{
	protected String animationTarget;
	protected HardAnimationLayer<ZombieData> layerBase;
	protected HardAnimationLayer<ZombieData> layerSet;
	protected AnimationBit<ZombieData> bitStand, bitWalk, bitJump;
	protected AnimationBit<ZombieData>[] bitAnimationSet;
	
	public ZombieController()
	{
		animationTarget = DefaultAddon.ZOMBIE.getKey();
		
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
	public void perform(ZombieData zombieData)
	{
		BendsVariable.tempData = zombieData;
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
		
		BendsPack.animate(zombieData, this.animationTarget, actions);
	}
}
