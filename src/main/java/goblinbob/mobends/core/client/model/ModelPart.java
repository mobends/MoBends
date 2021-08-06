package goblinbob.mobends.core.client.model;

import goblinbob.mobends.core.math.SmoothOrientation;
import goblinbob.mobends.core.math.TransformUtils;
import goblinbob.mobends.core.math.matrix.IMat4x4d;
import goblinbob.mobends.core.math.physics.AABBoxGroup;
import goblinbob.mobends.core.math.physics.IAABBox;
import goblinbob.mobends.core.math.physics.ICollider;
import goblinbob.mobends.core.math.vector.IVec3f;
import goblinbob.mobends.core.math.vector.Vec3f;
import goblinbob.mobends.core.util.GlHelper;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class ModelPart extends ModelRenderer implements IModelPart
{
    public Vec3f position = new Vec3f();
    public Vec3f scale = new Vec3f(1, 1, 1);
    public Vec3f offset = new Vec3f();
    public SmoothOrientation rotation = new SmoothOrientation();
    /**
     * The scale at which animation position offset is applied, used for child models.
     */
    public float offsetScale = 1.0F;
    /**
     * Offset applied before the parent transformation.
     */
    public Vec3f globalOffset = new Vec3f();
    protected List<MutatedBox> mutatedBoxes;

    /**
     * An optional parent.
     */
    protected IModelPart parent;
    protected ICollider collider;

    public ModelPart(ModelBase model, boolean register, int texOffsetX, int texOffsetY)
    {
        super(model, texOffsetX, texOffsetY);

        this.mutatedBoxes = new ArrayList<MutatedBox>();

        if (!register)
            model.boxList.remove(model.boxList.size() - 1);
    }

    public ModelPart(ModelBase model, boolean register)
    {
        this(model, register, 0, 0);
    }

    public ModelPart(ModelBase model, int texOffsetX, int texOffsetY)
    {
        this(model, true, texOffsetX, texOffsetY);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(float scale)
    {
        this.renderPart(scale);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderWithRotation(float scale)
    {
        this.renderPart(scale);
    }

    @Override
    public void renderPart(float scale)
    {
        if (!(this.isShowing())) return;
        if (!this.compiled)
            this.compileDisplayList(scale);

        GlStateManager.pushMatrix();

        this.applyCharacterTransform(scale);
        GlStateManager.callList(this.displayList);

        if (this.childModels != null)
        {
			for (ModelRenderer childModel : this.childModels)
			{
				childModel.render(scale);
			}
        }

        GlStateManager.popMatrix();
    }

    @Override
    public void renderJustPart(float scale)
    {
        if (!(this.isShowing())) return;
        if (!this.compiled)
            this.compileDisplayList(scale);

        GlStateManager.pushMatrix();

        this.applyLocalTransform(scale);
        GlStateManager.callList(this.displayList);

        if (this.childModels != null)
        {
			for (ModelRenderer childModel : this.childModels)
			{
				childModel.render(scale);
			}
        }

        GlStateManager.popMatrix();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void postRender(float scale)
    {
        this.applyCharacterTransform(scale);
        this.applyPostTransform(scale);
    }

    @Override
    public void applyPreTransform(float scale)
    {
        if (this.globalOffset.x != 0.0F || this.globalOffset.y != 0.0F || this.globalOffset.z != 0.0F)
            GlStateManager.translate(this.globalOffset.x * scale, this.globalOffset.y * scale, this.globalOffset.z * scale);
    }

    @Override
    public void applyPreTransform(float scale, IMat4x4d dest)
    {
        if (this.globalOffset.x != 0.0F || this.globalOffset.y != 0.0F || this.globalOffset.z != 0.0F)
            TransformUtils.translate(dest, this.globalOffset.x * scale, this.globalOffset.y * scale, this.globalOffset.z * scale);
    }

    @Override
    public void applyLocalTransform(float scale)
    {
        if (this.position.x != 0.0F || this.position.y != 0.0F || this.position.z != 0.0F)
            GlStateManager.translate(this.position.x * scale * offsetScale, this.position.y * scale * offsetScale, this.position.z * scale * offsetScale);

        if (this.offset.x != 0.0F || this.offset.y != 0.0F || this.offset.z != 0.0F)
            GlStateManager.translate(this.offset.x * scale * offsetScale, this.offset.y * scale * offsetScale, this.offset.z * scale * offsetScale);

        GlHelper.rotate(rotation.getSmooth());

        if (this.scale.x != 0.0F || this.scale.y != 0.0F || this.scale.z != 0.0F)
            GlStateManager.scale(this.scale.x, this.scale.y, this.scale.z);
    }

    @Override
    public void applyLocalTransform(float scale, IMat4x4d matrix)
    {
        if (this.position.x != 0.0F || this.position.y != 0.0F || this.position.z != 0.0F)
            TransformUtils.translate(matrix, this.position.x * scale * offsetScale, this.position.y * scale * offsetScale, this.position.z * scale * offsetScale);

        if (this.offset.x != 0.0F || this.offset.y != 0.0F || this.offset.z != 0.0F)
            TransformUtils.translate(matrix, this.offset.x * scale * offsetScale, this.offset.y * scale * offsetScale, this.offset.z * scale * offsetScale);

        TransformUtils.rotate(matrix, rotation.getSmooth());

        if (this.scale.x != 0.0F || this.scale.y != 0.0F || this.scale.z != 0.0F)
            TransformUtils.scale(matrix, this.scale.x, this.scale.y, this.scale.z, matrix);
    }

    @Override
    public void applyPostTransform(float scale)
    {
    }

    /**
     * Compiles a GL display list for this model
     */
    @SideOnly(Side.CLIENT)
    protected void compileDisplayList(float scale)
    {
        this.displayList = GLAllocation.generateDisplayLists(1);
        GlStateManager.glNewList(this.displayList, 4864);
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();

		for (net.minecraft.client.model.ModelBox modelBox : this.cubeList)
		{
			modelBox.render(bufferbuilder, scale);
		}

        GlStateManager.glEndList();
        this.compiled = true;
    }

    @Override
    public void update(float ticksPerFrame)
    {
        this.rotation.update(ticksPerFrame);
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

    public ModelPart setOffset(float x, float y, float z)
    {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
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
        this.compiled = false;
        return this;
    }

    public ModelPart addModelBox(float x, float y, float z, int width, int height, int length, float scaleFactor)
    {
        return this.addBox(new MutatedBox(this, this.textureOffsetX, this.textureOffsetY, x, y, z, width, height, length, scaleFactor));
    }

    @Override
    public ModelPart addBox(String partName, float offX, float offY, float offZ, int width, int height, int depth)
    {
        partName = this.boxName + "." + partName;
        TextureOffset textureoffset = this.baseModel.getTextureOffset(partName);
        this.setTextureOffset(textureoffset.textureOffsetX, textureoffset.textureOffsetY);
        return this.addBox((MutatedBox) new MutatedBox(this, this.textureOffsetX, this.textureOffsetY, offX, offY, offZ, width, height, depth, 0.0F).setBoxName(partName));
    }

    @Override
    public ModelPart addBox(float offX, float offY, float offZ, int width, int height, int depth)
    {
        return this.addBox(new MutatedBox(this, this.textureOffsetX, this.textureOffsetY, offX, offY, offZ, width, height, depth, 0.0F));
    }

    @Override
    public ModelPart addBox(float offX, float offY, float offZ, int width, int height, int depth, boolean mirrored)
    {
        return this.addBox(new MutatedBox(this, this.textureOffsetX, this.textureOffsetY, offX, offY, offZ, width, height, depth, 0.0F, mirrored));
    }

    @Override
    public void addBox(float x, float y, float z, int width, int height, int length, float scaleFactor)
    {
        this.addModelBox(x, y, z, width, height, length, scaleFactor);
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
    public SmoothOrientation getRotation()
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
    public IModelPart getParent()
    {
        return this.parent;
    }

    @Override
    public boolean isShowing()
    {
        return this.showModel && !this.isHidden;
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

    public void finish()
    {
        this.rotation.finish();
    }

    @Override
    public void syncUp(IModelPart part)
    {
        if (part == null)
            return;

        this.position.set(part.getPosition());
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

    public ModelPart setParent(IModelPart parent)
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
