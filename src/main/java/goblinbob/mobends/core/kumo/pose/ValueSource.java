package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.kumo.IKumoSubject;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.ValueTemplate;

/**
 * A number that is either constant or derived from a subject variable:
 * {@code clamp(variable * scale + offset, min, max)}.
 */
public class ValueSource
{

    public static final ValueSource ONE = new ValueSource(1F);
    public static final ValueSource ZERO = new ValueSource(0F);

    private final float constant;
    private final String variable;
    private final float scale;
    private final float offset;
    private final float min;
    private final float max;

    public ValueSource(float constant)
    {
        this.constant = constant;
        this.variable = null;
        this.scale = 1;
        this.offset = 0;
        this.min = Float.NEGATIVE_INFINITY;
        this.max = Float.POSITIVE_INFINITY;
    }

    public ValueSource(String variable, float scale, float offset, float min, float max)
    {
        this.constant = 0;
        this.variable = variable;
        this.scale = scale;
        this.offset = offset;
        this.min = min;
        this.max = max;
    }

    public boolean isConstant()
    {
        return variable == null;
    }

    public String getVariable()
    {
        return variable;
    }

    public float get(IKumoSubject subject)
    {
        if (variable == null)
        {
            return constant;
        }
        float value = (float) subject.getVariable(variable) * scale + offset;
        if (value < min) value = min;
        if (value > max) value = max;
        return value;
    }

    public static ValueSource fromTemplate(ValueTemplate template, ValueSource fallback) throws MalformedKumoTemplateException
    {
        if (template == null)
        {
            return fallback;
        }
        if (template.variable == null)
        {
            return new ValueSource(template.constant);
        }
        return new ValueSource(template.variable, template.scale, template.offset, template.min, template.max);
    }

}
