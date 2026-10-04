package goblinbob.mobends.lab;

import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.lab.compare.ComparisonReport;
import goblinbob.mobends.lab.compare.PoseComparator;
import goblinbob.mobends.lab.scenarios.Animators;
import goblinbob.mobends.lab.scenarios.Scenarios;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.Scenario;
import goblinbob.mobends.lab.trace.FramePose;
import goblinbob.mobends.lab.trace.PoseTrace;
import goblinbob.mobends.lab.trace.TraceIO;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The mobs that are model definitions animate as their Java data classes did: every golden
 * scenario, replayed on the data their definition makes (its entity scope, components and
 * settings), matches the golden recorded from the Java data class.
 */
public class DefinedParityTest
{

    private static final Map<EntityKind, String> DEFINITIONS = new EnumMap<>(EntityKind.class);

    static
    {
        DEFINITIONS.put(EntityKind.PLAYER, "player");
        DEFINITIONS.put(EntityKind.ZOMBIE, "zombie");
        DEFINITIONS.put(EntityKind.ZOMBIE_VILLAGER, "zombie_villager");
        DEFINITIONS.put(EntityKind.SKELETON, "skeleton");
        DEFINITIONS.put(EntityKind.PIG_ZOMBIE, "pig_zombie");
        DEFINITIONS.put(EntityKind.SQUID, "squid");
        DEFINITIONS.put(EntityKind.SPIDER, "spider");
        DEFINITIONS.put(EntityKind.WOLF, "wolf");
    }

    @TestFactory
    List<DynamicTest> definedMobsMatchTheGoldens()
    {
        return Scenarios.all().stream()
                .filter(s -> Animators.has(s.kind) && DEFINITIONS.containsKey(s.kind))
                .map(scenario -> DynamicTest.dynamicTest(scenario.id(), () -> check(scenario)))
                .collect(Collectors.toList());
    }

    private void check(Scenario scenario) throws Exception
    {
        EntityModelDefinition definition = ModelDefinitions.INSTANCE.load(new ResourceLocation("mobends", "bends/models/" + DEFINITIONS.get(scenario.kind) + ".json"));
        PoseTrace golden = TraceIO.read(TraceIO.fileFor(LabPaths.golden(), scenario.kind.id(), scenario.name));
        PoseTrace actual = new KumoSession(scenario, KumoSession.loadAnimator(Animators.forKind(scenario.kind)),
                entity -> DefinedEntityData.create(definition, entity)).run();
        // A definition may have bones the Java data class didn't (the hat layer): those the golden lacks aren't compared.
        Set<String> recorded = golden.frames.get(0).bones.keySet();
        for (FramePose frame : actual.frames)
        {
            frame.bones.keySet().retainAll(recorded);
        }
        ComparisonReport report = PoseComparator.compare(scenario.id(), golden, actual);
        assertTrue(report.isWithin(KumoParityTest.MAX_ANGLE_DEG, KumoParityTest.MAX_OFFSET),
                String.format("The defined %s deviates from the golden trace for %s (worst %.3f deg / %.4f offset)%n%s",
                        DEFINITIONS.get(scenario.kind), scenario.id(), report.worstAngleDeg(), report.worstOffset(), report.toMarkdown()));
    }

}
