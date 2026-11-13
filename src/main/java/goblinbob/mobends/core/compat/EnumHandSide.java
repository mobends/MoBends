package goblinbob.mobends.core.compat;

/** The side of the body a hand is on. In 1.8.9 every entity is right-handed (see {@link Living#getPrimaryHand}). */
public enum EnumHandSide
{
    LEFT,
    RIGHT;

    public EnumHandSide opposite()
    {
        return this == LEFT ? RIGHT : LEFT;
    }
}
