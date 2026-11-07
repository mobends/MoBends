package goblinbob.mobends.lab;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import goblinbob.mobends.core.definition.DefinitionMerge;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.lab.compare.ComparisonReport;
import goblinbob.mobends.lab.compare.PoseComparator;
import goblinbob.mobends.lab.scenarios.Animators;
import goblinbob.mobends.lab.scenarios.Scenarios;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.Scenario;
import goblinbob.mobends.lab.trace.PoseTrace;
import goblinbob.mobends.lab.trace.TraceIO;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The legacy resource pack (misc/legacy-pack): how the player, the zombie and the zombie villager
 * moved in Mo' Bends 1.X. It is a snapshot, not a fork: its animators, the clips they play and its
 * model definitions are its own copies, so nothing the mod changes reaches it.
 *
 * <ul>
 *     <li>Each scenario reproduces its own golden, in {@code golden/legacy}, recorded from the pack
 *     with {@code ./gradlew record --args="--legacy $PWD/golden/legacy <entity>[/scenario]"}.</li>
 *     <li>Everything the pack's files name is in the pack: the files it replaces the mod's with, and
 *     its own under {@code legacy/}.</li>
 *     <li>The zombie pigman, whose definition extends the zombie's, is the mod's with the pack as
 *     without it.</li>
 * </ul>
 */
public class LegacyPackTest
{
    private static final Path PACK = LabPaths.root().resolve("../misc/legacy-pack/assets/mobends/bends");
    private static final Path MOD = LabPaths.root().resolve("../src/main/resources/assets/mobends/bends");
    private static final Pattern KEY = Pattern.compile("mobends:bends/([a-z0-9_/]+\\.json)");
    /** The mod's files the pack names on purpose: the zombie pigman's animator, which the pack leaves to the mod. */
    private static final List<String> THE_MODS = Arrays.asList("animators/pig_zombie.json");

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

    @Test
    void thePackPlaysOnlyItsOwnFiles() throws Exception
    {
        List<String> outside = new ArrayList<>();
        try (Stream<Path> files = Files.walk(PACK))
        {
            for (Path file : files.filter(f -> f.toString().endsWith(".json")).collect(Collectors.toList()))
            {
                Matcher key = KEY.matcher(new String(Files.readAllBytes(file), StandardCharsets.UTF_8));
                while (key.find())
                {
                    if (!Files.exists(PACK.resolve(key.group(1))) && !THE_MODS.contains(key.group(1)))
                    {
                        outside.add(PACK.relativize(file) + " -> " + key.group(0));
                    }
                }
            }
        }
        assertTrue(outside.isEmpty(), "The legacy pack names files it doesn't have (copy them in, under legacy/): " + outside);
    }

    @Test
    void theHeadsTurnWithTheBodyButThePigmansIsTheMods() throws Exception
    {
        for (String name : new String[] { "player", "zombie" })
        {
            EntityModelDefinition definition = GSON.fromJson(json(PACK.resolve("models/" + name + ".json")), EntityModelDefinition.class);
            definition.validate();
            assertTrue(definition.bone("head").inheritRotation, name + ": the legacy head doesn't turn with the body");
        }

        JsonObject pigman = DefinitionMerge.merge(json(PACK.resolve("models/zombie.json")), json(PACK.resolve("models/pig_zombie.json")));
        JsonObject modPigman = DefinitionMerge.merge(json(MOD.resolve("models/zombie.json")), json(MOD.resolve("models/pig_zombie.json")));
        pigman.remove("@comment");
        modPigman.remove("@comment");
        assertEquals(modPigman, pigman, "With the legacy pack, the zombie pigman isn't the mod's (update misc/legacy-pack's pig_zombie.json)");
    }

    private static final Gson GSON = new Gson();

    private static JsonObject json(Path file) throws Exception
    {
        return JsonParser.parseString(new String(Files.readAllBytes(file), StandardCharsets.UTF_8)).getAsJsonObject();
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
