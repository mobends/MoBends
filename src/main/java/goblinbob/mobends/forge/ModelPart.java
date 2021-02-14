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
    public void doRender(MatrixStack.Entry p_228306_1_, IVertexBuilder p_228306_2_, int p_228306_3_, int p_228306_4_, float p_228306_5_, float p_228306_6_, float p_228306_7_, float p_228306_8_)
    {
        Matrix4f lvt_9_1_ = p_228306_1_.getMatrix();
        Matrix3f lvt_10_1_ = p_228306_1_.getNormal();
        ObjectListIterator var11 = this.cubeList.iterator();

        while(var11.hasNext())
        {
            MutatedBox lvt_12_1_ = (MutatedBox) var11.next();
            ModelRenderer.TexturedQuad[] var13 = lvt_12_1_.quads;
            int var14 = var13.length;

            byte tempFlag = lvt_12_1_.faceVisibilityFlag;

            for(int var15 = 0; var15 < var14; ++var15)
            {
                ModelRenderer.TexturedQuad lvt_16_1_ = var13[var15];
                Vector3f lvt_17_1_ = lvt_16_1_.normal.copy();
                lvt_17_1_.transform(lvt_10_1_);
                float lvt_18_1_ = lvt_17_1_.getX();
                float lvt_19_1_ = lvt_17_1_.getY();
                float lvt_20_1_ = lvt_17_1_.getZ();

                // This check is done to not draw hidden
                // faces.
                if ((tempFlag & 1) == 1)
                {
                    for (int lvt_21_1_ = 0; lvt_21_1_ < 4; ++lvt_21_1_)
                    {
                        ModelRenderer.PositionTextureVertex lvt_22_1_ = lvt_16_1_.vertexPositions[lvt_21_1_];
                        float lvt_23_1_ = lvt_22_1_.position.getX() / 16.0F;
                        float lvt_24_1_ = lvt_22_1_.position.getY() / 16.0F;
                        float lvt_25_1_ = lvt_22_1_.position.getZ() / 16.0F;
                        Vector4f lvt_26_1_ = new Vector4f(lvt_23_1_, lvt_24_1_, lvt_25_1_, 1.0F);
                        lvt_26_1_.transform(lvt_9_1_);
                        p_228306_2_.addVertex(lvt_26_1_.getX(), lvt_26_1_.getY(), lvt_26_1_.getZ(), p_228306_5_, p_228306_6_, p_228306_7_, p_228306_8_, lvt_22_1_.textureU, lvt_22_1_.textureV, p_228306_4_, p_228306_3_, lvt_18_1_, lvt_19_1_, lvt_20_1_);
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

//        this.position.set(part.getPosition());
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

    public ModelPart setParent(IForgeModelPart parent)
    {
        this.parent = parent;
        return this;
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
