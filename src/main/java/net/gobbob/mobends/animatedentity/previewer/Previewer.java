package net.gobbob.mobends.animatedentity.previewer;

import net.minecraft.entity.Entity;

public abstract class Previewer<T extends Entity>
{
	public abstract void prePreview(T entity, String animationToPreview);
	public abstract void postPreview(T entity, String animationToPreview);
}
