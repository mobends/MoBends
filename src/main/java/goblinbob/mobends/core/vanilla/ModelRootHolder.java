package goblinbob.mobends.core.vanilla;

import net.minecraft.client.model.geom.ModelPart;

import javax.annotation.Nullable;

/**
 * A vanilla model that keeps the root of its part tree, which the model definitions' part names
 * ({@link VanillaNames#modelPart}) are paths from. Vanilla's models only keep the parts they pose,
 * so a mixin keeps the root as the model is made ({@code HumanoidModelMixin}).
 */
public interface ModelRootHolder {
    @Nullable
    ModelPart mobends$getRoot();
}
