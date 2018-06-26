package net.gobbob.mobends.animation.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.animation.layer.HardAnimationLayer;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.PlayerData;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.pack.variable.BendsVariable;
import net.minecraft.block.Block;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBiped.ArmPose;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

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
	protected AnimationBit bitStand, bitWalk, bitJump, bitSprint, bitSneak;
	protected AnimationBit bitBow, bitAttack;

	public PlayerController()
	{
		this.layerBase = new HardAnimationLayer();
		this.layerSneak = new HardAnimationLayer();
		this.layerAction = new HardAnimationLayer();
		this.bitStand = new net.gobbob.mobends.animation.bit.biped.StandAnimationBit();
		this.bitWalk = new net.gobbob.mobends.animation.bit.biped.WalkAnimationBit();
		this.bitJump = new net.gobbob.mobends.animation.bit.player.JumpAnimationBit();
		this.bitSprint = new net.gobbob.mobends.animation.bit.biped.SprintAnimationBit();
		this.bitSneak = new net.gobbob.mobends.animation.bit.biped.SneakAnimationBit();
		this.bitBow = new net.gobbob.mobends.animation.bit.biped.BowAnimationBit();
		this.bitAttack = new net.gobbob.mobends.animation.bit.player.AttackAnimationBit();
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
		ItemStack itemstack = player.getHeldItemMainhand();
        ItemStack itemstack1 = player.getHeldItemOffhand();
        ModelBiped.ArmPose armPoseMain = ModelBiped.ArmPose.EMPTY;
        ModelBiped.ArmPose armPoseOff = ModelBiped.ArmPose.EMPTY;

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

		if (!playerData.isOnGround() | playerData.getTicksAfterTouchdown() < 2)
		{
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
				{
					this.layerBase.playOrContinueBit(bitSprint, entityData);
				}
				else
				{
					this.layerBase.playOrContinueBit(bitWalk, entityData);
				}
			}
			
			if (player.isSneaking())
			{
				this.layerSneak.playOrContinueBit(bitSneak, entityData);
			}
			else
			{
				this.layerSneak.clearAnimation();
			}
		}

		if(armPoseMain == ArmPose.BOW_AND_ARROW || armPoseOff == ArmPose.BOW_AND_ARROW)
		{
			this.layerAction.playOrContinueBit(this.bitBow, entityData);
		}
		else
		{
			this.layerAction.playOrContinueBit(this.bitAttack, entityData);
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
