package goblinbob.mobends.lab.sim;

import goblinbob.mobends.core.kumo.state.condition.TriggerConditionRegistry;
import goblinbob.mobends.standard.kumo.WolfStateCondition;

/**
 * Mirrors the registrations {@code DefaultAddon.registerContent} performs in the mod, for the
 * parts of it that the animation code needs.
 */
public class LabBootstrap
{
    private static boolean done = false;

    public static synchronized void ensure()
    {
        if (done) return;
        done = true;
        TriggerConditionRegistry.instance.register("mobends:wolf_state", WolfStateCondition::new, WolfStateCondition.Template.class);
    }
}
