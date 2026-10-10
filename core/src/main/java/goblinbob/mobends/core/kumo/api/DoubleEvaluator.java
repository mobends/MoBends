package goblinbob.mobends.core.kumo.api;

@FunctionalInterface
public interface DoubleEvaluator extends Evaluator
{

    /** This frame's value, in double precision, from its arguments (already evaluated) and the entity. */
    double evaluate(EvalContext context, EvalArgs args);

}
