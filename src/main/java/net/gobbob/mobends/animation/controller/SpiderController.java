package net.gobbob.mobends.animation.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.animation.layer.HardAnimationLayer;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.SpiderData;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.pack.variable.BendsVariable;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;

/*
 * This is an animation controller for a spider instance.
 * It's a part of the EntityData structure.
 */
public class SpiderController extends Controller
{
	protected String animationTarget = "spider";
	protected HardAnimationLayer layerBase;
	protected HardAnimationLayer layerAction;
	protected AnimationBit bitBase;
	protected AnimationBit bitIdle, bitMove, bitJump;
	protected AnimationBit bitDeath;
	
	public SpiderController()
	{
		this.layerBase = new HardAnimationLayer();
		this.layerAction = new HardAnimationLayer();
		this.bitBase = new net.gobbob.mobends.animation.bit.spider.SpiderBaseAnimationBit();
		this.bitIdle = new net.gobbob.mobends.animation.bit.spider.SpiderIdleAnimationBit();
		this.bitMove = new net.gobbob.mobends.animation.bit.spider.SpiderMoveAnimationBit();
		this.bitJump = new net.gobbob.mobends.animation.bit.spider.SpiderJumpAnimationBit();
		this.bitDeath = new net.gobbob.mobends.animation.bit.spider.SpiderDeathAnimationBit();
	}
	
	@Override
	public void perform(EntityData entityData)
	{
		if (!(entityData instanceof SpiderData))
			return;
		if (!(entityData.getEntity() instanceof EntitySpider))
			return;

		SpiderData spiderData = (SpiderData) entityData;
		BendsVariable.tempData = spiderData;
		EntitySpider spider = (EntitySpider) spiderData.getEntity();

		this.layerBase.playOrContinueBit(bitBase, entityData);
		
		if (spider.getHealth() <= 0F)
		{
			this.layerAction.clearAnimation();
			this.layerBase.playOrContinueBit(this.bitDeath, entityData);
		}
		else
		{
			if (!spiderData.isOnGround() || spiderData.getTicksAfterTouchdown() < 1)
			{
				this.layerAction.playOrContinueBit(bitJump, entityData);
			}
			else
			{
				if (spiderData.isStillHorizontally())
				{
					this.layerAction.playOrContinueBit(bitIdle, entityData);
				}
				else
				{
					this.layerAction.playOrContinueBit(bitMove, entityData);
				}
			}
		}
		
		this.layerBase.perform(entityData);
		this.layerAction.perform(entityData);
		
		List<String> actions = new ArrayList<String>();
		actions.addAll(Arrays.asList(layerBase.getActions(entityData)));
		actions.addAll(Arrays.asList(layerAction.getActions(entityData)));
		
		BendsPack.animate(entityData, this.animationTarget, actions);
	}
}
