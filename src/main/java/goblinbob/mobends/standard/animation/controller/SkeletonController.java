package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.standard.animation.bit.biped.item.BipedActionController;
import goblinbob.mobends.standard.animation.bit.biped.JumpAnimationBit;
import goblinbob.mobends.standard.animation.bit.skeleton.StandAnimationBit;
import goblinbob.mobends.standard.animation.bit.skeleton.WalkAnimationBit;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.data.SkeletonData;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
	protected AnimationBit<? extends BipedEntityData<EntitySkeleton>> bitStand, bitWalk, bitJump;

	protected final BipedActionController actionController = new BipedActionController();

	public SkeletonController()
	{
		this.layerBase = new HardAnimationLayer<>();

		this.bitStand = new StandAnimationBit();
		this.bitWalk = new WalkAnimationBit();
		this.bitJump = new JumpAnimationBit<>();
	}

	public void performActionAnimations(SkeletonData data, EntitySkeleton skeleton)
	{
		final EnumHandSide primaryHand = skeleton.getPrimaryHand();
		final ItemStack heldItemMainhand = skeleton.getHeldItemMainhand();
		final ItemStack heldItemOffhand = skeleton.getHeldItemOffhand();
		final Item activeItem = skeleton.getActiveItemStack().getItem();

		actionController.perform(data, primaryHand, heldItemMainhand, heldItemOffhand, activeItem);
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


		List<String> actions = new ArrayList<>();
		this.layerBase.perform(skeletonData, actions);
		this.performActionAnimations(skeletonData, skeleton);
		return actions;
	}
}
