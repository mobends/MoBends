package goblinbob.mobends.core.client.definition;

import java.util.List;
import java.util.ArrayList;
import goblinbob.mobends.core.math.vector.Vec3f;
import net.minecraft.client.model.ModelRenderer;
import goblinbob.mobends.core.client.model.ModelPart;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.util.GlHelper;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;

/**
 * A {@link ModelPart} with a constant rest rotation applied before the animated one. A part with a
 * parent (a split segment, or a bone declared inside another) is drawn by that parent, inside the
 * parent's transform, so it only applies its own.
 */
public class DefinedModelPart extends ModelPart
{

    private Quaternion restRotation;
    /** What its visibility follows besides its own: the stand-in in the vanilla field, or the bone's first part for a segment. */
    private ModelRenderer visibleWith;
    /** The bone's further segments, which {@link #postRender} goes through. */
    private final List<DefinedModelPart> segments = new ArrayList<>();
    /** Where vanilla attaches to the part, relative to where the bone turns (see {@code BoneDefinition#pivot}). */
    private final Vec3f postShift = new Vec3f();

    public DefinedModelPart(ModelBase model)
    {
        super(model, false, 0, 0);
    }

    public void setVisibleWith(ModelRenderer visibleWith)
    {
        this.visibleWith = visibleWith;
    }

    public void addSegment(DefinedModelPart segment)
    {
        segments.add(segment);
        segment.setVisibleWith(this);
    }

    public void setPostShift(float x, float y, float z)
    {
        postShift.set(x, y, z);
    }

    @Override
    public boolean isShowing()
    {
        return super.isShowing() && (visibleWith == null || (visibleWith instanceof DefinedModelPart
                ? ((DefinedModelPart) visibleWith).isShowing() : visibleWith.showModel && !visibleWith.isHidden));
    }

    /**
     * Where vanilla puts what it attaches to the part: through every segment (a held item follows
     * the forearm), each turning about its own pivot, then back to where the vanilla part turned.
     */
    @Override
    public void postRender(float scale)
    {
        applyCharacterTransform(scale);
        for (DefinedModelPart segment : segments)
        {
            segment.applyLocalTransform(scale);
            GlStateManager.translate(-segment.position.x * scale, -segment.position.y * scale, -segment.position.z * scale);
        }
        if (postShift.x != 0 || postShift.y != 0 || postShift.z != 0)
        {
            GlStateManager.translate(postShift.x * scale, postShift.y * scale, postShift.z * scale);
        }
    }

    public DefinedModelPart setRestRotation(Quaternion restRotation)
    {
        this.restRotation = restRotation;
        return this;
    }

    @Override
    public void render(float scale)
    {
        if (getParent() != null)
        {
            renderJustPart(scale);
            return;
        }
        super.render(scale);
    }

    @Override
    public void renderWithRotation(float scale)
    {
        render(scale);
    }

    @Override
    public void applyLocalTransform(float scale)
    {
        if (restRotation == null)
        {
            super.applyLocalTransform(scale);
            return;
        }
        if (this.position.x != 0.0F || this.position.y != 0.0F || this.position.z != 0.0F)
            GlStateManager.translate(this.position.x * scale * offsetScale, this.position.y * scale * offsetScale, this.position.z * scale * offsetScale);
        if (this.offset.x != 0.0F || this.offset.y != 0.0F || this.offset.z != 0.0F)
            GlStateManager.translate(this.offset.x * scale * offsetScale, this.offset.y * scale * offsetScale, this.offset.z * scale * offsetScale);
        GlHelper.rotate(restRotation);
        GlHelper.rotate(rotation.getSmooth());
        if (this.scale.x != 0.0F || this.scale.y != 0.0F || this.scale.z != 0.0F)
            GlStateManager.scale(this.scale.x, this.scale.y, this.scale.z);
    }

}
