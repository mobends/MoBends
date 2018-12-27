package net.gobbob.mobends.animation.controller;

import java.util.ArrayList;
import java.util.List;

import net.gobbob.mobends.core.EntityData;
import net.gobbob.mobends.core.animation.bit.AnimationBit;
import net.gobbob.mobends.core.animation.controller.Controller;
import net.gobbob.mobends.core.animation.layer.HardAnimationLayer;
import net.gobbob.mobends.core.pack.BendsPack;
import net.gobbob.mobends.core.pack.variable.BendsVariable;
import net.gobbob.mobends.data.SpiderData;
import net.minecraft.entity.monster.EntitySpider;

/*
 * This is an animation controller for a spider instance.
 * It's a part of the EntityData structure.
 * 
 * @author Iwo Plaza
 * 
 */
public class SpiderController extends Controller<SpiderData>
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
	public void perform(SpiderData spiderData)
	{
		BendsVariable.tempData = spiderData;
		EntitySpider spider = spiderData.getEntity();
		
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
		
		BendsPack.animate(spiderData, this.animationTarget, actions);
	}
}
