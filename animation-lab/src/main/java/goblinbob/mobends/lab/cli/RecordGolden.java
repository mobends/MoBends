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
import java.util.Arrays;

/**
 * Records golden traces from the entity's animator: accepts its current output as the expected
 * one. Only for new scenarios, or when a change to an animator is intended; name them explicitly.
 *
 * Usage: RecordGolden [--legacy] &lt;golden-dir&gt; entity[/scenario] ...
 *
 * With {@code --legacy}, the legacy resource pack's animators are recorded instead (into
 * {@code golden/legacy}, see LegacyPackTest).
 */
public class RecordGolden
{
    public static void main(String[] args) throws Exception
    {
        boolean legacy = args.length > 0 && args[0].equals("--legacy");
        if (legacy)
        {
            args = Arrays.copyOfRange(args, 1, args.length);
        }
        if (args.length < 2)
        {
            System.err.println("usage: RecordGolden [--legacy] <golden-dir> entity[/scenario] ...");
            System.exit(2);
        }

        Path root = Paths.get(args[0]);
        for (Scenario scenario : Scenarios.all())
        {
            String animator = legacy ? Animators.legacyForKind(scenario.kind) : Animators.forKind(scenario.kind);
            if (animator == null || !matches(scenario, args)) continue;

            PoseTrace trace = new KumoSession(scenario, KumoSession.loadAnimator(animator)).run();
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
