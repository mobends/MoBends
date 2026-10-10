package goblinbob.mobends.core.kumo.api;

/** Doubles an operation keeps between frames (see {@link BindArgs#doubleSlots}). */
public interface DoubleArraySlot
{

    int size();

    double get(EvalContext context, int index);

    void set(EvalContext context, int index, double value);

}
