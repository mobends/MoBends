package net.gobbob.mobends.core.util;

import net.gobbob.mobends.core.client.Mesh;

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
			// The order of pos/tex/color matters, since the format is POSITION_TEX_COLOR
			builder.pos(positions[posIndex + 0], positions[posIndex + 1], positions[posIndex + 2]  ).tex(minU, minV).color(color).normal(normals[face*3], normals[face*3+1], normals[face*3+2]).endVertex();
			builder.pos(positions[posIndex + 3], positions[posIndex + 4], positions[posIndex + 5]  ).tex(minU, maxV).color(color).normal(normals[face*3], normals[face*3+1], normals[face*3+2]).endVertex();
			builder.pos(positions[posIndex + 6], positions[posIndex + 7], positions[posIndex + 8]  ).tex(maxU, maxV).color(color).normal(normals[face*3], normals[face*3+1], normals[face*3+2]).endVertex();
			builder.pos(positions[posIndex + 9], positions[posIndex + 10], positions[posIndex + 11]).tex(maxU, minV).color(color).normal(normals[face*3], normals[face*3+1], normals[face*3+2]).endVertex();
		}
	}
}
