package net.gobbob.mobends.core.animatedentity;

import java.util.HashSet;
import java.util.Set;

import net.gobbob.mobends.core.data.EntityData;
import net.minecraft.entity.Entity;

public abstract class Previewer<D extends EntityData>
{
	public abstract void prePreview(D data, String animationToPreview);
	public abstract void postPreview(D data, String animationToPreview);
}
