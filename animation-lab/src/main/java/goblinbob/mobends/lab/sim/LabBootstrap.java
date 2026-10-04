package goblinbob.mobends.lab.sim;

import goblinbob.mobends.core.kumo.expr.ExpressionOperations;
import goblinbob.mobends.standard.kumo.ItemActionOperations;
import goblinbob.mobends.core.kumo.MinecraftKumoOperations;
import goblinbob.mobends.core.kumo.driver.DriverRegistry;
import goblinbob.mobends.standard.kumo.CapeDriver;
import goblinbob.mobends.standard.kumo.SwordTrailDriver;
import goblinbob.mobends.standard.kumo.spider.SpiderIdleLegsDriver;
import goblinbob.mobends.standard.kumo.spider.SpiderIdleLegsTemplate;
import goblinbob.mobends.standard.kumo.spider.SpiderMovingLegsDriver;
import goblinbob.mobends.standard.kumo.spider.SpiderMovingLegsTemplate;

/**
 * Mirrors the registrations {@code Core.preInit} and {@code DefaultAddon.registerContent} perform
 * in the mod, for the parts of it that the animation code needs.
 */
public class LabBootstrap
{
    private static boolean done = false;

    public static synchronized void ensure()
    {
        if (done) return;
        done = true;
        MinecraftKumoOperations.register();
        ExpressionOperations.register("mobends:use_action", ItemActionOperations.USE_ACTION, false, ItemActionOperations::useAction);
        ExpressionOperations.register("mobends:attack_action", ItemActionOperations.ATTACK_ACTION, false, ItemActionOperations::attackAction);
        DriverRegistry.INSTANCE.register("mobends:sword_trail", SwordTrailDriver::create, SwordTrailDriver.Template.class);
        DriverRegistry.INSTANCE.register("mobends:cape", CapeDriver::create, CapeDriver.Template.class);
        DriverRegistry.INSTANCE.register("mobends:spider_idle_legs", SpiderIdleLegsDriver::create, SpiderIdleLegsTemplate.class);
        DriverRegistry.INSTANCE.register("mobends:spider_moving_legs", SpiderMovingLegsDriver::create, SpiderMovingLegsTemplate.class);
    }
}
