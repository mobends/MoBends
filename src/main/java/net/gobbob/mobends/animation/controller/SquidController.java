package net.gobbob.mobends.animation.controller;

import net.gobbob.mobends.animation.layer.HardAnimationLayer;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.SquidData;
import net.gobbob.mobends.util.GUtil;
import net.minecraft.entity.passive.EntitySquid;

/**
 * This is an animation controller for a squid instance. It's a part of the
 * EntityData structure.
 * 
 * @author Iwo Plaza
 *
 */
public class SquidController extends Controller
{
	final String animationTarget = "squid";
	protected HardAnimationLayer<SquidData> layerBase;

	@Override
	public void perform(EntityData entityData)
	{
		if (!(entityData instanceof SquidData))
			return;
		if (!(entityData.getEntity() instanceof EntitySquid))
			return;

		SquidData data = (SquidData) entityData;
		EntitySquid squid = (EntitySquid) entityData.getEntity();

		float tentacleAngle = squid.lastTentacleAngle
				+ (squid.tentacleAngle - squid.lastTentacleAngle) * DataUpdateHandler.partialTicks;

		for (int i = 0; i < data.squidTentacles.length; ++i)
		{
			double d0 = (double) i * -360.0D / (double) data.squidTentacles.length + 90.0D;
			data.squidTentacles[i][0].rotation.orientX(tentacleAngle * 180.0F / GUtil.PI).rotateY((float) d0);

			for (int j = 1; j < SquidData.TENTACLE_SECTIONS; ++j)
			{
				data.squidTentacles[i][j].rotation.orientX(-tentacleAngle * 10.0F);
			}
		}
	}
}
