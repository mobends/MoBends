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
        goblinbob.mobends.core.kumo.driver.DriverRegistry.INSTANCE.register("mobends:sword_trail", goblinbob.mobends.standard.kumo.SwordTrailDriver::create, goblinbob.mobends.standard.kumo.SwordTrailDriver.Template.class);
        goblinbob.mobends.core.kumo.driver.DriverRegistry.INSTANCE.register("mobends:cape", goblinbob.mobends.standard.kumo.CapeDriver::create, goblinbob.mobends.standard.kumo.CapeDriver.Template.class);
        goblinbob.mobends.core.kumo.driver.DriverRegistry.INSTANCE.register("mobends:spider_idle_legs", goblinbob.mobends.standard.kumo.spider.SpiderIdleLegsDriver::create, goblinbob.mobends.standard.kumo.spider.SpiderIdleLegsTemplate.class);
        goblinbob.mobends.core.kumo.driver.DriverRegistry.INSTANCE.register("mobends:spider_moving_legs", goblinbob.mobends.standard.kumo.spider.SpiderMovingLegsDriver::create, goblinbob.mobends.standard.kumo.spider.SpiderMovingLegsTemplate.class);
    }
}
