package goblinbob.mobends.lab.cli;

import goblinbob.mobends.lab.compare.ComparisonReport;
import goblinbob.mobends.lab.compare.PoseComparator;
import goblinbob.mobends.lab.scenarios.Animators;
import goblinbob.mobends.lab.scenarios.Scenarios;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.Scenario;
import goblinbob.mobends.lab.trace.PoseTrace;
import goblinbob.mobends.lab.trace.TraceIO;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Runs every scenario whose entity has a KUMO animator and prints the parity report against the
 * golden trace. Usage: CompareKumo &lt;golden-dir&gt; [entity/scenario ...]
 */
public class CompareKumo
{
    public static void main(String[] args) throws Exception
    {
        Path root = Paths.get(args[0]);
        for (Scenario scenario : Scenarios.all())
        {
            if (!Animators.has(scenario.kind)) continue;
            if (args.length > 1 && !matches(scenario, args)) continue;

            PoseTrace golden = TraceIO.read(TraceIO.fileFor(root, scenario.kind.id(), scenario.name));
            PoseTrace actual = new KumoSession(scenario, KumoSession.loadAnimator(Animators.forKind(scenario.kind))).run();
            ComparisonReport report = PoseComparator.compare(scenario.id(), golden, actual);
            System.out.println(report.toMarkdown());

            Path dump = root.getParent().resolve("build").resolve("kumo-traces").resolve(scenario.kind.id()).resolve(scenario.name + ".json.gz");
            TraceIO.write(actual, dump);
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
