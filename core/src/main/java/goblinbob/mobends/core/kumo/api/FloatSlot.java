package goblinbob.mobends.core.kumo.api;

/** A number an operation keeps between frames (see {@link BindArgs#slot}). */
public interface FloatSlot
{

    float get(EvalContext context);

    void set(EvalContext context, float value);

}
