package net.gobbob.mobends.editor.viewport;

import net.gobbob.mobends.core.animatedentity.AnimatedEntity;
import net.gobbob.mobends.core.animatedentity.BoneMetadata;
import net.gobbob.mobends.core.animatedentity.IPreviewer;
import net.gobbob.mobends.core.client.model.IModelPart;
import net.gobbob.mobends.core.data.LivingEntityData;
import net.gobbob.mobends.core.math.TransformUtils;
import net.gobbob.mobends.core.math.matrix.IMat4x4d;
import net.gobbob.mobends.core.math.matrix.Mat4x4d;
import net.gobbob.mobends.core.math.physics.IAABBox;
import net.gobbob.mobends.core.math.physics.OBBox;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class AlterEntryRig
{

    private final AnimatedEntity<?> animatedEntity;
    final Map<String, Bone> nameToBoneMap = new HashMap<>();
    private Bone hoveredOverBone = null;
    private Bone selectedBone = null;

    public AlterEntryRig(AnimatedEntity<?> animatedEntity)
    {
        this.animatedEntity = animatedEntity;
        IPreviewer<?> previewer = animatedEntity.getPreviewer();
        LivingEntityData data = animatedEntity.getDataForPreview();

        if (previewer == null || data == null)
            return;

        Map<String, BoneMetadata> boneMetadata = previewer.getBoneMetadata();
        if (boneMetadata != null)
        {
            boneMetadata.forEach((key, value) -> {
                Object part = data.getPartForName(key);

                if (part instanceof IModelPart)
                {
                    IModelPart modelPart = (IModelPart) part;
                    this.nameToBoneMap.put(key, new Bone(key, modelPart, value.getBounds()));
                }
            });
        }
    }

    public void updateTransform()
    {
        Mat4x4d mat = new Mat4x4d(Mat4x4d.IDENTITY);
        TransformUtils.scale(mat, -1, 1, -1);
        animatedEntity.transformModelToCharacterSpace(mat);
        TransformUtils.scale(mat, 0.0625F, 0.0625F, 0.0625F);

        LivingEntityData data = animatedEntity.getDataForPreview();

        TransformUtils.translate(mat, data.renderOffset.getX(), -data.renderOffset.getY(), data.renderOffset.getZ());

        this.nameToBoneMap.forEach((key, bone) -> {
            bone.updateTransform(mat);
        });
    }

    public void hoverOver(@Nullable Bone bone)
    {
        this.hoveredOverBone = bone;
    }

    public void select(@Nullable Bone bone)
    {
        this.selectedBone = bone;
    }

    public boolean isBoneHoveredOver(Bone bone)
    {
        return bone == this.hoveredOverBone;
    }

    public boolean isBoneSelected(Bone bone)
    {
        return bone == this.selectedBone;
    }

    @Nullable
    public Bone getBone(String name)
    {
        return this.nameToBoneMap.get(name);
    }

    public AnimatedEntity<?> getAnimatedEntity()
    {
        return this.animatedEntity;
    }

    public static class Bone
    {

        final String name;
        final IModelPart part;
        /**
         * Can be null in case the bone represents something like item rotation.
         */
        final OBBox collider;

        public Bone(String name, IModelPart part)
        {
            this.name = name;
            this.part = part;
            this.collider = null;
        }

        public Bone(String name, IModelPart part, IAABBox bounds)
        {
            this.name = name;
            this.part = part;
            this.collider = new OBBox(bounds);
        }

        public void updateTransform(IMat4x4d parentMat)
        {
            if (this.collider != null)
            {
                this.collider.transform.copyFrom(parentMat);
                this.part.applyCharacterSpaceTransform(1, this.collider.transform);
            }
        }

        public String getName()
        {
            return this.name;
        }

    }

}
