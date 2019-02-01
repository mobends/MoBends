package net.gobbob.mobends.core.client.gui.customize.viewport;

import java.util.HashMap;
import java.util.Map;

import net.gobbob.mobends.core.client.model.IModelPart;
import net.gobbob.mobends.core.math.physics.OBBox;

public class AlterEntryRig
{
	
	Map<String, Bone> nameToBoneMap = new HashMap<>();
	
	public static class Bone
	{
		
		IModelPart part;
		OBBox boundingBox;
		
		public Bone(IModelPart part)
		{
			this.part = part;
			this.boundingBox = new OBBox(
				part.getBounds()
			);
		}
		
	}
	
}
