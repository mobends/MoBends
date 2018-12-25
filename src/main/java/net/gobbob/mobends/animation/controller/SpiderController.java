package net.gobbob.mobends.animation.controller;

import java.util.ArrayList;
import java.util.List;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.animation.layer.HardAnimationLayer;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.SpiderData;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.pack.variable.BendsVariable;
import net.minecraft.entity.monster.EntitySpider;

/*
 * This is an animation controller for a spider instance.
 * It's a part of the EntityData structure.
 * 
 * @author Iwo Plaza
 * 
 */
public class SpiderController extends Controller
{
	final String animationTarget = "spider";
	protected HardAnimationLayer<SpiderData> layerBase, layerAction;
	protected AnimationBit<SpiderData> bitBase;
	protected AnimationBit<SpiderData> bitIdle, bitMove, bitJump;
	protected AnimationBit<SpiderData> bitDeath;
	
	public SpiderController()
	{
		this.layerBase = new HardAnimationLayer<>();
		this.layerAction = new HardAnimationLayer<>();
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
		
		if (spider.getHealth() <= 0F)
		{
			this.layerAction.clearAnimation();
			this.layerBase.playOrContinueBit(this.bitDeath, spiderData);
		}
		else
		{
			this.layerBase.playOrContinueBit(bitBase, spiderData);
			
			if (!spiderData.isOnGround() || spiderData.getTicksAfterTouchdown() < 1)
			{
				this.layerAction.playOrContinueBit(bitJump, spiderData);
			}
			else
			{
				if (spiderData.isStillHorizontally())
				{
					this.layerAction.playOrContinueBit(bitIdle, spiderData);
				}
				else
				{
					this.layerAction.playOrContinueBit(bitMove, spiderData);
				}
			}
		}
		
		List<String> actions = new ArrayList<String>();
		this.layerBase.perform(spiderData, actions);
		this.layerAction.perform(spiderData, actions);
		
		BendsPack.animate(entityData, this.animationTarget, actions);
	}
}
