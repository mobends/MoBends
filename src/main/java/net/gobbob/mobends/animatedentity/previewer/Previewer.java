package net.gobbob.mobends.animatedentity.previewer;

import net.minecraft.entity.Entity;

public abstract class Previewer
{
	public abstract void prePreview(Entity entity, String animationToPreview);
	public abstract void postPreview(Entity entity, String animationToPreview);
}
