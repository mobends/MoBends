package goblinbob.mobends.lab;

import goblinbob.mobends.core.kumo.state.KumoProgram;
import goblinbob.mobends.lab.scenarios.Animators;
import goblinbob.mobends.lab.scenarios.Scenarios;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.Scenario;
import goblinbob.mobends.lab.compare.ComparisonReport;
import goblinbob.mobends.lab.compare.PoseComparator;
import goblinbob.mobends.lab.trace.PoseTrace;
import goblinbob.mobends.lab.trace.TraceIO;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An animator is compiled once and shared by every entity it animates: each kind's scenarios run
 * one after another on the program the first one compiled, and every one still matches its golden
 * (state left in the program by an entity would show in the next).
 */
public class ProgramSharingTest
{

    @Test
    void everyScenarioMatchesItsGoldenOnASharedProgram() throws Exception
    {
        Map<EntityKind, KumoProgram> programs = new HashMap<>();
        int shared = 0;
        for (Scenario scenario : Scenarios.all())
        {
            if (!Animators.has(scenario.kind)) continue;
            KumoProgram program = programs.get(scenario.kind);
            KumoSession session;
            if (program == null)
            {
                session = new KumoSession(scenario, KumoSession.loadAnimator(Animators.forKind(scenario.kind)));
                programs.put(scenario.kind, session.getProgram());
            }
            else
            {
                session = new KumoSession(scenario, program);
                shared++;
            }
            PoseTrace golden = TraceIO.read(TraceIO.fileFor(LabPaths.golden(), scenario.kind.id(), scenario.name));
            ComparisonReport report = PoseComparator.compare(scenario.id(), golden, session.run());
            assertTrue(report.isWithin(KumoParityTest.MAX_ANGLE_DEG, KumoParityTest.MAX_OFFSET),
                    String.format("%s deviates on a shared program (worst %.3f deg / %.4f offset)", scenario.id(), report.worstAngleDeg(), report.worstOffset()));
        }
        assertTrue(shared > 10, "only " + shared + " scenarios ran on a shared program");
    }

}
