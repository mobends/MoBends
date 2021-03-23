package goblinbob.mobends.forge;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import goblinbob.mobends.core.IModelPart;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.physics.AABBoxGroup;
import goblinbob.mobends.core.math.physics.IAABBox;
import goblinbob.mobends.core.math.physics.ICollider;
import goblinbob.mobends.core.math.vector.IVec3f;
import goblinbob.mobends.core.math.vector.Vec3f;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class ModelPart extends ModelRenderer implements IForgeModelPart
{
    public Vec3f position = new Vec3f();
    public Vec3f scale = new Vec3f(1, 1, 1);
    public Vec3f offset = new Vec3f();
    public Quaternion rotation = new Quaternion(Quaternion.IDENTITY);
    /**
     * The scale at which animation position offset is applied, used for child models.
     */
    public float offsetScale = 1.0F;
    /**
     * Offset applied before the parent transformation.
     */
    public Vec3f globalOffset = new Vec3f();
    protected List<MutatedBox> mutatedBoxes = new ArrayList<>();

    /**
     * An optional parent.
     */
    protected IForgeModelPart parent;
    protected ICollider collider;

    public ModelPart(Model model, int texOffsetX, int texOffsetY)
    {
        super(model, texOffsetX, texOffsetY);
    }

    public ModelPart(Model model)
    {
        this(model, 0, 0);
    }

    @Override
    public void doRender(MatrixStack.Entry stackEntry, IVertexBuilder vertexBuilder, int lightmapUV, int overlayUV, float red, float green, float blue, float alpha)
    {
        Matrix4f matrix = stackEntry.getMatrix();
        Matrix3f normalMatrix = stackEntry.getNormal();
        ObjectListIterator var11 = this.cubeList.iterator();

        while(var11.hasNext())
        {
            MutatedBox lvt_12_1_ = (MutatedBox) var11.next();
            ModelRenderer.TexturedQuad[] faces = lvt_12_1_.quads;
            int facesCount = faces.length;

            byte tempFlag = lvt_12_1_.faceVisibilityFlag;

            for(int faceIndex = 0; faceIndex < facesCount; ++faceIndex)
            {
                ModelRenderer.TexturedQuad face = faces[faceIndex];
                Vector3f transformedNormal = face.normal.copy();
                transformedNormal.transform(normalMatrix);
                float normalX = transformedNormal.getX();
                float normalY = transformedNormal.getY();
                float normalZ = transformedNormal.getZ();

                // This check is done to not draw hidden
                // faces.
                if ((tempFlag & 1) == 1)
                {
                    for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex)
                    {
                        ModelRenderer.PositionTextureVertex vertex = face.vertexPositions[vertexIndex];
                        float posX = vertex.position.getX() / 16.0F;
                        float posY = vertex.position.getY() / 16.0F;
                        float posZ = vertex.position.getZ() / 16.0F;
                        Vector4f position = new Vector4f(posX, posY, posZ, 1.0F);
                        position.transform(matrix);
                        vertexBuilder.addVertex(position.getX(), position.getY(), position.getZ(), red, green, blue, alpha, vertex.textureU, vertex.textureV, overlayUV, lightmapUV, normalX, normalY, normalZ);
                    }
                }

                tempFlag >>= 1;
            }
        }
    }

    @Override
    public void translateRotate(MatrixStack matrix)
    {
        this.applyCharacterTransform(matrix, 0.0625);
    }

    @Override
    public void applyPreTransform(MatrixStack matrix, double scale)
    {
        if (this.globalOffset.x != 0.0F || this.globalOffset.y != 0.0F || this.globalOffset.z != 0.0F)
            matrix.translate(this.globalOffset.x * scale, this.globalOffset.y * scale, this.globalOffset.z * scale);
    }

    @Override
    public void applyLocalTransform(MatrixStack matrix, double scale)
    {
        if (this.position.x != 0.0F || this.position.y != 0.0F || this.position.z != 0.0F)
            matrix.translate(this.position.x * scale, this.position.y * scale, this.position.z * scale);
        if (this.offset.x != 0.0F || this.offset.y != 0.0F || this.offset.z != 0.0F)
            matrix.translate(this.offset.x * scale * offsetScale, this.offset.y * scale * offsetScale, this.offset.z * scale * offsetScale);

        net.minecraft.util.math.vector.Quaternion quat = new net.minecraft.util.math.vector.Quaternion(this.rotation.x, this.rotation.y, this.rotation.z, this.rotation.w);
        matrix.rotate(quat);
        matrix.scale(this.scale.x, this.scale.y, this.scale.z);
    }

    @Override
    public void setRotationPoint(float x, float y, float z)
    {
        // Ignoring vanilla transformations.
        // The rotation point is often being changed during vanilla animation.
    }

    public ModelPart setPosition(float x, float y, float z)
    {
        this.position.set(x, y, z);
        return this;
    }

    public ModelPart setScale(float x, float y, float z)
    {
        this.scale.x = x;
        this.scale.y = y;
        this.scale.z = z;
        return this;
    }

    public ModelPart resetScale()
    {
        this.scale.set(0, 0, 0);
        return this;
    }

    public BoxFactory developBox(float x, float y, float z, int dx, int dy, int dz, float scaleFactor)
    {
        return new BoxFactory(x, y, z, dx, dy, dz, scaleFactor).setTarget(this);
    }

    public ModelPart addBox(MutatedBox box)
    {
        this.mutatedBoxes.add(box);
        this.cubeList.add(box);
        return this;
    }

    public ModelPart addModelBox(float x, float y, float z, int width, int height, int length, float scaleFactor)
    {
        return this.addBox(new MutatedBox(this, this.getTextureOffsetX(), this.getTextureOffsetY(), x, y, z, width, height, length, scaleFactor));
    }

    public MutatedBox getBox()
    {
        return getBox(0);
    }

    public MutatedBox getBox(int idx)
    {
        return ((MutatedBox) this.cubeList.get(idx));
    }

    @Override
    public Vec3f getPosition()
    {
        return this.position;
    }

    @Override
    public Vec3f getScale()
    {
        return this.scale;
    }

    @Override
    public Vec3f getOffset()
    {
        return this.offset;
    }

    @Override
    public Quaternion getRotation()
    {
        return this.rotation;
    }

    @Override
    public float getOffsetScale()
    {
        return this.offsetScale;
    }

    @Override
    public IVec3f getGlobalOffset()
    {
        return globalOffset;
    }

    @Override
    public IForgeModelPart getParent()
    {
        return this.parent;
    }

    @Override
    public boolean isShowing()
    {
        return this.showModel;
    }

    protected void updateBounds()
    {
        if (this.mutatedBoxes.size() == 1)
        {
            this.collider = this.mutatedBoxes.get(0).createAABB();
        }
        else
        {
            IAABBox[] bounds = new IAABBox[this.mutatedBoxes.size()];
            for (int i = 0; i < bounds.length; ++i)
            {
                bounds[i] = this.mutatedBoxes.get(i).createAABB();
            }

            this.collider = new AABBoxGroup(bounds);
        }
    }

    public ModelPart setMirror(boolean mirror)
    {
        this.mirror = mirror;
        return this;
    }

    @Override
    public void syncUp(IModelPart part)
    {
        if (part == null)
            return;

        this.offset.set(part.getOffset());
        this.rotation.set(part.getRotation());
        this.scale.set(part.getScale());
        this.offsetScale = part.getOffsetScale();
        this.globalOffset.set(part.getGlobalOffset());
    }

    @Override
    public void setVisible(boolean showModel)
    {
        this.showModel = showModel;
    }

    public void setParent(IForgeModelPart parent)
    {
        this.parent = parent;
    }

    @Override
    public void addChild(ModelPart child)
    {
        this.childModels.add(child);
    }

    public int getTextureOffsetX()
    {
        return this.textureOffsetX;
    }

    public int getTextureOffsetY()
    {
        return this.textureOffsetY;
    }

}
