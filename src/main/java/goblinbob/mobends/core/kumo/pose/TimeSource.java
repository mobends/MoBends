package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.kumo.IKumoSubject;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.TimeTemplate;

/**
 * Where a clip takes its playback time from: the node's own elapsed ticks (scaled), or a
 * subject variable (e.g. "limbSwing" for walk cycles, "ticks" for globally synchronised idles,
 * "ticksInAir" for one-shots that should follow an entity counter exactly).
 */
public class TimeSource
{

    private final String variable;
    private final float scale;
    private final float offset;

    public TimeSource(String variable, float scale, float offset)
    {
        this.variable = variable;
        this.scale = scale;
        this.offset = offset;
    }

    public static TimeSource elapsed(float speed)
    {
        return new TimeSource(null, speed, 0);
    }

    public boolean isElapsed()
    {
        return variable == null;
    }

    public float get(IKumoSubject subject, float elapsedTicks)
    {
        float base = variable == null ? elapsedTicks : (float) subject.getVariable(variable);
        return base * scale + offset;
    }

    public static TimeSource fromTemplate(TimeTemplate template) throws MalformedKumoTemplateException
    {
        if (template == null)
        {
            return elapsed(1F);
        }
        return new TimeSource(template.variable, template.scale, template.offset);
    }

}
