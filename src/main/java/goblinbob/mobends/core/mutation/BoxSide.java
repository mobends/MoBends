package goblinbob.mobends.core.mutation;

public enum BoxSide
{
    LEFT(0),
    RIGHT(1),
    TOP(2),
    BOTTOM(3),
    FRONT(4),
    BACK(5);

    public final int faceIndex;

    BoxSide(int index)
    {
        this.faceIndex = index;
    }

}
