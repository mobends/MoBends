package goblinbob.mobends.lab;

import goblinbob.mobends.lab.scenarios.Animators;
import goblinbob.mobends.lab.scenarios.Scenarios;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.ReferenceSession;
import goblinbob.mobends.lab.sim.Scenario;
import goblinbob.mobends.standard.data.BipedEntityData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The procedural bits had side effects beyond bone targets (the sword trail); the animator's
 * drivers must trigger them on the same frames.
 */
public class SideEffectParityTest
{
    @Test
    void swordTrailIsFedOnTheSameFrames() throws Exception
    {
        for (String id : new String[] { "player/sword_combo", "player/sword_moves", "player/offhand_use" })
        {
            Scenario scenario = Scenarios.byId(id);
            ReferenceSession reference = new ReferenceSession(scenario);
            reference.run();
            KumoSession kumo = new KumoSession(scenario, KumoSession.loadAnimator(Animators.forKind(scenario.kind)));
            kumo.run();
            BipedEntityData<?> ref = (BipedEntityData<?>) reference.data;
            BipedEntityData<?> actual = (BipedEntityData<?>) kumo.data;
            assertTrue(ref.swordTrail.samples > 0, id + ": the reference never fed the trail");
            assertEquals(ref.swordTrail.samples, actual.swordTrail.samples, id + ": trail samples");
            assertEquals(ref.swordTrail.resets, actual.swordTrail.resets, id + ": trail resets");
        }
    }
}
