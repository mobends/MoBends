package goblinbob.mobends.core.util;

import goblinbob.mobends.core.client.Mesh;

public class MeshBuilder
{
	public static void texturedSimpleCube(Mesh builder, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, IColorRead color, int[] tex, int textureWidth, int textureHeight, int faceSize)
	{
		double us = 1F / textureWidth;
		double vs = 1F / textureHeight;
		
		double positions[] = {
			// NEG_X
			minX, maxY, minZ,
			minX, minY, minZ,
			minX, minY, maxZ,
			minX, maxY, maxZ,
	        // POS_X
			maxX, maxY, maxZ,
	        maxX, minY, maxZ,
	        maxX, minY, minZ,
	        maxX, maxY, minZ,
	        // NEG_Z
	        maxX, maxY, minZ,
	        maxX, minY, minZ,
	        minX, minY, minZ,
	        minX, maxY, minZ,
	        // POS_Z
	        minX, maxY, maxZ,
	        minX, minY, maxZ,
	        maxX, minY, maxZ,
	        maxX, maxY, maxZ,
	        // NEG_Y
	        minX, minY, maxZ,
	        minX, minY, minZ,
	        maxX, minY, minZ,
	        maxX, minY, maxZ,
	        // POS_Y
	        maxX, maxY, maxZ,
	        maxX, maxY, minZ,
	        minX, maxY, minZ,
	        minX, maxY, maxZ
		};
		
		int normals[] = {
			-1,  0,  0,
			 1,  0,  0,
			 0,  0, -1,
			 0,  0,  1,
			 0, -1,  0,
			 0,  1,  0
		};
		
		for (int face = 0; face < 6; ++face)
		{
			int posIndex = face * 12;
			double minU = tex[face*2] * us;
			double maxU = (tex[face*2] + faceSize) * us;
			double minV = tex[face*2 + 1] * vs;
			double maxV = (tex[face*2 + 1] + faceSize) * vs;
			// The order of pos/tex/color/normal matters, since the format is POSITION_TEX_COLOR_NORMAL
			builder.pos(positions[posIndex + 0], positions[posIndex + 1], positions[posIndex + 2]  ).tex(minU, minV).color(color).normal(normals[face*3], normals[face*3+1], normals[face*3+2]).endVertex();
			builder.pos(positions[posIndex + 3], positions[posIndex + 4], positions[posIndex + 5]  ).tex(minU, maxV).color(color).normal(normals[face*3], normals[face*3+1], normals[face*3+2]).endVertex();
			builder.pos(positions[posIndex + 6], positions[posIndex + 7], positions[posIndex + 8]  ).tex(maxU, maxV).color(color).normal(normals[face*3], normals[face*3+1], normals[face*3+2]).endVertex();
			builder.pos(positions[posIndex + 9], positions[posIndex + 10], positions[posIndex + 11]).tex(maxU, minV).color(color).normal(normals[face*3], normals[face*3+1], normals[face*3+2]).endVertex();
		}
	}

	/**
	 *
	 * @param mesh The mesh to populate with new geametry.
	 * @param minX The origin of the plane in the x axis.
	 * @param y The origin of the plane in the y axis.
	 * @param minZ The origin of the plane in the z axis.
	 * @param width The width of the plane.
	 * @param length The length of the plane.
	 * @param facingUp Determines the normal direction of the plane.
	 * @param color The color of each vertex
	 * @param tex Texture coordinates in pixels { minU, minV, maxU, maxV }
	 * @param textureWidth The width of the texture.
	 * @param textureHeight The height of the texture.
	 */
	public static void texturedXZPlane(Mesh mesh, double minX, double y, double minZ, double width, double length, boolean facingUp, IColorRead color, int[] tex, int textureWidth, int textureHeight)
	{
		final double us = 1F / textureWidth;
		final double vs = 1F / textureHeight;

		final double maxX = minX + width;
		final double maxZ = minZ + length;

		int normals[] = { 0, facingUp ? -1 : 1,  0 };

		double minU = tex[0] * us;
		double maxU = tex[2] * us;
		double minV = tex[1] * vs;
		double maxV = tex[3] * vs;
		// The order of pos/tex/color/normal matters, since the format is POSITION_TEX_COLOR_NORMAL
		mesh.pos(minX, y, maxZ).tex(minU, minV).color(color).normal(normals[0], normals[1], normals[2]).endVertex();
		mesh.pos(minX, y, minZ).tex(minU, maxV).color(color).normal(normals[0], normals[1], normals[2]).endVertex();
		mesh.pos(maxX, y, minZ).tex(maxU, maxV).color(color).normal(normals[0], normals[1], normals[2]).endVertex();

		mesh.pos(minX, y, maxZ).tex(minU, minV).color(color).normal(normals[0], normals[1], normals[2]).endVertex();
		mesh.pos(maxX, y, minZ).tex(maxU, maxV).color(color).normal(normals[0], normals[1], normals[2]).endVertex();
		mesh.pos(maxX, y, maxZ).tex(maxU, minV).color(color).normal(normals[0], normals[1], normals[2]).endVertex();
	}
}
