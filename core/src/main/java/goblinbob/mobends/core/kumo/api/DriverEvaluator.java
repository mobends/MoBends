package goblinbob.mobends.core.kumo.api;

/** How a bound driver poses, every frame its node is posed. */
@FunctionalInterface
public interface DriverEvaluator
{

    void evaluate(EvalContext context, PoseWriter pose);

    /**
     * Called when the node holding the driver is entered, after its declared state is back to
     * its initial values. For what can't be declared state yet: {@code core:step_turn} publishes
     * its outputs at rest.
     */
    default void restart(EvalContext context)
    {
    }

}
