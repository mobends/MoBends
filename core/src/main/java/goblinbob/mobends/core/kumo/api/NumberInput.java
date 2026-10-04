package goblinbob.mobends.core.kumo.api;

/** A number a driver reads, evaluated for the frame before the driver is (nothing short-circuits). */
public interface NumberInput
{

    float get(EvalContext context);

    /** The value in double precision, where it has it ({@link DriverBindArgs#entityValue}). */
    double getDouble(EvalContext context);

}
