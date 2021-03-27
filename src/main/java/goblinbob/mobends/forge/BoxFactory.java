package goblinbob.mobends.forge;

import goblinbob.mobends.core.math.vector.IVec3fRead;
import goblinbob.mobends.core.math.vector.Vec3f;
import goblinbob.mobends.core.mutation.BoxSide;
import goblinbob.mobends.core.mutation.FaceRotation;
import net.minecraft.client.renderer.model.ModelRenderer;

public class BoxFactory
{
    /**
     * The optional ModelPart the box created by this factory should be added
     * to.
     */
    public ModelPart target;
    public final Vec3f min;
    public final Vec3f max;
    public final TextureFace[] faces;
    public int uvWidth;
    public int uvHeight;
    public int uvLength;
    public boolean mirrored;
    public byte faceVisibilityFlag;

    int textureU, textureV;
    boolean textureUVSet = false;

    public BoxFactory(ModelRenderer renderer, ModelRenderer.ModelBox source)
    {
        this.min = new Vec3f(source.minX, source.minY, source.minZ);
        this.max = new Vec3f(source.maxX, source.maxY, source.maxZ);
        this.faces = new TextureFace[6];
        this.mirrored = renderer.mirror;
        this.faceVisibilityFlag = 0b111111;
        this.textureU = 0;
        this.textureV = 0;

        ModelRenderer.TexturedQuad[] quadList = source.polygons;
        if (quadList == null)
        {
            return;
        }

        float textureWidth = renderer.xTexSize;
        float textureHeight = renderer.yTexSize;
        this.textureUVSet = true;

        for (int i = 0; i < 6; ++i)
        {
            if (mirrored)
            {
                final ModelRenderer.PositionTextureVertex startVertex = quadList[i].vertices[2];
                final ModelRenderer.PositionTextureVertex endVertex = quadList[i].vertices[0];
                this.faces[i] = new TextureFace(
                        (int) (startVertex.u * textureWidth),
                        (int) (startVertex.v * textureHeight),
                        (int) ((endVertex.u - startVertex.u) * textureWidth),
                        (int) ((endVertex.v - startVertex.v) * textureHeight)
                );
            }
            else
            {
                final ModelRenderer.PositionTextureVertex startVertex = quadList[i].vertices[1];
                final ModelRenderer.PositionTextureVertex endVertex = quadList[i].vertices[3];
                this.faces[i] = new TextureFace(
                        (int) (startVertex.u * textureWidth),
                        (int) (startVertex.v * textureHeight),
                        (int) ((endVertex.u - startVertex.u) * textureWidth),
                        (int) ((endVertex.v - startVertex.v) * textureHeight)
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

        this.faces[face.faceIndex].uPos += x;
        this.faces[face.faceIndex].vPos += y;
        return this;
    }

    public BoxFactory rotateTextureQuad(BoxSide face, FaceRotation rotation)
    {
        if (!this.textureUVSet)
        {
            this.textureUVSet = true;
            this.generateTextureFaces();
        }

        this.faces[face.faceIndex].faceRotation = rotation;

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

        this.faces[0] = new TextureFace(u + uvLength + uvWidth, v + uvLength, uvLength, uvHeight);
        this.faces[1] = new TextureFace(u, v + uvLength, uvLength, uvHeight);
        this.faces[2] = new TextureFace(u + uvLength, v, uvWidth, uvLength);
        this.faces[3] = new TextureFace(u + uvLength + uvWidth, v + uvLength, uvWidth, -uvLength);
        this.faces[4] = new TextureFace(u + uvLength, v + uvLength, uvWidth, uvHeight);
        this.faces[5] = new TextureFace(u + uvLength + uvWidth + uvLength, v + uvLength, uvWidth, uvHeight);
    }

    public static class TextureFace
    {

        public int uPos;
        public int vPos;
        public int uSize;
        public int vSize;
        public FaceRotation faceRotation = FaceRotation.IDENTITY;

        public TextureFace(int uPos, int vPos, int uSize, int vSize)
        {
            this.uPos = uPos;
            this.vPos = vPos;
            this.uSize = uSize;
            this.vSize = vSize;
        }

        public TextureFace(TextureFace face)
        {
            this.uPos = face.uPos;
            this.vPos = face.vPos;
            this.uSize = face.uSize;
            this.vSize = face.vSize;
        }

    }

}
