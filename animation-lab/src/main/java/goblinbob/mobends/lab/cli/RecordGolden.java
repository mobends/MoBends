package goblinbob.mobends.lab.cli;

import goblinbob.mobends.lab.scenarios.Animators;
import goblinbob.mobends.lab.scenarios.Scenarios;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.Scenario;
import goblinbob.mobends.lab.trace.PoseTrace;
import goblinbob.mobends.lab.trace.TraceIO;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Records golden traces from the entity's animator: accepts its current output as the expected
 * one. Only for new scenarios, or when a change to an animator is intended; name them explicitly.
 *
 * Usage: RecordGolden &lt;golden-dir&gt; entity[/scenario] ...
 */
public class RecordGolden
{
    public static void main(String[] args) throws Exception
    {
        if (args.length < 2)
        {
            System.err.println("usage: RecordGolden <golden-dir> entity[/scenario] ...");
            System.exit(2);
        }

        Path root = Paths.get(args[0]);
        for (Scenario scenario : Scenarios.all())
        {
            if (!Animators.has(scenario.kind) || !matches(scenario, args)) continue;

            PoseTrace trace = new KumoSession(scenario, KumoSession.loadAnimator(Animators.forKind(scenario.kind))).run();
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
