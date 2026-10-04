package goblinbob.mobends.core.kumo.api;

/** A condition a driver reads, evaluated for the frame before the driver is. */
public interface BooleanInput
{

    boolean get(EvalContext context);

}
