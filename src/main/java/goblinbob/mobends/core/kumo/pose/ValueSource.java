package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.kumo.IKumoSubject;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.ValueTemplate;

/**
 * A number that is either constant or derived from a variable:
 * {@code ease(clamp(variable, min, max)) * scale + offset} (easing applies to the clamped
 * variable before scaling, so a 0..1 ramp can be shaped and then scaled to degrees).
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
    private final Easing ease;
    private final float power;
    private final boolean clampAfter;
    private Fn fn = Fn.NONE;
    private float mul = 1;
    private float add = 0;

    public enum Easing { NONE, POW, EASE_IN, EASE_OUT, EASE_IN_OUT }

    /** A function applied after scaling and clamping; MC_* use Minecraft's sine table. */
    public enum Fn { NONE, SIN, COS, MC_SIN, MC_COS, ABS }

    /** Post-processing: {@code fn(value) * mul + add}. */
    public ValueSource post(Fn fn, float mul, float add)
    {
        this.fn = fn == null ? Fn.NONE : fn;
        this.mul = mul;
        this.add = add;
        return this;
    }

    private float post(float value)
    {
        switch (fn)
        {
            case SIN: value = (float) Math.sin(value); break;
            case COS: value = (float) Math.cos(value); break;
            case MC_SIN: value = net.minecraft.util.math.MathHelper.sin(value); break;
            case MC_COS: value = net.minecraft.util.math.MathHelper.cos(value); break;
            case ABS: value = Math.abs(value); break;
            default: break;
        }
        return value * mul + add;
    }

    public ValueSource(float constant)
    {
        this.constant = constant;
        this.variable = null;
        this.scale = 1;
        this.offset = 0;
        this.min = Float.NEGATIVE_INFINITY;
        this.max = Float.POSITIVE_INFINITY;
        this.ease = Easing.NONE;
        this.power = 1;
        this.clampAfter = false;
    }

    public ValueSource(String variable, float scale, float offset, float min, float max)
    {
        this(variable, scale, offset, min, max, Easing.NONE, 1, true);
    }

    public ValueSource(String variable, float scale, float offset, float min, float max, Easing ease, float power, boolean clampAfter)
    {
        this.constant = 0;
        this.variable = variable;
        this.scale = scale;
        this.offset = offset;
        this.min = min;
        this.max = max;
        this.ease = ease;
        this.power = power;
        this.clampAfter = clampAfter;
    }

    public boolean isConstant()
    {
        return variable == null;
    }

    public String getVariable()
    {
        return variable;
    }

    /** Resolves through the context's scopes (node-local, layer, subject). */
    public float get(ITriggerConditionContext context)
    {
        if (variable == null)
        {
            return constant;
        }
        return shape((float) context.resolveVariable(variable));
    }

    /** Subject-only resolution (no node or layer scopes). */
    public float get(IKumoSubject subject)
    {
        if (variable == null)
        {
            return constant;
        }
        return shape((float) subject.getVariable(variable));
    }

    private float shape(float value)
    {
        if (clampAfter)
        {
            // Legacy order: scale/offset first, then clamp (used by drivers like the body twist).
            value = value * scale + offset;
            if (value < min) value = min;
            if (value > max) value = max;
            return post(value);
        }
        if (value < min) value = min;
        if (value > max) value = max;
        switch (ease)
        {
            case POW: value = (float) Math.pow(value, power); break;
            case EASE_IN: value = (float) goblinbob.mobends.core.util.Tween.easeIn(value, power); break;
            case EASE_OUT: value = (float) goblinbob.mobends.core.util.Tween.easeOut(value, power); break;
            case EASE_IN_OUT: value = (float) goblinbob.mobends.core.util.Tween.easeInOut(value, power); break;
            default: break;
        }
        return post(value * scale + offset);
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
        Easing ease = Easing.NONE;
        if (template.ease != null)
        {
            try
            {
                ease = Easing.valueOf(template.ease.toUpperCase());
            }
            catch (IllegalArgumentException e)
            {
                throw new MalformedKumoTemplateException("Unknown easing: " + template.ease);
            }
        }
        // With an easing the clamp has to happen first (the ramp is shaped in 0..1, then scaled).
        boolean clampAfter = ease == Easing.NONE && !template.clampFirst;
        Fn fn = Fn.NONE;
        if (template.fn != null)
        {
            try
            {
                fn = Fn.valueOf(template.fn.toUpperCase().replace("MCSIN", "MC_SIN").replace("MCCOS", "MC_COS"));
            }
            catch (IllegalArgumentException e)
            {
                throw new MalformedKumoTemplateException("Unknown value function: " + template.fn);
            }
        }
        return new ValueSource(template.variable, template.scale, template.offset, template.min, template.max, ease, template.power, clampAfter)
                .post(fn, template.mul, template.add);
    }

}
