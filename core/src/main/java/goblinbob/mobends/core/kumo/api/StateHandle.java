package goblinbob.mobends.core.kumo.api;

/** A number state a driver steps ({@code inout}) or writes ({@code out}), declared in an animator's scope. */
public interface StateHandle
{

    float get(EvalContext context);

    void set(EvalContext context, float value);

}
