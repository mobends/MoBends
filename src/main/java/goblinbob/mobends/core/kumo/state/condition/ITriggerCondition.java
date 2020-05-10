package goblinbob.mobends.core.kumo.state.condition;

@FunctionalInterface
public interface ITriggerCondition
{

    boolean isConditionMet(ITriggerConditionContext context);

}
