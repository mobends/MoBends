package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.standard.animation.bit.biped.AttackSlashInwardAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.BowAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.HarvestAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.JumpAnimationBit;
import goblinbob.mobends.standard.animation.bit.skeleton.StandAnimationBit;
import goblinbob.mobends.standard.animation.bit.skeleton.WalkAnimationBit;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.data.SkeletonData;
import goblinbob.mobends.standard.main.ModConfig;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumHandSide;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is an animation controller for a skeleton instance.
 * It's a part of the EntityData structure.
 * 
 * @author Iwo Plaza
 *
 */
public class SkeletonController implements IAnimationController<SkeletonData>
{

	protected HardAnimationLayer<BipedEntityData<EntitySkeleton>> layerBase;
	protected HardAnimationLayer<BipedEntityData<?>> layerAction;
	protected AnimationBit<? extends BipedEntityData<EntitySkeleton>> bitStand, bitWalk, bitJump;

	protected AttackSlashInwardAnimationBit bitAttack = new AttackSlashInwardAnimationBit();
	protected BowAnimationBit bitBow = new BowAnimationBit();
	protected HarvestAnimationBit bitHarvest = new HarvestAnimationBit();

	public SkeletonController()
	{
		this.layerBase = new HardAnimationLayer<>();
		this.layerAction = new HardAnimationLayer<>();

		this.bitStand = new StandAnimationBit();
		this.bitWalk = new WalkAnimationBit();
		this.bitJump = new JumpAnimationBit<>();
	}

	public static boolean isHoldingBow(ModelBiped.ArmPose mainArmPose, ModelBiped.ArmPose offArmPose)
	{
		return mainArmPose == ModelBiped.ArmPose.BOW_AND_ARROW || offArmPose == ModelBiped.ArmPose.BOW_AND_ARROW;
	}

	public static boolean isHoldingWeapon(Item heldItemMainhand)
	{
		return heldItemMainhand instanceof ItemSword || ModConfig.getItemClassification(heldItemMainhand) == ModConfig.ItemClassification.WEAPON;
	}

	public void performActionAnimations(SkeletonData data, EntitySkeleton skeleton)
	{
		final EnumHandSide primaryHand = skeleton.getPrimaryHand();
		final EnumHandSide offHand = primaryHand == EnumHandSide.RIGHT ? EnumHandSide.LEFT : EnumHandSide.RIGHT;
		final ItemStack heldItemMainhand = skeleton.getHeldItemMainhand();
		final ItemStack heldItemOffhand = skeleton.getHeldItemOffhand();
		final ModelBiped.ArmPose armPoseMain = getAction(skeleton, heldItemMainhand);
		final ModelBiped.ArmPose armPoseOff = getAction(skeleton, heldItemOffhand);

		if (isHoldingBow(armPoseMain, armPoseOff))
		{
			bitBow.setActionHand(armPoseMain == ModelBiped.ArmPose.BOW_AND_ARROW ? primaryHand : offHand);
			layerAction.playOrContinueBit(bitBow, data);
		}
		else if (isHoldingWeapon(heldItemMainhand.getItem()) || heldItemMainhand.isEmpty())
		{
			layerAction.playOrContinueBit(bitAttack, data);
		}
		else
		{
			bitHarvest.setActionHand(primaryHand);
			if (skeleton.isSwingInProgress)
				layerAction.playOrContinueBit(bitHarvest, data);
			else
				layerAction.clearAnimation();
		}
	}
	
	@Override
	public Collection<String> perform(SkeletonData skeletonData)
	{
		EntitySkeleton skeleton =  skeletonData.getEntity();
		
		if (!skeletonData.isOnGround() || skeletonData.getTicksAfterTouchdown() < 1)
		{
			this.layerBase.playOrContinueBit(bitJump, skeletonData);
		}
		else
		{
			if (skeletonData.isStillHorizontally())
			{
				this.layerBase.playOrContinueBit(bitStand, skeletonData);
			}
			else
			{
				this.layerBase.playOrContinueBit(bitWalk, skeletonData);
			}
		}

		this.performActionAnimations(skeletonData, skeleton);

		List<String> actions = new ArrayList<>();
		this.layerBase.perform(skeletonData, actions);
		this.layerAction.perform(skeletonData, actions);
		return actions;
	}

	private ModelBiped.ArmPose getAction(EntitySkeleton skeleton, ItemStack heldItem)
	{
		if (!heldItem.isEmpty())
		{
			if (skeleton.getItemInUseCount() > 0)
			{
				EnumAction enumaction = heldItem.getItemUseAction();

				if (enumaction == EnumAction.BLOCK)
					return ModelBiped.ArmPose.BLOCK;
				else if (enumaction == EnumAction.BOW)
					return ModelBiped.ArmPose.BOW_AND_ARROW;
			}

			return ModelBiped.ArmPose.ITEM;
		}

		return ModelBiped.ArmPose.EMPTY;
	}

}
