package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.expr.FunctionCall;
import goblinbob.mobends.core.kumo.state.template.FunctionTemplate;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;
import goblinbob.mobends.core.kumo.state.template.DefinitionTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The definitions of one scope of an animator (the animator, a layer, a machine or a node), read
 * by their scoped names ({@code layer.combo}), and their values for the entity it animates.
 * <ul>
 *     <li>A <b>constant</b> is computed once, when the scope is created; a <b>state</b> starts
 *     from its initial value then, and only {@code set} statements and drivers change it.</li>
 *     <li>A <b>live</b> definition is computed once per frame, the first time it is read (or, if
 *     it remembers something, every frame the scope exists: see {@link #updateLive}).</li>
 * </ul>
 * Definitions may read each other in any order; one that depends on itself is an error. Each is
 * compiled the first time something reads it, and every one is checked when the animator loads.
 */
public final class DefinitionScope
{

    public enum Kind
    {
        ENTITY("entity"),
        ANIMATOR("animator"),
        LAYER("layer"),
        MACHINE("machine"),
        NODE("node");

        /** What its names start with: {@code layer.combo}. */
        public final String prefix;

        Kind(String prefix)
        {
            this.prefix = prefix;
        }
    }

    private static final int UNSET = 0, COMPUTING = 1, SET = 2;

    public final Kind kind;
    /** What declares it, for messages: "the layer", "the node 'walk'". */
    private final String owner;
    private final Map<String, Integer> indices = new LinkedHashMap<>();
    private final List<String> names = new ArrayList<>();
    private final List<DefinitionTemplate> templates = new ArrayList<>();
    private final List<Boolean> trusted = new ArrayList<>();
    /** Where the definitions are compiled: the names visible to them. */
    private ExpressionScope compileScope;
    /** The scope's functions, by name (see {@link FunctionCall}). */
    private final Map<String, FunctionTemplate> functions = new LinkedHashMap<>();

    // Compiled, by index.
    private Expression[] compiled = new Expression[0];
    private Expression.Type[] types = new Expression.Type[0];
    private final List<String> compiling = new ArrayList<>();

    // Where the entity's values are, in its state (see StateLayout): the values (floats), then, by
    // index too, whether each is set (ints) and the frame a live one was computed in (ints).
    private int valueSlot, statusSlot, frameSlot;

    public DefinitionScope(Kind kind, String owner)
    {
        this.kind = kind;
        this.owner = owner;
    }

    public String getOwner()
    {
        return owner;
    }

    /**
     * Declares {@code definitions} (as read from a file; null declares nothing).
     *
     * @param trusted whether the file declaring them is trusted (see {@link #state})
     */
    public void declare(@Nullable Map<String, DefinitionTemplate> definitions, boolean trusted) throws MalformedKumoTemplateException
    {
        if (definitions == null)
        {
            return;
        }
        for (Map.Entry<String, DefinitionTemplate> entry : definitions.entrySet())
        {
            String name = entry.getKey();
            if (name.indexOf('.') >= 0)
            {
                throw new MalformedKumoTemplateException(String.format("A definition's name can't contain a dot: '%s' (in %s).", name, owner));
            }
            if (indices.containsKey(name))
            {
                throw new MalformedKumoTemplateException(String.format("'%s.%s' is declared twice (in %s): a name can't be declared again, even by an animator that extends another.", kind.prefix, name, owner));
            }
            indices.put(name, names.size());
            names.add(name);
            templates.add(entry.getValue());
            this.trusted.add(trusted);
        }
        int count = names.size();
        compiled = java.util.Arrays.copyOf(compiled, count);
        types = java.util.Arrays.copyOf(types, count);
    }

    /** Declares {@code declared} (a file's {@code @functions}; null declares nothing). */
    public void declareFunctions(@Nullable Map<String, FunctionTemplate> declared) throws MalformedKumoTemplateException
    {
        if (declared == null)
        {
            return;
        }
        for (Map.Entry<String, FunctionTemplate> entry : declared.entrySet())
        {
            String name = entry.getKey();
            if (name.indexOf('.') >= 0)
            {
                throw new MalformedKumoTemplateException(String.format("A function's name can't contain a dot: '%s' (in %s).", name, owner));
            }
            if (functions.containsKey(name) || indices.containsKey(name))
            {
                throw new MalformedKumoTemplateException(String.format("'%s.%s' is declared twice (in %s): a function and a definition can't share a name, nor can two functions, even across 'extends'.",
                        kind.prefix, name, owner));
            }
            functions.put(name, entry.getValue());
        }
    }

    /** The function {@code name}, compiled where this scope's definitions are; null if the scope declares none. */
    @Nullable
    public FunctionCall.Declared function(String name)
    {
        FunctionTemplate template = functions.get(name);
        return template == null ? null : new FunctionCall.Declared(kind.prefix + "." + name, template, compileScope);
    }

    /**
     * Where the definitions are compiled, and then every one is: a mistake is reported when the
     * animator loads, used or not. So is every function's body, once, with stand-ins for its
     * arguments.
     */
    public void compileIn(ExpressionScope scope) throws MalformedKumoTemplateException
    {
        this.compileScope = scope;
        for (Map.Entry<String, FunctionTemplate> entry : functions.entrySet())
        {
            if (indices.containsKey(entry.getKey()))
            {
                throw new MalformedKumoTemplateException(String.format("'%s.%s' is both a definition and a function (in %s).", kind.prefix, entry.getKey(), owner));
            }
            FunctionCall.check(function(entry.getKey()));
        }
        valueSlot = scope.getLayout().floats(names.size(), 0);
        statusSlot = scope.getLayout().ints(names.size(), UNSET);
        frameSlot = scope.getLayout().ints(names.size(), -1);
        for (int i = 0; i < names.size(); i++)
        {
            compile(i);
        }
    }

    public boolean isEmpty()
    {
        return names.isEmpty();
    }

    /** The read of {@code name}, or null if the scope doesn't declare it. */
    @Nullable
    public Expression read(String name) throws MalformedKumoTemplateException
    {
        Integer index = indices.get(name);
        if (index == null)
        {
            return null;
        }
        compile(index);
        if (templates.get(index).kind == DefinitionTemplate.Kind.CONSTANT && compiled[index].isConstant())
        {
            // Reads nothing per entity: its value is the program's, not every entity's.
            return compiled[index];
        }
        return types[index] == Expression.Type.BOOLEAN ? new BooleanRead(this, index) : new NumberRead(this, index);
    }

    /**
     * The state {@code name}, for a statement or a driver to write.
     *
     * @param trusted whether the file writing it is trusted: a file from a resource pack may only
     *                write the state a file from a resource pack declares, so it can't steer the
     *                trusted layers (whose output the resource-pack limits are measured from)
     * @return null if the scope doesn't declare the name
     */
    @Nullable
    public StateRef state(String name, boolean trusted, String what) throws MalformedKumoTemplateException
    {
        Integer index = indices.get(name);
        if (index == null)
        {
            return null;
        }
        compile(index);
        String full = kind.prefix + "." + name;
        if (templates.get(index).kind != DefinitionTemplate.Kind.STATE)
        {
            throw new MalformedKumoTemplateException(String.format("%s can't change '%s': it is %s, not a state.", what, full,
                    templates.get(index).kind == DefinitionTemplate.Kind.LIVE ? "a live definition" : "a constant"));
        }
        if (!trusted && this.trusted.get(index))
        {
            throw new MalformedKumoTemplateException(String.format("%s, from a resource pack, can't change '%s', which a trusted file declares.", what, full));
        }
        return new StateRef(this, index, types[index], full);
    }

    private void compile(int index) throws MalformedKumoTemplateException
    {
        if (compiled[index] != null)
        {
            return;
        }
        String name = names.get(index);
        if (compiling.contains(name))
        {
            throw new MalformedKumoTemplateException(String.format("'%s.%s' depends on itself (in %s).", kind.prefix, name, owner));
        }
        if (compileScope == null)
        {
            throw new MalformedKumoTemplateException(String.format("'%s.%s' is read before %s is set up.", kind.prefix, name, owner));
        }
        compiling.add(name);
        try
        {
            DefinitionTemplate template = templates.get(index);
            Expression expression = compileScope.compileDefinition(template.expression);
            compiled[index] = expression;
            types[index] = expression.getType();
        }
        catch (MalformedKumoTemplateException e)
        {
            throw new MalformedKumoTemplateException(String.format("In '%s.%s' (in %s): %s", kind.prefix, name, owner, e.getMessage()));
        }
        finally
        {
            compiling.remove(name);
        }
    }

    // --- the entity's values -----------------------------------------------------------------------

    /**
     * Creates the scope for the entity: what its definitions remember starts over, and its
     * constants and states take their values (in any order: each reads the others it needs).
     */
    public void start(ITriggerConditionContext context)
    {
        int[] ints = context.getState().ints;
        for (int i = 0; i < compiled.length; i++)
        {
            ints[statusSlot + i] = UNSET;
            ints[frameSlot + i] = -1;
            if (compiled[i].isStateful())
            {
                compiled[i].restart(context);
            }
        }
        for (int i = 0; i < compiled.length; i++)
        {
            if (templates.get(i).kind != DefinitionTemplate.Kind.LIVE)
            {
                value(i, context);
            }
        }
    }

    /** Computes this frame's value of every live definition that remembers something: it steps every frame. */
    public void updateLive(ITriggerConditionContext context)
    {
        for (int i = 0; i < compiled.length; i++)
        {
            if (templates.get(i).kind == DefinitionTemplate.Kind.LIVE && compiled[i].isStateful())
            {
                value(i, context);
            }
        }
    }

    double value(int index, ITriggerConditionContext context)
    {
        EntityState state = context.getState();
        if (templates.get(index).kind == DefinitionTemplate.Kind.LIVE)
        {
            int frame = (int) context.getFrame();
            if (state.ints[frameSlot + index] != frame)
            {
                state.ints[frameSlot + index] = frame;
                state.floats[valueSlot + index] = evaluate(index, context);
            }
            return state.floats[valueSlot + index];
        }
        if (state.ints[statusSlot + index] == UNSET)
        {
            state.ints[statusSlot + index] = COMPUTING;
            state.floats[valueSlot + index] = evaluate(index, context);
            state.ints[statusSlot + index] = SET;
        }
        return state.floats[valueSlot + index];
    }

    void set(int index, double value, ITriggerConditionContext context)
    {
        EntityState state = context.getState();
        state.floats[valueSlot + index] = (float) value;
        state.ints[statusSlot + index] = SET;
    }

    private float evaluate(int index, ITriggerConditionContext context)
    {
        Expression expression = compiled[index];
        return expression.getType() == Expression.Type.BOOLEAN ? (expression.test(context) ? 1 : 0) : expression.get(context);
    }

    private static final class NumberRead extends Expression.NumberExpression
    {
        private final DefinitionScope scope;
        private final int index;

        NumberRead(DefinitionScope scope, int index)
        {
            this.scope = scope;
            this.index = index;
        }

        @Override
        public float get(ITriggerConditionContext context)
        {
            return (float) scope.value(index, context);
        }
    }

    private static final class BooleanRead extends Expression.BooleanExpression
    {
        private final DefinitionScope scope;
        private final int index;

        BooleanRead(DefinitionScope scope, int index)
        {
            this.scope = scope;
            this.index = index;
        }

        @Override
        public boolean test(ITriggerConditionContext context)
        {
            return scope.value(index, context) != 0;
        }
    }

}
