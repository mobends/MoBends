package goblinbob.mobends.core.bender;

import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.math.vector.IVec3fRead;
import goblinbob.mobends.core.math.vector.Vec3f;

import java.util.Map;

public interface IPreviewer<D extends EntityData<?>>
{

	/**
	 * Gets called right before the entity that the data belongs
	 * to is being previewed.
	 * @param data The data being previewed
	 * @param animationToPreview
	 */
	void prePreview(D data, String animationToPreview);
	
	/**
	 * Gets called right after the entity that the data belongs
	 * to was previewed.
	 * @param data The data the was previewed
	 * @param animationToPreview
	 */
	void postPreview(D data, String animationToPreview);
	
	/**
	 * @return The anchor point that the camera will focus on when
	 * first viewing the entity in a preview form.
	 */
	default IVec3fRead getAnchorPoint() { return Vec3f.ZERO; }
	
	Map<String, BoneMetadata> getBoneMetadata();
	
}
