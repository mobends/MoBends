package goblinbob.mobends.core.kumo.expr;

import com.google.gson.JsonElement;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The named expressions visible at one level of an animator (the animator, a layer, a node), and the
 * scope around it. A name resolves to the innermost declaration; a named expression is compiled in
 * the scope that declares it, so the names it uses are the ones visible there, not at the place it
 * is used.
 */
public class ExpressionScope
{

    /** No named expressions: every name is a variable. */
    public static final ExpressionScope ROOT = new ExpressionScope(null, Collections.emptyMap());

    @Nullable
    private final ExpressionScope parent;
    private final Map<String, ExpressionTemplate> declared;
    private final Map<String, Expression> compiled = new HashMap<>();
    private final Set<String> compiling = new HashSet<>();

    private ExpressionScope(@Nullable ExpressionScope parent, Map<String, ExpressionTemplate> declared)
    {
        this.parent = parent;
        this.declared = declared;
    }

    /**
     * A scope nested in this one, declaring {@code expressions} (a map as read from JSON; null or
     * empty declares nothing and returns this scope). Every declaration is compiled right away, so a
     * mistake is reported when the animator loads even if nothing uses it.
     */
    public ExpressionScope child(@Nullable Map<String, ExpressionTemplate> expressions) throws MalformedKumoTemplateException
    {
        if (expressions == null || expressions.isEmpty())
        {
            return this;
        }
        ExpressionScope scope = new ExpressionScope(this, expressions);
        for (String name : expressions.keySet())
        {
            scope.compileDeclared(name);
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
        ExpressionScope scope = new ExpressionScope(this, Collections.emptyMap());
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
