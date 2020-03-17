package goblinbob.mobends.core.client.model;

import goblinbob.mobends.core.math.vector.IVec3fRead;
import goblinbob.mobends.core.math.vector.Vec3f;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.TexturedQuad;

public class BoxFactory
{
	/**
	 * The optional ModelPart the box created by this factory should be added
	 * to.
	 */
	ModelPart target;
	final Vec3f min;
	final Vec3f max;
	final TextureFace[] faces;
	int uvWidth, uvHeight, uvLength;
	boolean mirrored;
	byte faceVisibilityFlag;
	
	int textureU, textureV;
	boolean textureUVSet = false;

	public BoxFactory(ModelRenderer renderer, ModelBox source)
	{
		this.min = new Vec3f(source.posX1, source.posY1, source.posZ1);
		this.max = new Vec3f(source.posX2, source.posY2, source.posZ2);
		this.faces = new TextureFace[6];
		this.mirrored = renderer.mirror;
		this.faceVisibilityFlag = 0b111111;
		this.textureU = 0;
		this.textureV = 0;

		TexturedQuad[] quadList = source.quadList;
		if (quadList == null)
		{
			return;
		}

		float textureWidth = renderer.textureWidth;
		float textureHeight = renderer.textureHeight;
		this.textureUVSet = true;

		for (int i = 0; i < 6; ++i)
		{
			if (mirrored)
			{
				this.faces[i] = new TextureFace(
					(int) (quadList[i].vertexPositions[2].texturePositionX * textureWidth),
					(int) (quadList[i].vertexPositions[2].texturePositionY * textureHeight),
					(int) (quadList[i].vertexPositions[0].texturePositionX * textureWidth),
					(int) (quadList[i].vertexPositions[0].texturePositionY * textureHeight)
				);
			}
			else
			{
				this.faces[i] = new TextureFace(
					(int) (quadList[i].vertexPositions[1].texturePositionX * textureWidth),
					(int) (quadList[i].vertexPositions[1].texturePositionY * textureHeight),
					(int) (quadList[i].vertexPositions[3].texturePositionX * textureWidth),
					(int) (quadList[i].vertexPositions[3].texturePositionY * textureHeight)
				);
			}
		}
	}
	
	public BoxFactory(float x, float y, float z, int dx, int dy, int dz, float delta)
	{
		this.min = new Vec3f(x - delta, y - delta, z - delta);
		this.max = new Vec3f(x + dx + delta, y + dy + delta, z + dz + delta);
		this.faces = new TextureFace[6];
		this.uvWidth = dx;
		this.uvHeight = dy;
		this.uvLength = dz;
		this.mirrored = false;
		this.faceVisibilityFlag = 0b111111;
		this.textureU = 0;
		this.textureV = 0;
	}
	
	public BoxFactory(float x0, float y0, float z0, float x1, float y1, float z1, TextureFace[] faces)
	{
		this.min = new Vec3f(x0, y0, z0);
		this.max = new Vec3f(x1, y1, z1);
		this.faces = new TextureFace[6];
		
		for (int i = 0; i < faces.length; ++i)
		{
			this.faces[i] = new TextureFace(faces[i]);
		}
		
		this.mirrored = false;
		this.faceVisibilityFlag = 0b111111;
		this.textureU = 0;
		this.textureV = 0;
		this.textureUVSet = true;
	}
	
	public BoxFactory(IVec3fRead min, IVec3fRead max, TextureFace[] faces)
	{
		this(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ(), faces);
	}
	
	BoxFactory setTarget(ModelPart target)
	{
		this.target = target;
		
		if (!this.textureUVSet)
		{
			this.textureU = this.target.getTextureOffsetX();
			this.textureV = this.target.getTextureOffsetY();
			this.generateTextureFaces();
		}
		
		return this;
	}
	
	public BoxFactory setMinMax(float x0, float y0, float z0, float x1, float y1, float z1)
	{
		this.min.set(x0, y0, z0);
		this.max.set(x1, y1, z1);
		return this;
	}
	
	public BoxFactory setPosSize(float x, float y, float z, float dx, float dy, float dz)
	{
		this.min.set(x, y, z);
		this.max.set(x + dx, y + dy, z + dz);
		return this;
	}
	
	public BoxFactory inflate(float dx, float dy, float dz)
	{
		this.min.add(-dx, -dy, -dz);
		this.max.add(dx, dy, dz);
		return this;
	}
	
	public BoxFactory setWidth(float width)
	{
		this.max.x = this.min.x + width;
		return this;
	}
	
	public BoxFactory setHeight(float height)
	{
		this.max.y = this.min.y + height;
		return this;
	}
	
	public BoxFactory setLength(float length)
	{
		this.max.z = this.min.z + length;
		return this;
	}
	
	public BoxFactory resize(float dx, float dy, float dz)
	{
		this.max.set(this.min.x + dx, this.min.y + dy, this.min.z + dz);
		return this;
	}

	public BoxFactory withUVs(int u, int v)
	{
		this.textureU = u;
		this.textureV = v;
		this.textureUVSet = true;
        this.generateTextureFaces();
		
        return this;
	}
	
	public BoxFactory hideFace(BoxSide face)
	{
		byte mask = 1;
		mask <<= face.faceIndex;
		this.faceVisibilityFlag &= (~mask);
		return this;
	}
	
	public BoxFactory showFace(BoxSide face)
	{
		byte mask = 1;
		mask <<= face.faceIndex;
		this.faceVisibilityFlag |= mask;
		return this;
	}
	
	public BoxFactory mirror()
	{
		this.mirrored = true;
		return this;
	}
	
	public BoxFactory offsetTextureQuad(BoxSide face, float x, float y)
	{
		if (!this.textureUVSet)
		{
			this.textureUVSet = true;
			this.generateTextureFaces();
		}
		
		this.faces[face.faceIndex].u0 += x;
		this.faces[face.faceIndex].u1 += x;
		this.faces[face.faceIndex].v0 += y;
		this.faces[face.faceIndex].v1 += y;
		return this;
	}
	
	public BoxFactory offset(float x, float y, float z)
	{
		this.min.add(x, y, z);
		this.max.add(x, y, z);
		return this;
	}
	
	public MutatedBox create()
	{
		MutatedBox box = new MutatedBox(this.target, this.min, this.max, this.faces, this.faceVisibilityFlag);
		if (this.target != null)
			this.target.addBox(box);
		return box;
	}
	
	public MutatedBox create(ModelRenderer renderer)
	{
		return new MutatedBox(renderer, this.min, this.max, this.faces, this.faceVisibilityFlag);
	}
	
	private void generateTextureFaces()
	{
		int u = this.textureU;
		int v = this.textureV;
		this.faces[0] = new TextureFace(u + uvLength + uvWidth, v + uvLength, u + uvLength + uvWidth + uvLength, v + uvLength + uvHeight);
        this.faces[1] = new TextureFace(u, v + uvLength, u + uvLength, v + uvLength + uvHeight);
        this.faces[2] = new TextureFace(u + uvLength, v, u + uvLength + uvWidth, v + uvLength);
        this.faces[3] = new TextureFace(u + uvLength + uvWidth, v + uvLength, u + uvLength + uvWidth + uvWidth, v);
        this.faces[4] = new TextureFace(u + uvLength, v + uvLength, u + uvLength + uvWidth, v + uvLength + uvHeight);
        this.faces[5] = new TextureFace(u + uvLength + uvWidth + uvLength, v + uvLength, u + uvLength + uvWidth + uvLength + uvWidth, v + uvLength + uvHeight);
	}
	
	static class TextureFace
	{
		
		int u0, v0;
		int u1, v1;
		
		public TextureFace(int u0, int v0, int u1, int v1)
		{
			this.u0 = u0;
			this.v0 = v0;
			this.u1 = u1;
			this.v1 = v1;
		}
		
		public TextureFace(TextureFace face)
		{
			this.u0 = face.u0;
			this.v0 = face.v0;
			this.u1 = face.u1;
			this.v1 = face.v1;
		}
		
	}
	
}
