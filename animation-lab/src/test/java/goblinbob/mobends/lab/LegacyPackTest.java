package goblinbob.mobends.lab;

import goblinbob.mobends.lab.compare.BoneStats;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The legacy resource pack (misc/legacy-pack): the player's and the zombie's animators without the
 * layer that stands them on their feet (core:step_turn), as they moved in 1.2.2.
 *
 * <ul>
 *     <li>Each scenario reproduces its own golden, in {@code golden/legacy}, recorded from the pack
 *     with {@code ./gradlew record --args="--legacy $PWD/golden/legacy <entity>[/scenario]"}.</li>
 *     <li>Each scenario poses every bone core:step_turn doesn't drive as the mod's own animator does
 *     (the mod's golden): the pack is a fork, and this is what keeps it one, failing when the mod's
 *     animator changes and the pack isn't changed with it.</li>
 * </ul>
 */
public class LegacyPackTest
{
    /** What core:step_turn poses: the legs, the turn of the whole model, and the hips' height. */
    private static final Set<String> STEP_TURN_BONES = new HashSet<>(Arrays.asList(
            "rightLeg", "rightForeLeg", "leftLeg", "leftForeLeg", "renderRotation", "localOffset",
            // the leaning zombie's crouch, which takes its lowered root back while it stands
            "globalOffset"));

    @TestFactory
    List<DynamicTest> legacyMatchesItsGolden()
    {
        return legacyScenarios().stream()
                .map(scenario -> DynamicTest.dynamicTest(scenario.id(), () -> {
                    PoseTrace golden = TraceIO.read(TraceIO.fileFor(LabPaths.golden().resolve("legacy"), scenario.kind.id(), scenario.name));
                    ComparisonReport report = compare(scenario, golden);
                    assertTrue(report.isWithin(KumoParityTest.MAX_ANGLE_DEG, KumoParityTest.MAX_OFFSET),
                            String.format("The legacy pack deviates from its golden trace for %s (worst %.3f deg / %.4f offset)",
                                    scenario.id(), report.worstAngleDeg(), report.worstOffset()));
                }))
                .collect(Collectors.toList());
    }

    @TestFactory
    List<DynamicTest> legacyMatchesTheModBeyondTheFeet()
    {
        return legacyScenarios().stream()
                .map(scenario -> DynamicTest.dynamicTest(scenario.id(), () -> {
                    PoseTrace golden = TraceIO.read(TraceIO.fileFor(LabPaths.golden(), scenario.kind.id(), scenario.name));
                    ComparisonReport report = compare(scenario, golden);
                    List<String> deviating = new ArrayList<>(report.problems);
                    for (BoneStats stats : report.bones.values())
                    {
                        if (STEP_TURN_BONES.contains(stats.bone)) continue;
                        if (stats.maxAngleDeg > KumoParityTest.MAX_ANGLE_DEG || stats.maxOffset > KumoParityTest.MAX_OFFSET)
                        {
                            deviating.add(String.format("%s (%.3f deg @%d, %.4f @%d)", stats.bone,
                                    stats.maxAngleDeg, stats.maxAngleFrame, stats.maxOffset, stats.maxOffsetFrame));
                        }
                    }
                    for (BoneStats stats : report.vectors.values())
                    {
                        if (!STEP_TURN_BONES.contains(stats.bone) && stats.maxOffset > KumoParityTest.MAX_OFFSET)
                        {
                            deviating.add(String.format("%s (%.4f @%d)", stats.bone, stats.maxOffset, stats.maxOffsetFrame));
                        }
                    }
                    assertTrue(deviating.isEmpty(), "The legacy pack poses these unlike the mod's animator for " + scenario.id()
                            + " (update misc/legacy-pack along with it): " + deviating);
                }))
                .collect(Collectors.toList());
    }

    private static List<Scenario> legacyScenarios()
    {
        return Scenarios.all().stream().filter(s -> Animators.hasLegacy(s.kind)).collect(Collectors.toList());
    }

    private static ComparisonReport compare(Scenario scenario, PoseTrace golden) throws Exception
    {
        PoseTrace actual = new KumoSession(scenario, KumoSession.loadAnimator(Animators.legacyForKind(scenario.kind))).run();
        actual.retainBonesOf(golden);
        ComparisonReport report = PoseComparator.compare(scenario.id(), golden, actual);
        System.out.println(report.toMarkdown());
        return report;
    }
}
