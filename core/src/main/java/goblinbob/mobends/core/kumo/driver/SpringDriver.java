package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.kumo.api.DriverEvaluator;
import goblinbob.mobends.core.kumo.api.FloatSlot;
import goblinbob.mobends.core.kumo.api.KumoDriver;
import goblinbob.mobends.core.kumo.api.NumberInput;
import goblinbob.mobends.core.kumo.api.StateHandle;
import goblinbob.mobends.core.kumo.state.template.pose.SpringTemplate;

/** See {@link SpringTemplate}: a state pulled towards a target like a mass on a spring. */
public final class SpringDriver
{

    /** The integration step, in ticks: small enough to stay stable for any stiffness an animator would use. */
    private static final float MAX_STEP = 0.25F;

    public static final KumoDriver<SpringTemplate> DRIVER = KumoDriver.of("core:spring", SpringTemplate.class, (template, args) -> {
        if (template.stiffness < 0 || template.friction < 0)
        {
            throw args.error("can't have a negative 'stiffness' or 'friction'.");
        }
        StateHandle state = args.inout("inout", template.inout);
        NumberInput target = args.number("target", template.target);
        float stiffness = template.stiffness;
        float friction = template.friction;
        // The spring's own velocity, at rest when the node is entered.
        FloatSlot velocity = args.slot("velocity", 0);
        return (DriverEvaluator) (context, pose) -> {
            float goal = target.get(context);
            float value = state.get(context);
            float v = velocity.get(context);
            float remaining = context.deltaTime();
            while (remaining > 0)
            {
                float dt = Math.min(remaining, MAX_STEP);
                // Semi-implicit Euler: the velocity first, then the value with the new velocity.
                v += ((goal - value) * stiffness - v * friction) * dt;
                value += v * dt;
                remaining -= dt;
            }
            velocity.set(context, v);
            state.set(context, value);
        };
    });

    private SpringDriver()
    {
    }

}
