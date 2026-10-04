package goblinbob.mobends.core.kumo.api;

@FunctionalInterface
public interface BooleanEvaluator extends Evaluator
{

    /** Whether it holds this frame, from its arguments (already evaluated) and the entity. */
    boolean evaluate(EvalContext context, EvalArgs args);

}
