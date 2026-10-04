package goblinbob.mobends.standard.kumo;

import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionOperations;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;

import static goblinbob.mobends.core.kumo.expr.ExpressionOperations.choice;
import static goblinbob.mobends.core.kumo.expr.ExpressionOperations.params;

/**
 * {@code mobends:use_action} and {@code mobends:attack_action}: how Mo' Bends classifies the item
 * in use and the held item's attack ({@link goblinbob.mobends.standard.ItemActions}, which the
 * config can override), as the bipeds' {@code useActionType} and {@code attackActionType}
 * properties give it.
 */
public final class ItemActionOperations
{

    public static final ExpressionOperations.Param[] USE_ACTION = params(choice("action", "food", "bow", "shield"));
    public static final ExpressionOperations.Param[] ATTACK_ACTION = params(choice("action", "fists", "sword", "tool"));

    private ItemActionOperations()
    {
    }

    /** {@code {"mobends:use_action": ["bow"]}}: the item in use is used as a bow. */
    public static Expression useAction(ExpressionOperations.Arguments args)
    {
        return new PropertyIs("useActionType", args.string(0).toUpperCase());
    }

    /** {@code {"mobends:attack_action": ["sword"]}}: the held item attacks as a sword. */
    public static Expression attackAction(ExpressionOperations.Arguments args)
    {
        return new PropertyIs("attackActionType", args.string(0).toUpperCase());
    }

    private static final class PropertyIs extends Expression.BooleanExpression
    {
        private final String property;
        private final String value;

        PropertyIs(String property, String value)
        {
            this.property = property;
            this.value = value;
        }

        @Override
        public boolean test(ITriggerConditionContext context)
        {
            return value.equals(context.getSubject().getProperty(property));
        }
    }

}
