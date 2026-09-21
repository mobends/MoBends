package goblinbob.mobends.lab;

import goblinbob.mobends.lab.compare.ComparisonReport;
import goblinbob.mobends.lab.compare.PoseComparator;
import goblinbob.mobends.lab.scenarios.Animators;
import goblinbob.mobends.lab.scenarios.Scenarios;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.Scenario;
import goblinbob.mobends.lab.trace.PoseTrace;
import goblinbob.mobends.lab.trace.TraceIO;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * The migration check: for every entity that has a KUMO animator, the animator must reproduce the
 * procedural controller's golden trace within tolerance.
 */
public class KumoParityTest
{
    /** Rotation tolerance in degrees. A model part is 16 units per block; 0.1 degree is invisible. */
    public static final double MAX_ANGLE_DEG = 0.1;
    /** Offset tolerance in model units (1/16 block). */
    public static final double MAX_OFFSET = 0.01;

    /**
     * Scenarios whose reference behaviour is not migrated yet. They are reported as skipped, not
     * as failures, until the matching animator work lands; remove them from here as it does.
     */
    public static final Set<String> PENDING = new HashSet<>(Arrays.asList());

    @TestFactory
    List<DynamicTest> animatorMatchesReference()
    {
        return Scenarios.all().stream()
                .filter(s -> Animators.has(s.kind))
                .map(scenario -> DynamicTest.dynamicTest(scenario.id(), () -> check(scenario)))
                .collect(Collectors.toList());
    }

    private void check(Scenario scenario) throws Exception
    {
        assumeFalse(PENDING.contains(scenario.id()), "pending: action layer not migrated yet");
        PoseTrace golden = TraceIO.read(TraceIO.fileFor(LabPaths.golden(), scenario.kind.id(), scenario.name));
        PoseTrace actual = new KumoSession(scenario, KumoSession.loadAnimator(Animators.forKind(scenario.kind))).run();
        ComparisonReport report = PoseComparator.compare(scenario.id(), golden, actual);
        System.out.println(report.toMarkdown());
        assertTrue(report.isWithin(MAX_ANGLE_DEG, MAX_OFFSET),
                String.format("KUMO animator deviates from the reference for %s (worst %.3f deg / %.4f offset)",
                        scenario.id(), report.worstAngleDeg(), report.worstOffset()));
    }
}
