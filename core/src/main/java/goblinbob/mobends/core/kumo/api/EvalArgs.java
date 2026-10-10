package goblinbob.mobends.core.kumo.api;

/**
 * An operation's arguments, evaluated for this frame (every one of them: nothing short-circuits).
 * The view is reused from frame to frame; don't keep it.
 */
public interface EvalArgs
{

    int count();

    /** A number or constant argument. */
    float number(int index);

    /** A double argument. */
    double doubleNumber(int index);

    /** A boolean argument. */
    boolean bool(int index);

}
