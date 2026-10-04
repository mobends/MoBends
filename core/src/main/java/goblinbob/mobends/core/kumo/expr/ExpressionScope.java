package goblinbob.mobends.core.kumo.expr;

import com.google.gson.JsonElement;
import goblinbob.mobends.core.kumo.state.VariableTable;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The named expressions visible at one level of an animator (the animator, a layer, a machine, a
 * node), and the scope around it. A name resolves to the innermost declaration; a named expression
 * is compiled in the scope that declares it, so the names it uses are the ones visible there, not
 * at the place it is used. A named expression that remembers something ({@code decreased},
 * {@code rose}, {@code fell}) is compiled anew for every use, so each use keeps its own memory.
 * Every scope of an animator shares its {@link VariableTable}, where a name no scope declares is
 * read as a variable.
 */
public class ExpressionScope
{

    @Nullable
    private final ExpressionScope parent;
    private final VariableTable variables;
    /** The lists collecting the stateful expressions compiled for the scope being instanced (a node's items). */
    private final Deque<List<Expression>> holders;
    private final Map<String, ExpressionTemplate> declared;
    private final Map<String, Expression> compiled = new HashMap<>();
    private final Set<String> compiling = new HashSet<>();

    private ExpressionScope(@Nullable ExpressionScope parent, VariableTable variables, Deque<List<Expression>> holders, Map<String, ExpressionTemplate> declared)
    {
        this.parent = parent;
        this.variables = variables;
        this.holders = holders;
        this.declared = declared;
    }

    /** The outermost scope of an animator: no named expressions, every name is a variable of {@code variables}. */
    public static ExpressionScope root(VariableTable variables)
    {
        return new ExpressionScope(null, variables, new ArrayDeque<>(), Collections.emptyMap());
    }

    /** The variables of the animator this scope belongs to. */
    public VariableTable getVariables()
    {
        return variables;
    }

    /**
     * A scope nested in this one, declaring {@code expressions} (as read from JSON; null or empty
     * declare nothing, and this scope is returned). Every declaration is compiled right away, so a
     * mistake is reported when the animator loads even if nothing uses it.
     */
    public ExpressionScope child(@Nullable Map<String, ExpressionTemplate> expressions) throws MalformedKumoTemplateException
    {
        if (expressions == null || expressions.isEmpty())
        {
            return this;
        }
        ExpressionScope scope = new ExpressionScope(this, variables, holders, expressions);
        for (String name : scope.declared.keySet())
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
        ExpressionScope scope = new ExpressionScope(this, variables, holders, Collections.emptyMap());
        scope.compiled.putAll(values);
        return scope;
    }

    /**
     * Collects into {@code holder} every stateful expression compiled until {@link #endHolding}:
     * the scope being instanced starts their memory over when it starts.
     */
    public void beginHolding(List<Expression> holder)
    {
        holders.push(holder);
    }

    public void endHolding()
    {
        holders.pop();
    }

    /** Notes a compiled expression with the scope that holds it (see {@link #beginHolding}). */
    void held(Expression expression)
    {
        if (expression.isStateful() && !holders.isEmpty())
        {
            holders.peek().add(expression);
        }
    }

    /** The named expression {@code name} as seen from this scope, or null if no scope declares it. */
    @Nullable
    public Expression resolve(String name) throws MalformedKumoTemplateException
    {
        for (ExpressionScope scope = this; scope != null; scope = scope.parent)
        {
            if (scope.declared.containsKey(name) || scope.compiled.containsKey(name))
            {
                Expression expression = scope.compileDeclared(name);
                return expression.isStateful() ? scope.compileFresh(name) : expression;
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
        expression = compileFresh(name);
        compiled.put(name, expression);
        return expression;
    }

    private Expression compileFresh(String name) throws MalformedKumoTemplateException
    {
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
                return Expression.compileAny(json, this);
            }
            catch (MalformedKumoTemplateException e)
            {
                throw new MalformedKumoTemplateException("In the named expression '" + name + "': " + e.getMessage());
            }
        }
        finally
        {
            compiling.remove(name);
        }
    }

}
