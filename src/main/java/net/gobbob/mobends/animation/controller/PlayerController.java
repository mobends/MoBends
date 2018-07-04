package net.gobbob.mobends.animation.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.animation.bit.biped.BowAnimationBit;
import net.gobbob.mobends.animation.bit.biped.EatingAnimationBit;
import net.gobbob.mobends.animation.layer.HardAnimationLayer;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.PlayerData;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.pack.variable.BendsVariable;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBiped.ArmPose;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;

/*
 * This is an animation controller for a player instance.
 * It's a part of the EntityData structure.
 */
public class PlayerController extends Controller
{
	protected String animationTarget = "player";
	protected HardAnimationLayer layerBase;
	protected HardAnimationLayer layerSneak;
	protected HardAnimationLayer layerAction;
	protected AnimationBit bitStand, bitWalk, bitJump, bitSprint, bitSprintJump, bitSneak, bitLadderClimb, bitSwimming;
	protected AnimationBit bitAttack;
	protected BowAnimationBit bitBow;
	protected EatingAnimationBit bitEating;

	public PlayerController()
	{
		this.layerBase = new HardAnimationLayer();
		this.layerSneak = new HardAnimationLayer();
		this.layerAction = new HardAnimationLayer();
		this.bitStand = new net.gobbob.mobends.animation.bit.biped.StandAnimationBit();
		this.bitWalk = new net.gobbob.mobends.animation.bit.biped.WalkAnimationBit();
		this.bitJump = new net.gobbob.mobends.animation.bit.biped.JumpAnimationBit();
		this.bitSprint = new net.gobbob.mobends.animation.bit.biped.SprintAnimationBit();
		this.bitSprintJump = new net.gobbob.mobends.animation.bit.player.SprintJumpAnimationBit();
		this.bitSneak = new net.gobbob.mobends.animation.bit.biped.SneakAnimationBit();
		this.bitLadderClimb = new net.gobbob.mobends.animation.bit.biped.LadderClimbAnimationBit();
		this.bitSwimming = new net.gobbob.mobends.animation.bit.biped.SwimmingAnimationBit();
		this.bitBow = new net.gobbob.mobends.animation.bit.biped.BowAnimationBit();
		this.bitAttack = new net.gobbob.mobends.animation.bit.player.AttackAnimationBit();
		this.bitEating = new net.gobbob.mobends.animation.bit.biped.EatingAnimationBit();
	}

	@Override
	public void perform(EntityData entityData)
	{
		if (!(entityData instanceof PlayerData))
			return;
		if (!(entityData.getEntity() instanceof AbstractClientPlayer))
			return;

		PlayerData playerData = (PlayerData) entityData;
		BendsVariable.tempData = playerData;
		AbstractClientPlayer player = (AbstractClientPlayer) playerData.getEntity();
		EnumHandSide primaryHand = player.getPrimaryHand();
		EnumHandSide offHand = primaryHand == EnumHandSide.RIGHT ? EnumHandSide.LEFT : EnumHandSide.RIGHT;
		ItemStack itemstack = player.getHeldItemMainhand();
        ItemStack itemstack1 = player.getHeldItemOffhand();
        ItemStack activeStack = player.getActiveItemStack();
        ModelBiped.ArmPose armPoseMain = ModelBiped.ArmPose.EMPTY;
        ModelBiped.ArmPose armPoseOff = ModelBiped.ArmPose.EMPTY;
        EnumHand activeHand = player.getActiveHand();
        EnumHandSide activeHandSide = activeHand == EnumHand.MAIN_HAND ? primaryHand : offHand;
        
        if (!itemstack.isEmpty())
        {
            armPoseMain = ModelBiped.ArmPose.ITEM;

            if (player.getItemInUseCount() > 0)
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

            if (player.getItemInUseCount() > 0)
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

        if(playerData.isClimbing())
        {
        	this.layerBase.playOrContinueBit(bitLadderClimb, entityData);
        	this.layerSneak.clearAnimation();
		}
        else if(player.isInWater())
        {
			this.layerBase.playOrContinueBit(bitSwimming, entityData);
			this.layerSneak.clearAnimation();
		}
        else if (!playerData.isOnGround() || playerData.getTicksAfterTouchdown() < 1)
		{
			if (player.isSprinting())
				this.layerBase.playOrContinueBit(bitSprintJump, entityData);
			else
				this.layerBase.playOrContinueBit(bitJump, entityData);
			this.layerSneak.clearAnimation();
		}
		else
		{
			if (playerData.isStillHorizontally())
			{
				this.layerBase.playOrContinueBit(bitStand, entityData);
			}
			else
			{
				if (player.isSprinting())
					this.layerBase.playOrContinueBit(bitSprint, entityData);
				else
					this.layerBase.playOrContinueBit(bitWalk, entityData);
			}
			
			if (player.isSneaking())
				this.layerSneak.playOrContinueBit(bitSneak, entityData);
			else
				this.layerSneak.clearAnimation();
		}
		
        if (activeStack != null && activeStack.getItem() instanceof ItemFood)
		{
        	this.bitEating.setActionHand(activeHandSide);
        	this.layerAction.playOrContinueBit(bitEating, entityData);
		}
        else
        {
			if(armPoseMain == ArmPose.BOW_AND_ARROW || armPoseOff == ArmPose.BOW_AND_ARROW)
			{
				this.bitBow.setActionHand(armPoseMain == ArmPose.BOW_AND_ARROW ? primaryHand : offHand);
				this.layerAction.playOrContinueBit(this.bitBow, entityData);
			}
			else
			{
				this.layerAction.playOrContinueBit(this.bitAttack, entityData);
			}
        }
		
		layerBase.perform(entityData);
		layerSneak.perform(entityData);
		layerAction.perform(entityData);
		
		List<String> actions = new ArrayList<String>();
		actions.addAll(Arrays.asList(layerBase.getActions(entityData)));
		actions.addAll(Arrays.asList(layerSneak.getActions(entityData)));
		actions.addAll(Arrays.asList(layerAction.getActions(entityData)));
		
		BendsPack.animate(entityData, this.animationTarget, actions);
	}
}
