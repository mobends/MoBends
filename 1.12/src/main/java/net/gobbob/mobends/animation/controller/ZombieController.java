package net.gobbob.mobends.animation.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.animation.layer.HardAnimationLayer;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.ZombieData;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.pack.variable.BendsVariable;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;

/*
 * This is an animation controller for a zombie instance.
 * It's a part of the EntityData structure.
 */
public class ZombieController extends Controller
{
	protected String animationTarget = "zombie";
	protected HardAnimationLayer layerBase;
	protected AnimationBit bitStand, bitWalk, bitJump;
	
	public ZombieController()
	{
		this.layerBase = new HardAnimationLayer();
		this.bitStand = new net.gobbob.mobends.animation.bit.biped.StandAnimationBit();
		this.bitWalk = new net.gobbob.mobends.animation.bit.biped.WalkAnimationBit();
		this.bitJump = new net.gobbob.mobends.animation.bit.biped.JumpAnimationBit();
	}
	
	@Override
	public void perform(EntityData entityData)
	{
		if (!(entityData instanceof ZombieData))
			return;
		if (!(entityData.getEntity() instanceof EntityZombie))
			return;

		ZombieData zombieData = (ZombieData) entityData;
		BendsVariable.tempData = zombieData;
		EntityZombie zombie = (EntityZombie) zombieData.getEntity();
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

		if (!zombieData.isOnGround() | zombieData.getTicksAfterTouchdown() < 2)
		{
			this.layerBase.playOrContinueBit(bitJump, entityData);
		}
		else
		{
			if (zombieData.isStillHorizontally())
			{
				this.layerBase.playOrContinueBit(bitStand, entityData);
			}
			else
			{
				this.layerBase.playOrContinueBit(bitWalk, entityData);
			}
		}
		
		this.layerBase.perform(entityData);
		
		List<String> actions = new ArrayList<String>();
		actions.addAll(Arrays.asList(layerBase.getActions(entityData)));
		
		BendsPack.animate(entityData, this.animationTarget, actions);
	}
}
