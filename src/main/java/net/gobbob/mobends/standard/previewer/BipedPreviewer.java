package net.gobbob.mobends.standard.previewer;

import net.gobbob.mobends.core.animatedentity.Previewer;
import net.gobbob.mobends.core.client.event.DataUpdateHandler;
import net.gobbob.mobends.standard.data.BipedEntityData;
import net.gobbob.mobends.standard.data.PlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class BipedPreviewer<D extends BipedEntityData> extends Previewer<D>
{
	
	@Override
	public void prePreview(D data, String animationToPreview)
	{
		final float ticks = DataUpdateHandler.getTicks();
		
		data.headYaw.override(0F);
		data.headPitch.override(0F);
		
		switch (animationToPreview)
		{
			case "walk":
				prepareForWalk(data);
				break;
			case "jump":
				prepareForJump(data);
				break;
			default:
				prepareForDefault(data);
		}
	}

	protected void prepareForWalk(D data)
	{
		final float ticks = DataUpdateHandler.getTicks();
		
		data.limbSwing.override(ticks * 0.6F);
		data.overrideOnGroundState(true);
		data.limbSwingAmount.override(1F);
		data.overrideStillness(false);
	}
	
	protected void prepareForJump(D data)
	{
		final float ticks = DataUpdateHandler.getTicks();
		
		final float JUMP_DURATION = 10;
		final float WAIT_DURATION = 10;
		final float TOTAL_DURATION = JUMP_DURATION + WAIT_DURATION;
		float t = ticks % TOTAL_DURATION;
		
		if (t <= JUMP_DURATION)
		{
			data.overrideOnGroundState(false);
			
			double yOffset = Math.sin(t/JUMP_DURATION * Math.PI) * 0.8;
			GlStateManager.translate(0, yOffset, 0);
		} else {
			data.overrideOnGroundState(true);
		}
		
		
		data.limbSwingAmount.override(0F);
		data.overrideStillness(true);
	}
	
	protected void prepareForDefault(D data)
	{
		data.overrideOnGroundState(true);
		data.limbSwingAmount.override(0F);
		data.overrideStillness(true);
	}
	
	@Override
	public void postPreview(D data, String animationToPreview) {}
	
}
