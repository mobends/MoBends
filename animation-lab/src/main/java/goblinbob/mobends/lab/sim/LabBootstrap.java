package goblinbob.mobends.lab.sim;

import goblinbob.mobends.standard.kumo.MoBendsOperations;
import goblinbob.mobends.core.data.EntityComponents;
import goblinbob.mobends.core.kumo.MinecraftKumoOperations;
import goblinbob.mobends.standard.client.renderer.entity.SwordTrail;
import goblinbob.mobends.standard.data.CapeWave;
import goblinbob.mobends.core.kumo.driver.DriverRegistry;
import goblinbob.mobends.standard.kumo.CapeDriver;
import goblinbob.mobends.standard.kumo.SwordTrailDriver;
import goblinbob.mobends.standard.kumo.spider.SpiderIdleLegsDriver;
import goblinbob.mobends.standard.kumo.spider.SpiderIdleLegsTemplate;
import goblinbob.mobends.standard.kumo.spider.SpiderLegs;
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
        MoBendsOperations.register();
        DriverRegistry.INSTANCE.register("mobends:sword_trail", SwordTrailDriver::create, SwordTrailDriver.Template.class);
        DriverRegistry.INSTANCE.register("mobends:cape", CapeDriver::create, CapeDriver.Template.class);
        DriverRegistry.INSTANCE.register("mobends:spider_idle_legs", SpiderIdleLegsDriver::create, SpiderIdleLegsTemplate.class);
        DriverRegistry.INSTANCE.register("mobends:spider_moving_legs", SpiderMovingLegsDriver::create, SpiderMovingLegsTemplate.class);
        EntityComponents.register("mobends:sword_trail", data -> new SwordTrail(() -> null));
        EntityComponents.register("mobends:cape_wave", data -> new CapeWave(data.getEntity()));
        EntityComponents.register("mobends:spider_legs", SpiderLegs::new);
    }
}
