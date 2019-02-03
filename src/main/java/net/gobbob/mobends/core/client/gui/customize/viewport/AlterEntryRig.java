package net.gobbob.mobends.core.client.gui.customize.viewport;

import java.util.HashMap;
import java.util.Map;

import net.gobbob.mobends.core.animatedentity.AlterEntry;
import net.gobbob.mobends.core.animatedentity.BoneMetadata;
import net.gobbob.mobends.core.animatedentity.IPreviewer;
import net.gobbob.mobends.core.client.model.IModelPart;
import net.gobbob.mobends.core.data.LivingEntityData;
import net.gobbob.mobends.core.math.TransformUtils;
import net.gobbob.mobends.core.math.matrix.IMat4x4d;
import net.gobbob.mobends.core.math.matrix.Mat4x4d;
import net.gobbob.mobends.core.math.matrix.MatrixUtils;
import net.gobbob.mobends.core.math.physics.IAABBox;
import net.gobbob.mobends.core.math.physics.IOBBox;
import net.gobbob.mobends.core.math.physics.OBBox;

public class AlterEntryRig
{
	
	private final AlterEntry alterEntry;
	final Map<String, Bone> nameToBoneMap = new HashMap<>();
	
	public AlterEntryRig(AlterEntry alterEntry)
	{
		this.alterEntry = alterEntry;
		IPreviewer previewer = alterEntry.getPreviewer();
		LivingEntityData data = alterEntry.getDataForPreview();
		
		if (previewer == null || data == null)
			return;
		
		Map<String, BoneMetadata> boneMetadata = previewer.getBoneMetadata();
		if (boneMetadata != null)
		{
			boneMetadata.forEach((key, value) -> {
				Object part = data.getPartForName(key);
				
				if (part instanceof IModelPart)
				{
					IModelPart modelPart = (IModelPart) part;
					this.nameToBoneMap.put(key, new Bone(modelPart, value.getBounds()));
				}
			});
		}
	}
	
	public void updateTransform()
	{
		Mat4x4d mat = new Mat4x4d(Mat4x4d.IDENTITY);
		TransformUtils.scale(mat, -1, 1, -1);
		alterEntry.transformModelToCharacterSpace(mat);
		
		this.nameToBoneMap.forEach((key, bone) -> {
			bone.updateTransform(mat);
		});
	}
	
	public static class Bone
	{
		
		final IModelPart part;
		final OBBox collider;
		private boolean hover;
		
		public Bone(IModelPart part, IAABBox bounds)
		{
			this.part = part;
			this.collider = new OBBox(bounds);
			this.hover = false;
		}
		
		public void updateTransform(IMat4x4d parentMat)
		{
			this.collider.transform.copyFrom(parentMat);
			this.part.applyCharacterSpaceTransform(0.0625F, this.collider.transform);
			//TransformUtils.scale(this.collider.transform, 0.5F, 0.5F, 0.5F);
			TransformUtils.scale(this.collider.transform, 0.0625F, 0.0625F, 0.0625F);
		}
		
		public void onHover()
		{
			this.hover = true;
		}
		
		public void onHoverLeave()
		{
			this.hover = false;
		}
		
		public boolean isHoveredOver()
		{
			return this.hover;
		}
	}
	
}
