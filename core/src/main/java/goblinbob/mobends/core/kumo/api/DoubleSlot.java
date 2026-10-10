package goblinbob.mobends.core.kumo.api;

/** A double an operation keeps between frames (see {@link BindArgs#doubleSlot}). */
public interface DoubleSlot
{

    double get(EvalContext context);

    void set(EvalContext context, double value);

}
