package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.core.math.SmoothOrientation;
import goblinbob.mobends.standard.animation.bit.spider.*;
import goblinbob.mobends.standard.data.SpiderData;
import goblinbob.mobends.standard.animation.bit.spider.SpiderCrawlAnimationBit;
import net.minecraft.entity.monster.EntitySpider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is an animation controller for a spider instance.
 * It's a part of the EntityData structure.
 *
 * @author Iwo Plaza
 *
 */
public class SpiderController implements IAnimationController<SpiderData>
{

	protected HardAnimationLayer<SpiderData> layerBase;
	protected AnimationBit<SpiderData> bitIdle, bitMove, bitJump;
	protected AnimationBit<SpiderData> bitDeath;
	protected AnimationBit<SpiderData> bitClimb;

	protected boolean resetAfterJumped = false;

	public SpiderController()
	{
		this.layerBase = new HardAnimationLayer<>();
		this.bitIdle = new SpiderIdleAnimationBit();
		this.bitMove = new SpiderMoveAnimationBit();
		this.bitJump = new SpiderJumpAnimationBit();
		this.bitDeath = new SpiderDeathAnimationBit();
		this.bitClimb = new SpiderCrawlAnimationBit();
	}

	@Override
	public Collection<String> perform(SpiderData spiderData)
	{
		final EntitySpider spider = spiderData.getEntity();

		if (spider.getHealth() <= 0F)
		{
			this.layerBase.playOrContinueBit(this.bitDeath, spiderData);
		}
		else
		{
			if (spider.isBesideClimbableBlock())
			{
				this.layerBase.playOrContinueBit(bitClimb, spiderData);
			}
			else
			{
				if (!spiderData.isOnGround() || spiderData.getTicksAfterTouchdown() < 1)
				{
					this.layerBase.playOrContinueBit(bitJump, spiderData);

					if (resetAfterJumped)
						resetAfterJumped = false;
				}
				else
				{
					if (!resetAfterJumped)
					{
						for (SpiderData.Limb limb : spiderData.limbs)
							limb.resetPosition();
						resetAfterJumped = true;
					}

					if (spiderData.isStillHorizontally())
					{
						this.layerBase.playOrContinueBit(bitIdle, spiderData);
					}
					else
					{
						this.layerBase.playOrContinueBit(bitMove, spiderData);
					}
				}
			}
		}

		final List<String> actions = new ArrayList<>();
		this.layerBase.perform(spiderData, actions);
		return actions;
	}

	public static void putLimbOnGround(SmoothOrientation upperLimb, SmoothOrientation lowerLimb, boolean odd, double stretchDistance, double groundLevel)
	{
		putLimbOnGround(upperLimb, lowerLimb, odd, stretchDistance, groundLevel, 1.0F);
		upperLimb.finish();
		lowerLimb.finish();
	}

	public static void putLimbOnGround(SmoothOrientation upperLimb, SmoothOrientation lowerLimb, boolean odd, double stretchDistance, double groundLevel, float smoothness)
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
		upperLimb.setSmoothness(smoothness).localRotateZ((float) (upperAngle / Math.PI * 180) * (odd ? -1 : 1));
		lowerLimb.setSmoothness(smoothness).orientZ((float) (lowerAngle / Math.PI * 180) * (odd ? -1 : 1));
	}



}
