package goblinbob.mobends.forge;

import goblinbob.mobends.core.math.physics.AABBox;
import goblinbob.mobends.core.math.vector.IVec3fRead;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.ModelRenderer.PositionTextureVertex;
import net.minecraft.client.renderer.model.ModelRenderer.TexturedQuad;
import net.minecraft.util.Direction;

public class MutatedBox extends ModelRenderer.ModelBox
{

    /** The (x,y,z) vertex positions and (u,v) texture coordinates for each of the 8 points on a cube */
    public final PositionTextureVertex[] vertexPositions = new PositionTextureVertex[8];

    // We just need 6 bits (6 faces)
    protected final byte faceVisibilityFlag;

    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int TOP = 2;
    public static final int BOTTOM = 3;
    public static final int FRONT = 4;
    public static final int BACK = 5;

    public MutatedBox(ModelRenderer renderer, IVec3fRead min, IVec3fRead max, BoxFactory.TextureFace[] faces, byte faceVisibilityFlag)
    {
        super(0, 0,
                0, 0, 0,
                0, 0, 0,
                0, 0, 0, renderer.mirror, renderer.textureWidth, renderer.textureHeight);
        this.faceVisibilityFlag = faceVisibilityFlag;

        float x0 = min.getX();
        float y0 = min.getY();
        float z0 = min.getZ();
        float x1 = max.getX();
        float y1 = max.getY();
        float z1 = max.getZ();

        if (renderer.mirror)
        {
            float temp = x1;
            x1 = x0;
            x0 = temp;
        }

        PositionTextureVertex positiontexturevertex7 = new PositionTextureVertex(x0, y0, z0, 0.0F, 0.0F);
        PositionTextureVertex positiontexturevertex = new PositionTextureVertex(x1, y0, z0, 0.0F, 8.0F);
        PositionTextureVertex positiontexturevertex1 = new PositionTextureVertex(x1, y1, z0, 8.0F, 8.0F);
        PositionTextureVertex positiontexturevertex2 = new PositionTextureVertex(x0, y1, z0, 8.0F, 0.0F);
        PositionTextureVertex positiontexturevertex3 = new PositionTextureVertex(x0, y0, z1, 0.0F, 0.0F);
        PositionTextureVertex positiontexturevertex4 = new PositionTextureVertex(x1, y0, z1, 0.0F, 8.0F);
        PositionTextureVertex positiontexturevertex5 = new PositionTextureVertex(x1, y1, z1, 8.0F, 8.0F);
        PositionTextureVertex positiontexturevertex6 = new PositionTextureVertex(x0, y1, z1, 8.0F, 0.0F);
        this.vertexPositions[0] = positiontexturevertex7;
        this.vertexPositions[1] = positiontexturevertex;
        this.vertexPositions[2] = positiontexturevertex1;
        this.vertexPositions[3] = positiontexturevertex2;
        this.vertexPositions[4] = positiontexturevertex3;
        this.vertexPositions[5] = positiontexturevertex4;
        this.vertexPositions[6] = positiontexturevertex5;
        this.vertexPositions[7] = positiontexturevertex6;
        this.quads[0] = ModelUtils.createQuad(new PositionTextureVertex[] {positiontexturevertex4, positiontexturevertex, positiontexturevertex1, positiontexturevertex5}, faces[0], renderer.textureWidth, renderer.textureHeight, renderer.mirror, Direction.DOWN);
        this.quads[1] = ModelUtils.createQuad(new PositionTextureVertex[] {positiontexturevertex7, positiontexturevertex3, positiontexturevertex6, positiontexturevertex2}, faces[1], renderer.textureWidth, renderer.textureHeight, renderer.mirror, Direction.UP);
        this.quads[2] = ModelUtils.createQuad(new PositionTextureVertex[] {positiontexturevertex4, positiontexturevertex3, positiontexturevertex7, positiontexturevertex}, faces[2], renderer.textureWidth, renderer.textureHeight, renderer.mirror, Direction.WEST);
        this.quads[3] = ModelUtils.createQuad(new PositionTextureVertex[] {positiontexturevertex1, positiontexturevertex2, positiontexturevertex6, positiontexturevertex5}, faces[3], renderer.textureWidth, renderer.textureHeight, renderer.mirror, Direction.NORTH);
        this.quads[4] = ModelUtils.createQuad(new PositionTextureVertex[] {positiontexturevertex, positiontexturevertex7, positiontexturevertex2, positiontexturevertex1}, faces[4], renderer.textureWidth, renderer.textureHeight, renderer.mirror, Direction.EAST);
        this.quads[5] = ModelUtils.createQuad(new PositionTextureVertex[] {positiontexturevertex3, positiontexturevertex4, positiontexturevertex5, positiontexturevertex6}, faces[5], renderer.textureWidth, renderer.textureHeight, renderer.mirror, Direction.SOUTH);
    }

    public MutatedBox(ModelRenderer modelRenderer, int texU, int texV, float x, float y, float z, int width, int height, int length, float inflationX, float inflationY, float inflationZ, boolean mirrored, byte faceVisibilityFlag)
    {
        super(texU, texV, x, y, z, (int) width, (int) height, (int) length, inflationX, inflationY, inflationZ, mirrored, modelRenderer.textureWidth, modelRenderer.textureHeight);
        this.faceVisibilityFlag = faceVisibilityFlag;
        float f4 = x + (float) width;
        float f5 = y + (float) height;
        float f6 = z + (float) length;
        x -= inflationX;
        y -= inflationY;
        z -= inflationZ;
        f4 += inflationX;
        f5 += inflationY;
        f6 += inflationZ;

        if (mirrored)
        {
            float f7 = f4;
            f4 = x;
            x = f7;
        }

        PositionTextureVertex positiontexturevertex7 = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);
        PositionTextureVertex positiontexturevertex = new PositionTextureVertex(f4, y, z, 0.0F, 8.0F);
        PositionTextureVertex positiontexturevertex1 = new PositionTextureVertex(f4, f5, z, 8.0F, 8.0F);
        PositionTextureVertex positiontexturevertex2 = new PositionTextureVertex(x, f5, z, 8.0F, 0.0F);
        PositionTextureVertex positiontexturevertex3 = new PositionTextureVertex(x, y, f6, 0.0F, 0.0F);
        PositionTextureVertex positiontexturevertex4 = new PositionTextureVertex(f4, y, f6, 0.0F, 8.0F);
        PositionTextureVertex positiontexturevertex5 = new PositionTextureVertex(f4, f5, f6, 8.0F, 8.0F);
        PositionTextureVertex positiontexturevertex6 = new PositionTextureVertex(x, f5, f6, 8.0F, 0.0F);
        this.vertexPositions[0] = positiontexturevertex7;
        this.vertexPositions[1] = positiontexturevertex;
        this.vertexPositions[2] = positiontexturevertex1;
        this.vertexPositions[3] = positiontexturevertex2;
        this.vertexPositions[4] = positiontexturevertex3;
        this.vertexPositions[5] = positiontexturevertex4;
        this.vertexPositions[6] = positiontexturevertex5;
        this.vertexPositions[7] = positiontexturevertex6;
        this.quads[0] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex4, positiontexturevertex, positiontexturevertex1, positiontexturevertex5}, texU + length + width, texV + length, texU + length + width + length, texV + length + height, modelRenderer.textureWidth, modelRenderer.textureHeight, mirrored, Direction.DOWN);
        this.quads[1] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex7, positiontexturevertex3, positiontexturevertex6, positiontexturevertex2}, texU, texV + length, texU + length, texV + length + height, modelRenderer.textureWidth, modelRenderer.textureHeight, mirrored, Direction.UP);
        this.quads[2] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex4, positiontexturevertex3, positiontexturevertex7, positiontexturevertex}, texU + length, texV, texU + length + width, texV + length, modelRenderer.textureWidth, modelRenderer.textureHeight, mirrored, Direction.WEST);
        this.quads[3] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex1, positiontexturevertex2, positiontexturevertex6, positiontexturevertex5}, texU + length + width, texV + length, texU + length + width + width, texV, modelRenderer.textureWidth, modelRenderer.textureHeight, mirrored, Direction.NORTH);
        this.quads[4] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex, positiontexturevertex7, positiontexturevertex2, positiontexturevertex1}, texU + length, texV + length, texU + length + width, texV + length + height, modelRenderer.textureWidth, modelRenderer.textureHeight, mirrored, Direction.EAST);
        this.quads[5] = new TexturedQuad(new PositionTextureVertex[] {positiontexturevertex3, positiontexturevertex4, positiontexturevertex5, positiontexturevertex6}, texU + length + width + length, texV + length, texU + length + width + length + width, texV + length + height, modelRenderer.textureWidth, modelRenderer.textureHeight, mirrored, Direction.SOUTH);
    }

    public MutatedBox(ModelRenderer modelRenderer, int texU, int texV, float x, float y, float z, int width, int height, int length, float inflation)
    {
        this(modelRenderer, texU, texV, x, y, z, width, height, length, inflation, inflation, inflation, modelRenderer.mirror, (byte) 0b111111);
    }

    public byte getFaceVisibilityFlag()
    {
        return faceVisibilityFlag;
    }

    public boolean isFaceVisible(int faceIndex)
    {
        return ((faceVisibilityFlag >> faceIndex) & 1) == 1;
    }

    public AABBox createAABB()
    {
        return new AABBox(this.posX1, this.posY1, this.posZ1, this.posX2, this.posY2, this.posZ2);
    }

}
