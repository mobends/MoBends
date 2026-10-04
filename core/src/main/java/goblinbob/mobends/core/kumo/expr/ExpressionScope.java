package goblinbob.mobends.core.kumo.expr;

import com.google.gson.JsonElement;
import goblinbob.mobends.core.kumo.state.VariableTable;
import goblinbob.mobends.core.kumo.state.condition.ITriggerCondition;
import goblinbob.mobends.core.kumo.state.condition.TriggerConditionRegistry;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The named expressions and named conditions visible at one level of an animator (the animator, a
 * layer, a machine, a node), and the scope around it. A name resolves to the innermost declaration;
 * a named expression is compiled, and a named condition instanced, in the scope that declares it, so
 * the names it uses are the ones visible there, not at the place it is used. Every scope of an
 * animator shares its {@link VariableTable}, where a name no scope declares is read as a variable.
 */
public class ExpressionScope
{

    @Nullable
    private final ExpressionScope parent;
    private final VariableTable variables;
    private final Map<String, ExpressionTemplate> declared;
    private final Map<String, Expression> compiled = new HashMap<>();
    private final Set<String> compiling = new HashSet<>();
    private final Map<String, TriggerConditionTemplate> declaredConditions;
    private final Set<String> instancing = new HashSet<>();

    private ExpressionScope(@Nullable ExpressionScope parent, VariableTable variables, Map<String, ExpressionTemplate> declared, Map<String, TriggerConditionTemplate> declaredConditions)
    {
        this.parent = parent;
        this.variables = variables;
        this.declared = declared;
        this.declaredConditions = declaredConditions;
    }

    /** The outermost scope of an animator: no named expressions, every name is a variable of {@code variables}. */
    public static ExpressionScope root(VariableTable variables)
    {
        return new ExpressionScope(null, variables, Collections.emptyMap(), Collections.emptyMap());
    }

    /** The variables of the animator this scope belongs to. */
    public VariableTable getVariables()
    {
        return variables;
    }

    /** {@link #child(Map, Map)} without named conditions. */
    public ExpressionScope child(@Nullable Map<String, ExpressionTemplate> expressions) throws MalformedKumoTemplateException
    {
        return child(expressions, null);
    }

    /**
     * A scope nested in this one, declaring {@code expressions} and {@code conditions} (maps as read
     * from JSON; null or empty declare nothing, and with neither this scope is returned). Every
     * declaration is compiled (or instanced once) right away, so a mistake is reported when the
     * animator loads even if nothing uses it.
     */
    public ExpressionScope child(@Nullable Map<String, ExpressionTemplate> expressions, @Nullable Map<String, TriggerConditionTemplate> conditions) throws MalformedKumoTemplateException
    {
        boolean noExpressions = expressions == null || expressions.isEmpty();
        boolean noConditions = conditions == null || conditions.isEmpty();
        if (noExpressions && noConditions)
        {
            return this;
        }
        ExpressionScope scope = new ExpressionScope(this, variables, noExpressions ? Collections.<String, ExpressionTemplate>emptyMap() : expressions,
                                                    noConditions ? Collections.<String, TriggerConditionTemplate>emptyMap() : conditions);
        for (String name : scope.declared.keySet())
        {
            scope.compileDeclared(name);
        }
        for (String name : scope.declaredConditions.keySet())
        {
            scope.instanceDeclared(name);
        }
        return scope;
    }

    /**
     * A scope nested in this one, naming values that are already compiled (e.g. a clip's
     * {@code clipLength}); null or empty names nothing and returns this scope.
     */
    public ExpressionScope withValues(@Nullable Map<String, Expression> values)
    {
        if (values == null || values.isEmpty())
        {
            return this;
        }
        ExpressionScope scope = new ExpressionScope(this, variables, Collections.emptyMap(), Collections.emptyMap());
        scope.compiled.putAll(values);
        return scope;
    }

    /** The named expression {@code name} as seen from this scope, or null if no scope declares it. */
    @Nullable
    public Expression resolve(String name) throws MalformedKumoTemplateException
    {
        for (ExpressionScope scope = this; scope != null; scope = scope.parent)
        {
            if (scope.declared.containsKey(name) || scope.compiled.containsKey(name))
            {
                return scope.compileDeclared(name);
            }
        }
        return null;
    }

    /**
     * A new instance of the named condition {@code name} as seen from this scope, or null if no
     * scope declares it. Every use gets its own instance, so conditions with a memory
     * ({@code core:decreased}) keep one per use.
     */
    @Nullable
    public ITriggerCondition createCondition(String name) throws MalformedKumoTemplateException
    {
        for (ExpressionScope scope = this; scope != null; scope = scope.parent)
        {
            if (scope.declaredConditions.containsKey(name))
            {
                return scope.instanceDeclared(name);
            }
        }
        return null;
    }

    private ITriggerCondition instanceDeclared(String name) throws MalformedKumoTemplateException
    {
        if (!instancing.add(name))
        {
            throw new MalformedKumoTemplateException("The named condition '" + name + "' depends on itself.");
        }
        try
        {
            TriggerConditionTemplate template = declaredConditions.get(name);
            if (template == null)
            {
                throw new MalformedKumoTemplateException("The named condition '" + name + "' is empty.");
            }
            try
            {
                return TriggerConditionRegistry.INSTANCE.createFromTemplate(template, this);
            }
            catch (MalformedKumoTemplateException e)
            {
                throw new MalformedKumoTemplateException("In the named condition '" + name + "': " + e.getMessage());
            }
        }
        finally
        {
            instancing.remove(name);
        }
    }

    private Expression compileDeclared(String name) throws MalformedKumoTemplateException
    {
        Expression expression = compiled.get(name);
        if (expression != null)
        {
            return expression;
        }
        if (!compiling.add(name))
        {
            throw new MalformedKumoTemplateException("The named expression '" + name + "' depends on itself.");
        }
        try
        {
            ExpressionTemplate template = declared.get(name);
            JsonElement json = template == null ? null : template.json;
            if (json == null)
            {
                throw new MalformedKumoTemplateException("The named expression '" + name + "' is empty.");
            }
            try
            {
                expression = Expression.compile(json, this);
            }
            catch (MalformedKumoTemplateException e)
            {
                throw new MalformedKumoTemplateException("In the named expression '" + name + "': " + e.getMessage());
            }
            compiled.put(name, expression);
            return expression;
        }
        finally
        {
            compiling.remove(name);
        }
    }

}
