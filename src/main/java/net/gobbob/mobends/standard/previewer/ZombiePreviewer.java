package net.gobbob.mobends.standard.previewer;

import net.gobbob.mobends.core.animatedentity.Previewer;
import net.gobbob.mobends.standard.data.ZombieData;

public class ZombiePreviewer extends Previewer<ZombieData>
{

	/*
	 * The Entity is generated specifically just for preview, so
	 * it can be manipulated in any way.
	 */
	@Override
	public void prePreview(ZombieData data, String animationToPreview)
	{
		if (animationToPreview.contentEquals("jump"))
		{
			data.overrideOnGroundState(false);
		}
		else
		{
			data.overrideOnGroundState(true);
		}
	}

	@Override
	public void postPreview(ZombieData data, String animationToPreview)
	{
	}

}
