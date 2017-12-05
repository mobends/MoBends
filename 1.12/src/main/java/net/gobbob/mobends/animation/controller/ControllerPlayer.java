package net.gobbob.mobends.animation.controller;

import java.util.List;

import net.gobbob.mobends.animation.layer.AnimationLayer;
import net.gobbob.mobends.animation.layer.AnimationLayerHard;
import net.gobbob.mobends.data.DataPlayer;
import net.gobbob.mobends.data.EntityData;

public class ControllerPlayer extends Controller
{
	protected AnimationLayerHard layerBase;
	
	public ControllerPlayer() {
		layerBase = new AnimationLayerHard();
		layerBase.playBit(new net.gobbob.mobends.animation.bit.player.AnimationStand(layerBase));
	}
	
	@Override
	public void perform(EntityData entityData)
	{
		if(!(entityData instanceof DataPlayer))
			return;
		
		DataPlayer data = (DataPlayer) entityData;
		//data.body.rotation.set(90, 0, 0);
		layerBase.perform(entityData);
	}
}
