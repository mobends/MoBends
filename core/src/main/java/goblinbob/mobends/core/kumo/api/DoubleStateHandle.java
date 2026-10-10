package goblinbob.mobends.core.kumo.api;

/** A double state a driver steps ({@code inout}) or writes ({@code out}): a position in the world, say. */
public interface DoubleStateHandle
{

    double get(EvalContext context);

    void set(EvalContext context, double value);

}
