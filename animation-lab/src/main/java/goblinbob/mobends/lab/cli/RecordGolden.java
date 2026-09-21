package goblinbob.mobends.lab.cli;

import goblinbob.mobends.lab.scenarios.Scenarios;
import goblinbob.mobends.lab.sim.ReferenceSession;
import goblinbob.mobends.lab.sim.Scenario;
import goblinbob.mobends.lab.trace.PoseTrace;
import goblinbob.mobends.lab.trace.TraceIO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Records the golden traces from the reference (procedural) animation code.
 *
 * Usage: RecordGolden &lt;golden-dir&gt; [scenarioId...]
 */
public class RecordGolden
{
    public static void main(String[] args) throws IOException
    {
        if (args.length < 1)
        {
            System.err.println("usage: RecordGolden <golden-dir> [entity/scenario ...]");
            System.exit(2);
        }

        Path root = Paths.get(args[0]);
        for (Scenario scenario : Scenarios.all())
        {
            if (args.length > 1 && !matches(scenario, args)) continue;

            PoseTrace trace = new ReferenceSession(scenario).run();
            Path file = TraceIO.fileFor(root, scenario.kind.id(), scenario.name);
            TraceIO.write(trace, file);
            System.out.printf("recorded %-40s %5d frames  %7d bytes%n", scenario.id(), trace.frames.size(), Files.size(file));
        }
    }

    private static boolean matches(Scenario scenario, String[] args)
    {
        for (int i = 1; i < args.length; i++)
        {
            if (scenario.id().equals(args[i]) || scenario.kind.id().equals(args[i])) return true;
        }
        return false;
    }
}
