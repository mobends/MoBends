package net.gobbob.mobends.animation.controller;

import java.util.List;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.animation.layer.AnimationLayer;
import net.gobbob.mobends.animation.layer.HardAnimationLayer;
import net.gobbob.mobends.data.PlayerData;
import net.gobbob.mobends.data.EntityData;

public class PlayerController extends Controller
{
	protected HardAnimationLayer layerBase;

	protected AnimationBit bitStand, bitWalk;

	public PlayerController()
	{
		this.layerBase = new HardAnimationLayer();
		this.bitStand = new net.gobbob.mobends.animation.bit.player.StandAnimationBit(layerBase);
		this.bitWalk = new net.gobbob.mobends.animation.bit.player.WalkAnimationBit(layerBase);
	}

	@Override
	public void perform(EntityData entityData)
	{
		if (!(entityData instanceof PlayerData))
			return;

		PlayerData data = (PlayerData) entityData;

		boolean still = data.motion.x == 0.0f && data.motion.z == 0.0f;
		if (still)
		{
			if (!layerBase.isPlaying(bitStand))
				layerBase.playBit(bitStand);
		}
		else
		{
			if (!layerBase.isPlaying(bitWalk))
				layerBase.playBit(bitWalk);
		}

		layerBase.perform(entityData);
	}
}
