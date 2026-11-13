package goblinbob.mobends.core.definition;

import com.google.gson.JsonElement;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.state.VariableTable;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

/**
 * An {@code @when} known when its file loads: a model definition's variant (see
 * {@link DefinitionMerge#applyVariants}), or a whole type or extension file. Only constants and
 * constant operations, such as {@code {"core:minecraft_version": [">=", "1.10"]}}.
 */
public final class LoadCondition {
    private LoadCondition() {
    }

    /** Whether {@code when} holds; {@code where} starts the message of a load error. */
    public static boolean holds(JsonElement when, String where) throws MalformedKumoTemplateException {
        Expression condition;
        try {
            condition = Expression.compile(when, ExpressionScope.root(new VariableTable()), Expression.Type.BOOLEAN);
        } catch (MalformedKumoTemplateException e) {
            throw new MalformedKumoTemplateException(where + ": " + e.getMessage());
        }
        if (!condition.isConstant()) {
            throw new MalformedKumoTemplateException(where + ": its \"@when\" has to be known when the file loads (only constant operations, such as core:minecraft_version), got "
                    + when + ".");
        }
        return condition.test(null);
    }
}
