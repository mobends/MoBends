package net.gobbob.mobends.core.animation.bit;

import net.gobbob.mobends.core.animation.keyframe.*;
import net.gobbob.mobends.core.client.event.DataUpdateHandler;
import net.gobbob.mobends.core.client.model.IModelPart;
import net.gobbob.mobends.core.data.EntityData;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Map;

public class KeyframeAnimationBit<T extends EntityData<?>> extends AnimationBit<T>
{

	protected KeyframeAnimation performedAnimation = null;
	private ArmatureMask mask = null;
	private TriggerMode triggerMode = TriggerMode.RETRIGGER;
	private float keyframeIndex = 0;
	private float animationSpeed;
	
	public KeyframeAnimationBit(ResourceLocation animationResource, float animationSpeed)
	{
		this.animationSpeed = animationSpeed;

		try
		{
			this.performedAnimation = AnimationLoader.loadFromResource(animationResource);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void setAnimation(KeyframeAnimation animation)
	{
		this.performedAnimation = animation;
	}
	
	public void clearAnimation()
	{
		this.performedAnimation = null;
	}
	
	public void setMask(ArmatureMask mask)
	{
		this.mask = mask;
	}
	
	public void setAnimationSpeed(float animationSpeed)
	{
		this.animationSpeed = animationSpeed;
	}
	
	public KeyframeAnimationBit<T> setTriggerMode(TriggerMode triggerMode)
	{
		this.triggerMode = triggerMode;
		return this;
	}
	
	@Override
	public void onPlay(T entityData)
	{
		if (this.triggerMode == TriggerMode.RETRIGGER)
			this.keyframeIndex = 0F;
	}
	
	@Override
	public void perform(T entityData)
	{
		if (this.performedAnimation != null)
		{
			int minKeyframes = Integer.MAX_VALUE;

			int index = (int) keyframeIndex;
			int nextIndex = index + 1;
			if (nextIndex >= minKeyframes - 1)
				nextIndex = minKeyframes - 1;
			float progress = keyframeIndex - index;

			if (this.performedAnimation.bones.containsKey("root") && shouldPartBeAffected("root"))
			{
				Bone rootBone = this.performedAnimation.bones.get("root");
				Keyframe keyframe = rootBone.keyframes.get(index);
				Keyframe nextFrame = rootBone.keyframes.get(nextIndex);

				float x = keyframe.position[0] + (nextFrame.position[0] - keyframe.position[0]) * progress;
				float y = keyframe.position[1] + (nextFrame.position[1] - keyframe.position[1]) * progress;
				float z = keyframe.position[2] + (nextFrame.position[2] - keyframe.position[2]) * progress;

				entityData.globalOffset.set(z, x, y);
			}

			for (Map.Entry<String, Bone> entry : this.performedAnimation.bones.entrySet())
			{
				Bone bone = entry.getValue();
				minKeyframes = bone.keyframes.size();

				Object part = entityData.getPartForName(entry.getKey());

				if (part != null && shouldPartBeAffected(entry.getKey()))
				{
					Keyframe keyframe = bone.keyframes.get(index);
					Keyframe nextFrame = bone.keyframes.get(nextIndex);

					if (keyframe != null && nextFrame != null)
					{
						if (part instanceof IModelPart)
						{
							IModelPart box = (IModelPart) part;
							float x0 = keyframe.rotation[0];
							float y0 = keyframe.rotation[1];
							float z0 = keyframe.rotation[2];
							float w0 = keyframe.rotation[3];
							float x1 = nextFrame.rotation[0];
							float y1 = nextFrame.rotation[1];
							float z1 = nextFrame.rotation[2];
							float w1 = nextFrame.rotation[3];

							box.getRotation().set(x0 + (x1-x0) * progress,
									y0 + (y1-y0) * progress,
									z0 + (z1-z0) * progress,
									w0 + (w1-w0) * progress);
						}
					}
				}
			}

			keyframeIndex += DataUpdateHandler.ticksPerFrame * this.animationSpeed;
			if (keyframeIndex >= minKeyframes - 1)
				keyframeIndex -= minKeyframes - 1;
		}
	}

	private boolean shouldPartBeAffected(String partName)
	{
		return mask == null || mask.doesAllow(partName);
	}

	@Override
	public String[] getActions(T entityData)
	{
		return null;
	}
	
	public static enum TriggerMode
	{
		RETRIGGER,
		CONTINUE
	}

}
