package net.gobbob.mobends.standard.previewer;

import net.gobbob.mobends.core.animatedentity.BoneMetadata;
import net.gobbob.mobends.core.animatedentity.IPreviewer;
import net.gobbob.mobends.core.client.event.DataUpdateHandler;
import net.gobbob.mobends.core.math.vector.IVec3fRead;
import net.gobbob.mobends.core.math.vector.Vec3fReadonly;
import net.gobbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.renderer.GlStateManager;

import java.util.HashMap;
import java.util.Map;

public class BipedPreviewer<D extends BipedEntityData<?>> implements IPreviewer<D>
{
	
	private static final Vec3fReadonly VIEWPORT_ANCHOR = new Vec3fReadonly(0, 1.3F, 0);
	
	private static final Map<String, BoneMetadata> BONE_METADATA = new HashMap<String, BoneMetadata>() {{
		put("head", new BoneMetadata(-4F, -8F, -4F, 4F, 0F, 4F));
		put("body", new BoneMetadata(-4.0F, -12.0F, -2.0F, 4F, 0, 2F));
		put("leftArm", new BoneMetadata(-1.0F, -2.0F, -2.0F, 3F, 4F, 2F));
		put("rightArm", new BoneMetadata(-4F + 1F, -2.0F, -2.0F, 1F, 4F, 2F));
		
//		"body", "leftArm", "rightArm", "leftForeArm", "rightForeArm", "leftLeg", "rightLeg",
//		"leftForeLeg", "rightForeLeg", "totalRotation", "leftItemRotation", "rightItemRotation"
	}};
	
	@Override
	public void prePreview(D data, String animationToPreview)
	{	
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
	
	@Override
	public IVec3fRead getAnchorPoint() { return VIEWPORT_ANCHOR; }

	@Override
	public Map<String, BoneMetadata> getBoneMetadata()
	{
		return BONE_METADATA;
	}
	
}
