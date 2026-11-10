package goblinbob.mobends.core.client.model;

import net.minecraft.client.model.geom.ModelPart;

import javax.annotation.Nullable;

/**
 * A model part that knows the root of the tree it was built in ({@code ModelPartMixin}), as
 * {@link ModelRoots} records it. Kept on the part, so the root lives as long as any of its parts
 * does: models keep only the parts they pose, never the root.
 */
public interface TreeRoot {
    @Nullable
    ModelPart mobends$getTreeRoot();

    void mobends$setTreeRoot(ModelPart root);
}
