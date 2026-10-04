package goblinbob.mobends.core.kumo.api;

/** Numbers an operation keeps between frames (see {@link BindArgs#slots}). */
public interface FloatArraySlot
{

    int size();

    float get(EvalContext context, int index);

    void set(EvalContext context, int index, float value);

}
