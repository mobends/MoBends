package goblinbob.mobends.core.kumo.option;

import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;

import javax.annotation.Nullable;

/**
 * An option declared by a file (see {@link OptionTemplate}), with the value the user gave it. The
 * value is kept by {@link KumoOptions} under the option's key ({@code <owner>#<name>}), so it
 * outlives a reload, and every declaration of the option shares it. Until the user changes it, the
 * option has its file's default, even when a new version of the file changes the default.
 */
public final class KumoOption
{

    private final String owner;
    private final String name;
    private final OptionTemplate template;
    private final KumoOptions.Cell cell;

    KumoOption(String owner, String name, OptionTemplate template, KumoOptions.Cell cell)
    {
        this.owner = owner;
        this.name = name;
        this.template = template;
        this.cell = cell;
    }

    /** What declares it: a model definition's location, a type's id or an extension's id. */
    public String getOwner()
    {
        return owner;
    }

    public String getName()
    {
        return name;
    }

    /** The key its value is stored by: {@code <owner>#<name>}. */
    public String getKey()
    {
        return KumoOptions.key(owner, name);
    }

    public boolean isBoolean()
    {
        return template.isBoolean;
    }

    public double getMin()
    {
        return template.min;
    }

    public double getMax()
    {
        return template.max;
    }

    /** The step a number moves by; 0 for any value. */
    public double getStep()
    {
        return template.step;
    }

    public double getDefault()
    {
        return template.defaultValue;
    }

    /** The language key of its label, or null for none (the menu shows the name). */
    @Nullable
    public String getLabel()
    {
        return template.label;
    }

    /** The value: the user's, or else the default. A boolean is 0 or 1. */
    public double get()
    {
        double value = cell.value;
        return Double.isNaN(value) ? template.defaultValue : template.normalize(value);
    }

    public boolean isOn()
    {
        return get() != 0;
    }

    /** Whether the user left it at its default. */
    public boolean isDefault()
    {
        return Double.isNaN(cell.value) || template.normalize(cell.value) == template.defaultValue;
    }

    /** Sets the value (normalized: see {@link OptionTemplate#normalize}); the default clears the user's value. */
    public void set(double value)
    {
        double normalized = template.normalize(value);
        cell.set(normalized == template.defaultValue ? Double.NaN : normalized);
    }

    public void reset()
    {
        cell.set(Double.NaN);
    }

    /** The expression that reads it, every frame. */
    public Expression read()
    {
        if (template.isBoolean)
        {
            return new Expression.BooleanExpression()
            {
                @Override
                public boolean test(ITriggerConditionContext context)
                {
                    return isOn();
                }
            };
        }
        return new Expression.NumberExpression()
        {
            @Override
            public float get(ITriggerConditionContext context)
            {
                return (float) KumoOption.this.get();
            }
        };
    }

    @Override
    public String toString()
    {
        return getKey() + "=" + get();
    }

}
