package goblinbob.mobends.lab;

import goblinbob.mobends.lab.scenarios.Animators;
import goblinbob.mobends.lab.scenarios.Scenarios;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.Scenario;
import goblinbob.mobends.standard.client.renderer.entity.SwordTrail;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Side effects beyond bone targets (the sword trail) have to happen on the right frames. The
 * expected counts are the ones the original procedural code produced in these scenarios.
 */
public class SideEffectParityTest
{
    @Test
    void swordTrailIsFedOnTheSameFrames() throws Exception
    {
        assertTrail("player/sword_combo", 40, 5);
        assertTrail("player/sword_moves", 86, 5);
        assertTrail("player/offhand_use", 5, 1);
    }

    private static void assertTrail(String id, int samples, int resets) throws Exception
    {
        Scenario scenario = Scenarios.byId(id);
        KumoSession kumo = new KumoSession(scenario, KumoSession.loadAnimator(Animators.forKind(scenario.kind)));
        kumo.run();
        SwordTrail trail = kumo.data.getComponent("swordTrail", SwordTrail.class);
        assertEquals(samples, trail.samples, id + ": trail samples");
        assertEquals(resets, trail.resets, id + ": trail resets");
    }
}
