package goblinbob.mobends.core.kumo.api;

/** A double a driver reads (a position in the world), evaluated for the frame before the driver is. */
public interface DoubleInput
{

    double get(EvalContext context);

}
