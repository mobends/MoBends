package goblinbob.mobends.core.kumo.expr;

import com.google.gson.JsonElement;
import goblinbob.mobends.core.kumo.state.DefinitionScope;
import goblinbob.mobends.core.kumo.state.StateLayout;
import goblinbob.mobends.core.kumo.state.StateRef;
import goblinbob.mobends.core.kumo.state.VariableTable;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * What a place in an animator sees: the scopes around it (the entity, the animator, its layer, the
 * innermost machine around it, its node), whose definitions it reads by scoped name ({@code layer.combo}),
 * and the built-in values (bare names). There is no lookup through enclosing scopes: the prefix
 * says which scope a name lives in, and a name that scope doesn't declare is an error.
 */
public class ExpressionScope
{

    @Nullable
    private final DefinitionScope entity, animator, layer, machine, node;
    /** The class of the entity animated, which operations bind against; null if unknown. */
    @Nullable
    private final Class<?> entityClass;
    /** Whether {@code field} may read the entity here (an entity definition). */
    private final boolean readsFields;
    /** Whether this is a type file's selector, which runs before the entity has any data. */
    private final boolean selector;
    private final VariableTable variables;
    /** Whether the file this place is in is trusted (see {@link DefinitionScope#state}). */
    private final boolean trusted;
    /** Built-in values of this place only (a clip's {@code clipLength}). */
    private final Map<String, Expression> values;
    /** The lists collecting the stateful expressions compiled for the scope being instanced (a node's items). */
    private final Deque<List<Expression>> holders;
    /** Where the animator's per-entity state goes: every stateful element compiled here takes its slots in it. */
    private final StateLayout layout;
    /** Inside a function's body: its arguments, by parameter name (see {@link FunctionCall}); else empty. */
    private Map<String, FunctionCall.Argument> arguments = Collections.emptyMap();
    /** The functions whose bodies are being compiled around this place, the outermost first: a call to one of them would never end. */
    private List<String> calling = Collections.emptyList();

    private ExpressionScope(@Nullable DefinitionScope entity, @Nullable DefinitionScope animator, @Nullable DefinitionScope layer, @Nullable DefinitionScope machine,
                            @Nullable DefinitionScope node, @Nullable Class<?> entityClass, boolean readsFields, boolean selector, VariableTable variables,
                            boolean trusted, Map<String, Expression> values, Deque<List<Expression>> holders, StateLayout layout)
    {
        this.layout = layout;
        this.selector = selector;
        this.entity = entity;
        this.entityClass = entityClass;
        this.readsFields = readsFields;
        this.animator = animator;
        this.layer = layer;
        this.machine = machine;
        this.node = node;
        this.variables = variables;
        this.trusted = trusted;
        this.values = values;
        this.holders = holders;
    }

    /** The outermost place of an animator: no scope yet, every bare name a built-in or a value of the entity. */
    public static ExpressionScope root(VariableTable variables)
    {
        return new ExpressionScope(null, null, null, null, null, null, false, false, variables, true, Collections.emptyMap(), new ArrayDeque<>(), new StateLayout());
    }

    /**
     * A type file's selector: it runs before the entity has any data, so it reads no names, and
     * only the selector-safe operations (see {@link goblinbob.mobends.core.kumo.api.KumoOperation#selectorSafe}).
     */
    public static ExpressionScope selector()
    {
        return new ExpressionScope(null, null, null, null, null, null, false, true, new VariableTable(), true, Collections.emptyMap(), new ArrayDeque<>(), new StateLayout());
    }

    public boolean isSelector()
    {
        return selector;
    }

    /** This place, as a function's body: its arguments, the call {@code function} being added to the calls compiling around it. */
    public ExpressionScope withArguments(Map<String, FunctionCall.Argument> arguments, String function, List<String> callingAround)
    {
        ExpressionScope body = new ExpressionScope(entity, animator, layer, machine, node, entityClass, readsFields, selector, variables, trusted, values, holders, layout);
        body.arguments = arguments;
        List<String> calls = new java.util.ArrayList<>(callingAround);
        calls.add(function);
        body.calling = Collections.unmodifiableList(calls);
        return body;
    }

    /** The functions whose bodies are being compiled around this place, the outermost first. */
    public List<String> getCalling()
    {
        return calling;
    }

    /** The argument of parameter {@code name} of the function whose body this is. */
    FunctionCall.Argument argument(String name) throws MalformedKumoTemplateException
    {
        if (calling.isEmpty())
        {
            throw new MalformedKumoTemplateException(String.format("'arg.%s' is read outside a function: arg. names are a function's parameters, read in its body.", name));
        }
        FunctionCall.Argument argument = arguments.get(name);
        if (argument == null)
        {
            throw new MalformedKumoTemplateException(String.format("'%s' has no parameter '%s'.", calling.get(calling.size() - 1), name));
        }
        return argument;
    }

    /** The function {@code name} (a scoped name, {@code animator.wobble}). */
    FunctionCall.Declared resolveFunction(String name) throws MalformedKumoTemplateException
    {
        DefinitionScope scope = scopeOf(name);
        FunctionCall.Declared function = scope.function(name.substring(name.indexOf('.') + 1));
        if (function == null)
        {
            throw new MalformedKumoTemplateException(String.format("Unknown function '%s': %s declares no '%s' in its \"@functions\".", name, scope.getOwner(), name.substring(name.indexOf('.') + 1)));
        }
        return function;
    }

    /** Where the animator's per-entity state goes (see {@link StateLayout}). */
    public StateLayout getLayout()
    {
        return layout;
    }

    /** The entity's values the animator reads, by name. */
    public VariableTable getVariables()
    {
        return variables;
    }

    /** This place, inside {@code scope}: for a machine, the innermost machine around it. */
    public ExpressionScope inside(DefinitionScope scope)
    {
        switch (scope.kind)
        {
            case ENTITY:
                return new ExpressionScope(scope, animator, layer, machine, node, entityClass, readsFields, selector, variables, trusted, values, holders, layout);
            case ANIMATOR:
                return new ExpressionScope(entity, scope, layer, machine, node, entityClass, readsFields, selector, variables, trusted, values, holders, layout);
            case LAYER:
                return new ExpressionScope(entity, animator, scope, machine, node, entityClass, readsFields, selector, variables, trusted, values, holders, layout);
            case MACHINE:
                return new ExpressionScope(entity, animator, layer, scope, node, entityClass, readsFields, selector, variables, trusted, values, holders, layout);
            default:
                return new ExpressionScope(entity, animator, layer, machine, scope, entityClass, readsFields, selector, variables, trusted, values, holders, layout);
        }
    }

    /** This place, animating an entity of {@code type} (null: unknown), which operations bind against. */
    public ExpressionScope forEntity(@Nullable Class<?> type)
    {
        return new ExpressionScope(entity, animator, layer, machine, node, type, readsFields, selector, variables, trusted, values, holders, layout);
    }

    /** This place, where {@code field} reads an entity of {@code type}: an entity definition. */
    public ExpressionScope readingFieldsOf(Class<?> type)
    {
        return new ExpressionScope(entity, animator, layer, machine, node, type, true, selector, variables, trusted, values, holders, layout);
    }

    /** The class of the entity animated, or null if unknown. */
    @Nullable
    public Class<?> getEntityClass()
    {
        return entityClass;
    }

    /** The entity's class, if {@code field} may read it here (an entity definition), else null. */
    @Nullable
    public Class<?> getFieldsOf()
    {
        return readsFields ? entityClass : null;
    }

    /** This place, in a file that is trusted or not (see {@link DefinitionScope#state}). */
    public ExpressionScope trusted(boolean trusted)
    {
        return new ExpressionScope(entity, animator, layer, machine, node, entityClass, readsFields, selector, variables, trusted, values, holders, layout);
    }

    public boolean isTrusted()
    {
        return trusted;
    }

    /**
     * This place, with built-in values of its own (e.g. a clip's {@code clipLength}); null or
     * empty names nothing and returns this place.
     */
    public ExpressionScope withValues(@Nullable Map<String, Expression> values)
    {
        if (values == null || values.isEmpty())
        {
            return this;
        }
        Map<String, Expression> all = new HashMap<>(this.values);
        all.putAll(values);
        return new ExpressionScope(entity, animator, layer, machine, node, entityClass, readsFields, selector, variables, trusted, all, holders, layout);
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

    /** Compiles a definition's expression here: its scope holds what it remembers, not the place reading it. */
    public Expression compileDefinition(JsonElement json) throws MalformedKumoTemplateException
    {
        return Expression.compileAny(json, this);
    }

    /** A built-in value of this place only, or null. */
    @Nullable
    Expression value(String name)
    {
        return values.get(name);
    }

    /** The read of a scoped name ({@code layer.combo}). */
    Expression resolveScoped(String name) throws MalformedKumoTemplateException
    {
        DefinitionScope scope = scopeOf(name);
        Expression read = scope.read(name.substring(name.indexOf('.') + 1));
        if (read == null)
        {
            throw new MalformedKumoTemplateException(String.format("Unknown name '%s': %s declares no '%s'.", name, scope.getOwner(), name.substring(name.indexOf('.') + 1)));
        }
        return read;
    }

    /** The state {@code name} (a scoped name), for a statement or a driver to write. */
    public StateRef resolveState(String name, String what) throws MalformedKumoTemplateException
    {
        if (name == null || name.indexOf('.') < 0)
        {
            throw new MalformedKumoTemplateException(String.format("%s writes '%s', which is no state: a state is a scoped name, such as 'layer.combo'.", what, name));
        }
        DefinitionScope scope = scopeOf(name);
        StateRef state = scope.state(name.substring(name.indexOf('.') + 1), trusted, what);
        if (state == null)
        {
            throw new MalformedKumoTemplateException(String.format("%s writes '%s', but %s declares no '%s'.", what, name, scope.getOwner(), name.substring(name.indexOf('.') + 1)));
        }
        return state;
    }

    private DefinitionScope scopeOf(String name) throws MalformedKumoTemplateException
    {
        int dot = name.indexOf('.');
        String prefix = name.substring(0, dot);
        if (name.indexOf('.', dot + 1) >= 0)
        {
            throw new MalformedKumoTemplateException(String.format("A name is one scope and one name: '%s' has more.", name));
        }
        DefinitionScope scope;
        switch (prefix)
        {
            case "entity": scope = entity; break;
            case "animator": scope = animator; break;
            case "layer": scope = layer; break;
            case "machine": scope = machine; break;
            case "node": scope = node; break;
            default:
                throw new MalformedKumoTemplateException(String.format("Unknown scope '%s' in '%s': names start with entity., animator., layer., machine. or node.", prefix, name));
        }
        if (scope == null)
        {
            throw new MalformedKumoTemplateException(String.format("'%s' is read outside any %s: %s. names are read inside the %s that declares them.", name, prefix, prefix, prefix));
        }
        return scope;
    }

}
