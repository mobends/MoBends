package net.gobbob.mobends.core.animatedentity;

import net.gobbob.mobends.core.data.EntityData;

public abstract class Previewer<D extends EntityData<?>>
{
	public abstract void prePreview(D data, String animationToPreview);
	public abstract void postPreview(D data, String animationToPreview);
}
