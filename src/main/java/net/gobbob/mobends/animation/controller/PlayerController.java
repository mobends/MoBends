package net.gobbob.mobends.animation.controller;

import java.util.ArrayList;
import java.util.List;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.animation.bit.KeyframeAnimationBit;
import net.gobbob.mobends.animation.bit.biped.BowAnimationBit;
import net.gobbob.mobends.animation.bit.biped.EatingAnimationBit;
import net.gobbob.mobends.animation.bit.biped.FallingAnimationBit;
import net.gobbob.mobends.animation.bit.player.FlyingAnimationBit;
import net.gobbob.mobends.animation.keyframe.ArmatureMask;
import net.gobbob.mobends.animation.layer.HardAnimationLayer;
import net.gobbob.mobends.animation.layer.KeyframeAnimationLayer;
import net.gobbob.mobends.data.BipedEntityData;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.PlayerData;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.pack.variable.BendsVariable;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBiped.ArmPose;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;

/*
 * This is an animation controller for a player instance.
 * It's a part of the EntityData structure.
 */
public class PlayerController extends Controller<PlayerData>
{
	protected final String ANIMATION_TARGET = "player";
	protected HardAnimationLayer<BipedEntityData<?, ?>> layerBase;
	protected HardAnimationLayer<BipedEntityData<?, ?>> layerTorch;
	protected HardAnimationLayer<BipedEntityData<?, ?>> layerSneak;
	protected HardAnimationLayer<BipedEntityData<?, ?>> layerAction;
	protected KeyframeAnimationLayer<PlayerData> layerKeyframe;

	protected AnimationBit<BipedEntityData<?, ?>> bitStand, bitWalk, bitJump, bitSprint, bitSneak, bitLadderClimb,
			bitSwimming, bitRiding, bitSitting, bitFalling;
	protected AnimationBit<PlayerData> bitSprintJump;
	protected AnimationBit<BipedEntityData<?, ?>> bitTorchHolding;
	protected AnimationBit<PlayerData> bitAttack;
	protected FlyingAnimationBit bitFlying;
	protected BowAnimationBit bitBow;
	protected EatingAnimationBit bitEating;
	protected KeyframeAnimationBit<BipedEntityData<?, ?>> bitBreaking;

	protected ArmatureMask upperBodyOnlyMask;

	public PlayerController()
	{
		this.layerBase = new HardAnimationLayer<>();
		this.layerTorch = new HardAnimationLayer<>();
		this.layerSneak = new HardAnimationLayer<>();
		this.layerAction = new HardAnimationLayer<>();
		this.layerKeyframe = new KeyframeAnimationLayer<>();

		this.bitStand = new net.gobbob.mobends.animation.bit.biped.StandAnimationBit<>();
		this.bitWalk = new net.gobbob.mobends.animation.bit.biped.WalkAnimationBit<>();
		this.bitJump = new net.gobbob.mobends.animation.bit.biped.JumpAnimationBit<>();
		this.bitSprint = new net.gobbob.mobends.animation.bit.biped.SprintAnimationBit();
		this.bitSprintJump = new net.gobbob.mobends.animation.bit.player.SprintJumpAnimationBit();
		this.bitSneak = new net.gobbob.mobends.animation.bit.biped.SneakAnimationBit();
		this.bitLadderClimb = new net.gobbob.mobends.animation.bit.biped.LadderClimbAnimationBit();
		this.bitSwimming = new net.gobbob.mobends.animation.bit.biped.SwimmingAnimationBit();
		this.bitRiding = new net.gobbob.mobends.animation.bit.biped.RidingAnimationBit();
		this.bitSitting = new net.gobbob.mobends.animation.bit.biped.SittingAnimationBit();
		this.bitFalling = new net.gobbob.mobends.animation.bit.biped.FallingAnimationBit();
		this.bitFlying = new net.gobbob.mobends.animation.bit.player.FlyingAnimationBit();
		this.bitTorchHolding = new net.gobbob.mobends.animation.bit.biped.TorchHoldingAnimationBit();
		this.bitAttack = new net.gobbob.mobends.animation.bit.player.AttackAnimationBit();
		this.bitBow = new net.gobbob.mobends.animation.bit.biped.BowAnimationBit();
		this.bitEating = new net.gobbob.mobends.animation.bit.biped.EatingAnimationBit();
		this.bitBreaking = new net.gobbob.mobends.animation.bit.biped.BreakingAnimationBit(1.2F);

		this.upperBodyOnlyMask = new ArmatureMask(ArmatureMask.Mode.EXCLUDE_ONLY);
		this.upperBodyOnlyMask.exclude("root");
		this.upperBodyOnlyMask.exclude("head");
		this.upperBodyOnlyMask.exclude("leftLeg");
		this.upperBodyOnlyMask.exclude("leftForeLeg");
		this.upperBodyOnlyMask.exclude("rightLeg");
		this.upperBodyOnlyMask.exclude("rightForeLeg");
	}

	public static boolean shouldPerformBreaking(PlayerData data)
	{
		AbstractClientPlayer player = (AbstractClientPlayer) data.getEntity();
		ItemStack itemstack = player.getHeldItemMainhand();

		if (itemstack != null)
		{
			return itemstack.getItem() instanceof ItemPickaxe || (itemstack.getItem() instanceof ItemAxe);
		}
		return false;
	}

	@Override
	public void perform(PlayerData playerData)
	{
		BendsVariable.tempData = playerData;
		AbstractClientPlayer player = playerData.getEntity();
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
				} else if (enumaction == EnumAction.BOW)
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
				} else if (enumaction1 == EnumAction.BOW)
				{
					armPoseOff = ModelBiped.ArmPose.BOW_AND_ARROW;
				}
			}
		}

		if (player.isRiding())
		{
			if (player.getRidingEntity() instanceof EntityLivingBase)
			{
				this.layerBase.playOrContinueBit(bitRiding, playerData);
			} else
			{
				this.layerBase.playOrContinueBit(bitSitting, playerData);
			}
			this.layerSneak.clearAnimation();
			this.bitBreaking.setMask(this.upperBodyOnlyMask);
		} else
		{
			if (playerData.isClimbing())
			{
				this.layerBase.playOrContinueBit(bitLadderClimb, playerData);
				this.layerSneak.clearAnimation();
				this.layerTorch.clearAnimation();
				this.bitBreaking.setMask(this.upperBodyOnlyMask);
			} else if (player.isInWater())
			{
				this.layerBase.playOrContinueBit(bitSwimming, playerData);
				this.layerSneak.clearAnimation();
				this.layerTorch.clearAnimation();
				this.bitBreaking.setMask(this.upperBodyOnlyMask);
			} else if (!playerData.isOnGround() || playerData.getTicksAfterTouchdown() < 1)
			{
				// Airborne
				if (player.capabilities.isFlying)
				{
					// Flying
					this.layerBase.playOrContinueBit(bitFlying, playerData);
				} else
				{
					if (playerData.getTicksFalling() > FallingAnimationBit.TICKS_BEFORE_FALLING)
					{
						this.layerBase.playOrContinueBit(bitFalling, playerData);
					} else
					{
						if (player.isSprinting())
							this.layerBase.playOrContinueBit(bitSprintJump, playerData);
						else
							this.layerBase.playOrContinueBit(bitJump, playerData);
					}
				}
				this.layerSneak.clearAnimation();
				this.layerTorch.clearAnimation();
				this.bitBreaking.setMask(this.upperBodyOnlyMask);
			} else
			{
				if (playerData.isStillHorizontally())
				{
					this.layerBase.playOrContinueBit(bitStand, playerData);
					this.layerTorch.playOrContinueBit(bitTorchHolding, playerData);
					this.bitBreaking.setMask(null);
				} else
				{
					if (player.isSprinting())
					{
						this.layerBase.playOrContinueBit(bitSprint, playerData);
						this.layerTorch.clearAnimation();
					} else
					{
						this.layerBase.playOrContinueBit(bitWalk, playerData);
						this.layerTorch.playOrContinueBit(bitTorchHolding, playerData);
					}
					this.bitBreaking.setMask(this.upperBodyOnlyMask);
				}

				if (player.isSneaking())
					this.layerSneak.playOrContinueBit(bitSneak, playerData);
				else
					this.layerSneak.clearAnimation();
			}
		}

		/**
		 * ACTIONS
		 */
		if (activeStack != null && activeStack.getItem() instanceof ItemFood)
		{
			this.bitEating.setActionHand(activeHandSide);
			this.layerAction.playOrContinueBit(bitEating, playerData);
		} else
		{
			if (armPoseMain == ArmPose.BOW_AND_ARROW || armPoseOff == ArmPose.BOW_AND_ARROW)
			{
				this.bitBow.setActionHand(armPoseMain == ArmPose.BOW_AND_ARROW ? primaryHand : offHand);
				this.layerAction.playOrContinueBit(this.bitBow, playerData);
			} else if (itemstack != null
					&& (itemstack.getItem() instanceof ItemPickaxe || itemstack.getItem() instanceof ItemAxe))
			{
				if (player.isSwingInProgress)
					this.layerAction.playOrContinueBit(this.bitBreaking, playerData);
				else
					this.layerAction.clearAnimation();
			} else
			{
				this.layerAction.playOrContinueBit(this.bitAttack, playerData);
			}
		}

		List<String> actions = new ArrayList<String>();
		layerBase.perform(playerData, actions);
		layerSneak.perform(playerData, actions);
		layerTorch.perform(playerData, actions);
		layerAction.perform(playerData, actions);
		layerKeyframe.perform(playerData, actions);

		BendsPack.animate(playerData, this.ANIMATION_TARGET, actions);
	}
}
