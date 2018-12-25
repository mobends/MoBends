package net.gobbob.mobends.animation.controller;

import net.gobbob.mobends.animation.layer.HardAnimationLayer;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.ZombieData;

/**
 * This is an animation controller for a squid instance.
 * It's a part of the EntityData structure.
 * 
 * @author Iwo Plaza
 *
 */
public class SquidController extends Controller
{
	final String animationTarget = "squid";
	protected HardAnimationLayer<ZombieData> layerBase;
	
	@Override
	public void perform(EntityData entityData)
	{
	}
}
