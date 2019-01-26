package net.gobbob.mobends.core.animatedentity;

import net.gobbob.mobends.core.data.EntityData;
import net.gobbob.mobends.core.math.vector.IVec3fRead;
import net.gobbob.mobends.core.math.vector.Vec3f;

public interface IPreviewer<D extends EntityData<?>>
{
	void prePreview(D data, String animationToPreview);
	void postPreview(D data, String animationToPreview);
	default IVec3fRead getAnchorPoint() { return Vec3f.ZERO; }
}
