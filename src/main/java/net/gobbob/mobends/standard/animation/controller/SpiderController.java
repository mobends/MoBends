package net.gobbob.mobends.standard.animation.controller;

import java.util.ArrayList;
import java.util.List;

import net.gobbob.mobends.core.animation.bit.AnimationBit;
import net.gobbob.mobends.core.animation.controller.Controller;
import net.gobbob.mobends.core.animation.layer.HardAnimationLayer;
import net.gobbob.mobends.core.pack.BendsPack;
import net.gobbob.mobends.core.pack.variable.BendsVariable;
import net.gobbob.mobends.core.util.SmoothOrientation;
import net.gobbob.mobends.standard.data.SpiderData;
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
		this.bitBase = new net.gobbob.mobends.standard.animation.bit.spider.SpiderBaseAnimationBit();
		this.bitIdle = new net.gobbob.mobends.standard.animation.bit.spider.SpiderIdleAnimationBit();
		this.bitMove = new net.gobbob.mobends.standard.animation.bit.spider.SpiderMoveAnimationBit();
		this.bitJump = new net.gobbob.mobends.standard.animation.bit.spider.SpiderJumpAnimationBit();
		this.bitDeath = new net.gobbob.mobends.standard.animation.bit.spider.SpiderDeathAnimationBit();
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
			//this.layerBase.playOrContinueBit(bitBase, spiderData);
			
			if (spiderData.isStillHorizontally())
			{
				this.layerAction.playOrContinueBit(bitIdle, spiderData);
			}
			else
			{
				this.layerAction.playOrContinueBit(bitMove, spiderData);
			}
			
			/*if (!spiderData.isOnGround() || spiderData.getTicksAfterTouchdown() < 1)
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
			}*/
		}
		
		List<String> actions = new ArrayList<String>();
		//this.layerBase.perform(spiderData, actions);
		this.layerAction.perform(spiderData, actions);
		
		BendsPack.animate(spiderData, this.animationTarget, actions);
	}
	
	public static void putLimbOnGround(SmoothOrientation upperLimb, SmoothOrientation lowerLimb, boolean odd, double stretchDistance, double groundLevel)
	{
		final float limbSegmentLength = 12F;
		final float maxStretch = limbSegmentLength * 2;
		
		double c = groundLevel == 0F ? stretchDistance : Math.sqrt(stretchDistance * stretchDistance + groundLevel * groundLevel);
		if (c > maxStretch)
		{
			c = maxStretch;
		}
		
		final double alpha = c > maxStretch ? 0 : Math.acos((c/2)/limbSegmentLength);
		final double beta = Math.atan2(stretchDistance, -groundLevel);
		
		double lowerAngle = Math.max(-2.3, -2 * alpha);
		double upperAngle = Math.min(1, alpha + beta - Math.PI/2);
		upperLimb.localRotateZ((float) (upperAngle / Math.PI * 180) * (odd ? -1 : 1)).finish();
		lowerLimb.orientInstantZ((float) (lowerAngle / Math.PI * 180) * (odd ? -1 : 1));
	}
}
