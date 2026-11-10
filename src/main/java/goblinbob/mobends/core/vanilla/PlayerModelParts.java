package goblinbob.mobends.core.vanilla;

import net.minecraft.client.model.geom.ModelPart;

/**
 * What vanilla's {@code PlayerModel} keeps private that the bent layers need (through
 * {@code PlayerModelMixin}): whether its arms are slim, and its cape.
 */
public interface PlayerModelParts {
    boolean mobends$isSlim();

    // 26.x's cape is a model of its own.
    //? if <26.1 {
    ModelPart mobends$cloak();
    //?}
}
