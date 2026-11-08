package goblinbob.mobends.core.vanilla;

import net.minecraft.client.model.geom.ModelPart;

/**
 * How a vanilla {@code AgeableListModel} draws a baby, which it keeps private (through
 * {@code AgeableListModelMixin}): its head parts made smaller by one scale and moved, the rest by
 * another. A bent baby is drawn the same way, each bent part as the vanilla part it stands for.
 */
public interface AgeableModelLayout {
    /** Whether the head parts are scaled at all (they are only moved otherwise). */
    boolean mobends$scalesHead();

    /** How far down the head parts are moved, in model units. */
    float mobends$babyYHeadOffset();

    /** How far forward the head parts are moved, in model units. */
    float mobends$babyZHeadOffset();

    /** The head parts are drawn {@code 1.5 / this} big. */
    float mobends$babyHeadScale();

    /** The other parts are drawn {@code 1 / this} big. */
    float mobends$babyBodyScale();

    /** How far down the other parts are moved, in model units. */
    float mobends$bodyYOffset();

    /** The parts drawn as the head. */
    Iterable<ModelPart> mobends$headParts();
}
