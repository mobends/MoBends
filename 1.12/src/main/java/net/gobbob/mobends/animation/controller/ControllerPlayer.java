package net.gobbob.mobends.animation.controller;

import java.util.List;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.animation.layer.AnimationLayer;
import net.gobbob.mobends.animation.layer.AnimationLayerHard;
import net.gobbob.mobends.data.DataPlayer;
import net.gobbob.mobends.data.EntityData;

public class ControllerPlayer extends Controller
{
	protected AnimationLayerHard layerBase;
	
	protected AnimationBit bitStand, bitWalk;
	
	public ControllerPlayer() {
		this.layerBase = new AnimationLayerHard();
		this.bitStand = new net.gobbob.mobends.animation.bit.player.AnimationStand(layerBase);
		this.bitWalk = new net.gobbob.mobends.animation.bit.player.AnimationWalk(layerBase);
	}
	
	@Override
	public void perform(EntityData entityData)
	{
		if(!(entityData instanceof DataPlayer))
			return;
		
		DataPlayer data = (DataPlayer) entityData;
		
		boolean still = data.motion.x == 0.0f && data.motion.z == 0.0f;
		if(still){
			if(!layerBase.isPlaying(bitStand))
				layerBase.playBit(bitStand);
		}else {
			if(!layerBase.isPlaying(bitWalk))
				layerBase.playBit(bitWalk);
		}
		
		layerBase.perform(entityData);
	}
}
