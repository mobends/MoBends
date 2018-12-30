package net.gobbob.mobends.standard.previewer;

import net.gobbob.mobends.core.animatedentity.Previewer;
import net.gobbob.mobends.core.client.event.DataUpdateHandler;
import net.gobbob.mobends.standard.data.SpiderData;
import net.minecraft.client.renderer.GlStateManager;

public class SpiderPreviewer extends Previewer<SpiderData>
{

	/*
	 * The Entity is generated specifically just for preview, so
	 * it can be manipulated in any way.
	 */
	@Override
	public void prePreview(SpiderData data, String animationToPreview)
	{
		data.limbSwingAmount.override(0F);
		
		switch (animationToPreview)
		{
			case "jump":
				{
					final float ticks = DataUpdateHandler.getTicks();
					
					final float JUMP_DURATION = 10;
					final float WAIT_DURATION = 10;
					final float TOTAL_DURATION = JUMP_DURATION + WAIT_DURATION;
					float t = ticks % TOTAL_DURATION;
					
					if (t <= JUMP_DURATION)
					{
						data.overrideOnGroundState(false);
						
						double yOffset = Math.sin(t/JUMP_DURATION * Math.PI) * 1.5;
						GlStateManager.translate(0, yOffset, 0);
					} else {
						data.overrideOnGroundState(true);
					}
					
					data.limbSwingAmount.override(0F);
					data.overrideStillness(true);
				}
				break;
			case "walk":
				final float ticks = DataUpdateHandler.getTicks();
				
				data.limbSwing.override(ticks * 0.6F);
				data.overrideOnGroundState(true);
				data.limbSwingAmount.override(1F);
				data.overrideStillness(false);
				break;
			default:
				data.overrideOnGroundState(true);
		}
	}

	@Override
	public void postPreview(SpiderData data, String animationToPreview)
	{
	}
}
