package goblinbob.mobends.core.kumo.api;

import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionOperations;
import goblinbob.mobends.core.kumo.expr.ExpressionOperations.Kind;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A registered operation: its signature, checked when an animator loads and used for its error
 * messages, and how it binds to the entity's class (see {@link Binder}). Built with
 * {@link #named}:
 * <pre>{@code
 * KumoOperation.named("mymod:distance_to_nearest")
 *         .param("entityType", Kind.STRING)
 *         .returns(Expression.Type.NUMBER)
 *         .withFallback()
 *         .bind(args -> ...);
 * }</pre>
 */
public final class KumoOperation
{

    /** One parameter: its name (for messages), its kind and, for a choice, its choices. */
    public static final class Param
    {
        public final String name;
        public final Kind kind;
        final String[] choices;

        Param(String name, Kind kind, String[] choices)
        {
            this.name = name;
            this.kind = kind;
            this.choices = choices;
        }
    }

    public final String name;
    public final List<Param> params;
    /** Whether the last parameter repeats: the operation takes as many arguments as it has parameters, or more. */
    public final boolean repeatsLast;
    public final Expression.Type returns;
    /** Whether it may be written with a {@code @fallback}, used where it doesn't apply to the entity's class. */
    public final boolean takesFallback;
    /** Whether it is a function of its arguments alone (no entity, no state): with constant arguments, it is computed once, at load. */
    public final boolean pure;
    /** Whether a type file's selector may use it: it reads the entity alone, no entity data. */
    public final boolean selectorSafe;
    /**
     * Whether its value for one entity stays the same for the entity's life (a player's name does,
     * a skin variant doesn't: it reads {@code default} until the skin downloads). A selector
     * that holds an unstable operation is asked again every frame.
     */
    public final boolean stable;
    public final Binder binder;

    private KumoOperation(Builder builder, Binder binder)
    {
        this.name = builder.name;
        this.params = Collections.unmodifiableList(new ArrayList<>(builder.params));
        this.repeatsLast = builder.repeatsLast;
        this.returns = builder.returns;
        this.takesFallback = builder.takesFallback;
        this.pure = builder.pure;
        this.selectorSafe = builder.selectorSafe;
        this.stable = builder.stable;
        this.binder = binder;
    }

    public static Builder named(String name)
    {
        return new Builder(name);
    }

    /** This operation, renamed (an addon's registry adds its mod id). */
    public KumoOperation renamed(String name)
    {
        Builder builder = new Builder(name);
        builder.params.addAll(params);
        builder.repeatsLast = repeatsLast;
        builder.returns = returns;
        builder.takesFallback = takesFallback;
        builder.pure = pure;
        builder.selectorSafe = selectorSafe;
        builder.stable = stable;
        return new KumoOperation(builder, binder);
    }

    /** The parameters as the expression compiler takes them. */
    public ExpressionOperations.Param[] compilerParams()
    {
        ExpressionOperations.Param[] result = new ExpressionOperations.Param[params.size()];
        for (int i = 0; i < result.length; i++)
        {
            Param param = params.get(i);
            result[i] = ExpressionOperations.param(param.name, param.kind, param.choices);
        }
        return result;
    }

    public static final class Builder
    {
        private final String name;
        private final List<Param> params = new ArrayList<>();
        private boolean repeatsLast;
        private Expression.Type returns;
        private boolean takesFallback;
        private boolean pure;
        private boolean selectorSafe;
        private boolean stable = true;

        private Builder(String name)
        {
            this.name = name;
        }

        /**
         * A parameter: {@link Kind#NUMBER} or {@link Kind#BOOLEAN} (any expression of that type,
         * evaluated every frame), {@link Kind#CONSTANT} (a number written out), or
         * {@link Kind#STRING} (a string written out); see {@link #choice} for a choice.
         */
        public Builder param(String name, Kind kind)
        {
            if (kind == Kind.ANY || kind == Kind.CHOICE)
            {
                throw new IllegalArgumentException("A registered operation's parameter has a fixed type, and a choice its choices (choice()): '" + name + "' is " + kind + ".");
            }
            params.add(new Param(name, kind, new String[0]));
            return this;
        }

        /** A parameter that is one of {@code choices}, written out. */
        public Builder choice(String name, String... choices)
        {
            params.add(new Param(name, Kind.CHOICE, Arrays.copyOf(choices, choices.length)));
            return this;
        }

        /** The last parameter repeats. */
        public Builder repeatsLast()
        {
            this.repeatsLast = true;
            return this;
        }

        public Builder returns(Expression.Type type)
        {
            this.returns = type;
            return this;
        }

        /** It may be written with a {@code @fallback}, used where it doesn't apply to the entity's class. */
        public Builder withFallback()
        {
            this.takesFallback = true;
            return this;
        }

        /** Same arguments, same result: no entity, no state. */
        public Builder pure()
        {
            this.pure = true;
            return this;
        }

        /**
         * A type file's selector may use it: it reads the entity alone, no entity data (a selector
         * runs before there is any). {@code stable}: its value for one entity stays the same for
         * the entity's life, so a selector holding it needn't be asked again.
         */
        public Builder selectorSafe(boolean stable)
        {
            this.selectorSafe = true;
            this.stable = stable;
            return this;
        }

        public KumoOperation bind(Binder binder)
        {
            if (returns == null)
            {
                throw new IllegalArgumentException("Operation '" + name + "' needs returns(...).");
            }
            if (repeatsLast && params.isEmpty())
            {
                throw new IllegalArgumentException("Operation '" + name + "' repeats its last parameter but has none.");
            }
            return new KumoOperation(this, binder);
        }
    }

}
