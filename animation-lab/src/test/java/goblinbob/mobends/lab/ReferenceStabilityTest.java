package goblinbob.mobends.lab;

import goblinbob.mobends.lab.compare.ComparisonReport;
import goblinbob.mobends.lab.compare.PoseComparator;
import goblinbob.mobends.lab.scenarios.Scenarios;
import goblinbob.mobends.lab.sim.ReferenceSession;
import goblinbob.mobends.lab.sim.Scenario;
import goblinbob.mobends.lab.trace.PoseTrace;
import goblinbob.mobends.lab.trace.TraceIO;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the reference: re-records every scenario through the original animation code and
 * compares it with the committed golden trace. Any refactor of the mod's animation sources that
 * changes a bone pose shows up here. Regenerate with {@code gradle record} only on purpose.
 */
public class ReferenceStabilityTest
{
    /** Trace files are rounded to 1e-6; angles derived from them differ by well under this. */
    private static final double MAX_ANGLE_DEG = 0.001;
    private static final double MAX_OFFSET = 0.0001;

    @TestFactory
    List<DynamicTest> referenceMatchesGolden()
    {
        return Scenarios.all().stream().map(scenario -> DynamicTest.dynamicTest(scenario.id(), () -> check(scenario))).collect(Collectors.toList());
    }

    private void check(Scenario scenario) throws Exception
    {
        Path file = TraceIO.fileFor(LabPaths.golden(), scenario.kind.id(), scenario.name);
        assertTrue(Files.exists(file), "missing golden trace " + file + " (run: gradle record)");

        PoseTrace golden = TraceIO.read(file);
        PoseTrace fresh = new ReferenceSession(scenario).run();
        ComparisonReport report = PoseComparator.compare(scenario.id(), golden, fresh);

        if (!report.isWithin(MAX_ANGLE_DEG, MAX_OFFSET))
        {
            System.out.println(report.toMarkdown());
        }
        assertTrue(report.isWithin(MAX_ANGLE_DEG, MAX_OFFSET), "reference output drifted from golden trace for " + scenario.id());
    }
}
