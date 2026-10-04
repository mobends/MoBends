package goblinbob.mobends.core.kumo.api;

@FunctionalInterface
public interface NumberEvaluator extends Evaluator
{

    /** This frame's value, from its arguments (already evaluated) and the entity. */
    float evaluate(EvalContext context, EvalArgs args);

}
