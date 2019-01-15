package net.gobbob.mobends.core.animatedentity;

import net.gobbob.mobends.core.data.EntityData;
import net.gobbob.mobends.core.util.Vector3;

public interface IPreviewer<D extends EntityData<?>>
{
	void prePreview(D data, String animationToPreview);
	void postPreview(D data, String animationToPreview);
	default Vector3 getAnchorPoint() { return Vector3.ZERO; }
}
