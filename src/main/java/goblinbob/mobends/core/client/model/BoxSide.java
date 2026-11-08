package goblinbob.mobends.core.client.model;

/** A box's faces, in the mod's order (see {@link MutatedBox}). */
public enum BoxSide {
    LEFT(0),
    RIGHT(1),
    TOP(2),
    BOTTOM(3),
    FRONT(4),
    BACK(5);

    final int faceIndex;

    BoxSide(int index) {
        this.faceIndex = index;
    }
}
