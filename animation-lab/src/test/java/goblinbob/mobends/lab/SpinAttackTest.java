package goblinbob.mobends.lab;

import goblinbob.mobends.core.kumo.option.KumoOptions;
import goblinbob.mobends.lab.scenarios.Animators;
import goblinbob.mobends.lab.scenarios.Scenarios;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.Scenario;
import goblinbob.mobends.standard.kumo.MoBendsOperations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The player's sword combo ends with a whirl unless its spinAttack option is off. */
public class SpinAttackTest
{

    @AfterEach
    void restoreOptions()
    {
        KumoOptions.INSTANCE.load(Collections.emptyMap());
    }

    /** The slashes of five attacks in a row, in order. */
    private static List<String> slashes() throws Exception
    {
        Scenario scenario = Scenarios.byId("player/sword_combo");
        KumoSession session = new KumoSession(scenario, KumoSession.loadAnimator(Animators.forKind(EntityKind.PLAYER)));
        List<String> slashes = new ArrayList<>();
        for (int frame = 0; frame < scenario.frameCount() && frame * 20F / Scenarios.FPS < 90; frame++)
        {
            session.step();
            for (String node : session.animator.getCurrentNodes())
            {
                if (node.startsWith("slash_") && (slashes.isEmpty() || !slashes.get(slashes.size() - 1).equals(node)))
                {
                    slashes.add(node);
                }
            }
        }
        return slashes;
    }

    @Test
    void theFifthSlashIsTheWhirl() throws Exception
    {
        List<String> slashes = slashes();
        assertEquals(5, slashes.size(), slashes.toString());
        assertEquals("slash_whirl", slashes.get(4));
    }

    @Test
    void withoutTheSpinAttackTheComboStartsOver() throws Exception
    {
        KumoOptions.INSTANCE.load(Collections.singletonMap(MoBendsOperations.SPIN_ATTACK_OPTION, 0.0));
        List<String> slashes = slashes();
        assertEquals(5, slashes.size(), slashes.toString());
        assertEquals(slashes.get(0), slashes.get(4));
    }

}
