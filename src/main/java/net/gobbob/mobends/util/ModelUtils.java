package net.gobbob.mobends.util;

import org.lwjgl.util.vector.Vector3f;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.math.AxisAlignedBB;

public class ModelUtils
{
	public static AxisAlignedBB getBounds(ModelRenderer modelRenderer)
	{
		return getBounds(modelRenderer, new Vector3f(0, 0, 0), new AxisAlignedBB(0, 0, 0, 0, 0, 0));
	}
	
	public static AxisAlignedBB getBounds(ModelRenderer modelRenderer, Vector3f position, AxisAlignedBB oldBounds)
	{
		double minX = oldBounds.minX;
		double minY = oldBounds.minY;
		double minZ = oldBounds.minZ;
		double maxX = oldBounds.maxX;
		double maxY = oldBounds.maxY;
		double maxZ = oldBounds.maxZ;
		
		float x = modelRenderer.rotationPointX + position.x;
		float y = modelRenderer.rotationPointY + position.y;
		float z = modelRenderer.rotationPointZ + position.z;
		
		if(modelRenderer.cubeList != null) {
			for(ModelBox box : modelRenderer.cubeList) {
				if(x + box.posX1 < minX)
					minX = x + box.posX1;
				if(y + box.posY1 < minY)
					minY = y + box.posY1;
				if(z + box.posZ1 < minZ)
					minZ = z + box.posZ1;
				if(x + box.posX2 > maxX)
					maxX = x + box.posX2;
				if(y + box.posY2 > maxY)
					maxY = y + box.posY2;
				if(z + box.posZ2 > maxZ)
					maxZ = z + box.posZ2;
			}
		}
		
		AxisAlignedBB newBounds = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
		
		if(modelRenderer.childModels != null)
		for(ModelRenderer child : modelRenderer.childModels) {
			newBounds = getBounds(child, new Vector3f(x, y, z), newBounds);
		}
		
		return newBounds;
	}
}
