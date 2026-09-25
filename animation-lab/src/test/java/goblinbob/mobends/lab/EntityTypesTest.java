package goblinbob.mobends.lab;

import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.types.EntityTypeDefinition;
import goblinbob.mobends.core.types.TypeOrder;
import goblinbob.mobends.lab.scenarios.Scenarios;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.Scenario;
import goblinbob.mobends.lab.trace.BonePose;
import goblinbob.mobends.lab.trace.FramePose;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static goblinbob.mobends.lab.scenarios.Scripts.WALK_SPEED;
import static goblinbob.mobends.lab.scenarios.Scripts.between;
import static goblinbob.mobends.lab.scenarios.Scripts.walk;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Precedence between entity types (misc/kumo-format.md, "Entity types and selectors") and the example type pack. */
public class EntityTypesTest
{

    private static final Path EXAMPLE = LabPaths.root().resolve("../misc/examples/player-name-type/assets/mobends_example/bends");

    private static TypeOrder.Ranked type(String id, int rank, int specificity)
    {
        return new TypeOrder.Ranked()
        {
            @Override public String getId() { return id; }
            @Override public int getRank() { return rank; }
            @Override public int getSpecificity() { return specificity; }
            @Override public String toString() { return id; }
        };
    }

    @Test
    void rankThenSpecificityThenId()
    {
        TypeOrder.Ranked general = type("a:general", 0, 1);
        TypeOrder.Ranked specific = type("z:specific", 0, 3);
        TypeOrder.Ranked ranked = type("m:ranked", 1, 0);
        TypeOrder.Ranked twin = type("b:twin", 0, 3);

        assertSame(specific, TypeOrder.first(Arrays.asList(general, specific)), "more conditions win");
        assertSame(ranked, TypeOrder.first(Arrays.asList(general, specific, ranked)), "a user rank beats specificity");
        assertSame(twin, TypeOrder.first(Arrays.asList(specific, twin)), "equal rank and specificity: the lexically first id");
        assertNull(TypeOrder.first(Arrays.<TypeOrder.Ranked>asList()));
    }

    @Test
    void specificityCountsConditionsNotCombinators() throws Exception
    {
        assertEquals(0, EntityTypeDefinition.parse("{\"id\": \"x:none\"}").specificity());
        assertEquals(1, EntityTypeDefinition.parse("{\"id\": \"x:one\", \"selector\": {\"type\": \"core:entity_type\", \"entityType\": \"minecraft:player\"}}").specificity());
        assertEquals(3, EntityTypeDefinition.parse("{\"id\": \"x:nested\", \"selector\": {\"type\": \"core:and\", \"conditions\": ["
                + "{\"type\": \"core:entity_type\", \"entityType\": \"minecraft:player\"},"
                + "{\"type\": \"core:or\", \"conditions\": [{\"type\": \"core:player_name\", \"name\": \"A\"}, {\"type\": \"core:not\", \"condition\": {\"type\": \"core:player_name\", \"name\": \"B\"}}]}"
                + "]}}").specificity());
    }

    @Test
    void exampleTypeBeatsTheBuiltInPlayerType() throws Exception
    {
        EntityTypeDefinition example = EntityTypeDefinition.parse(new String(Files.readAllBytes(EXAMPLE.resolve("types/bendy_tester.json")), StandardCharsets.UTF_8));
        assertEquals("mobends_example:bendy_tester", example.id);
        assertEquals(2, example.specificity());

        TypeOrder.Ranked exampleType = type(example.id, 0, example.specificity());
        // Built-in types are one condition: "this entity's default model is this bender".
        TypeOrder.Ranked builtIn = type("mobends-player", 0, 1);
        assertSame(exampleType, TypeOrder.first(Arrays.asList(builtIn, exampleType)));
        // Unless the user ranks the built-in type above it.
        TypeOrder.Ranked rankedBuiltIn = type("mobends-player", 1, 1);
        assertSame(rankedBuiltIn, TypeOrder.first(Arrays.asList(rankedBuiltIn, exampleType)));
    }

    @Test
    void exampleAnimatorHoldsTheArmsForward() throws Exception
    {
        AnimatorTemplate template;
        try (Reader reader = Files.newBufferedReader(EXAMPLE.resolve("animators/zombie_arms.json"), StandardCharsets.UTF_8))
        {
            template = KumoSerializer.INSTANCE.gson.fromJson(reader, AnimatorTemplate.class);
        }
        Scenario scenario = new Scenario(EntityKind.PLAYER, "example_zombie_arms", Scenarios.FPS, 120, (tick, in) -> {
            if (between(tick, 30, 120)) walk(in, WALK_SPEED);
        });
        List<FramePose> frames = new KumoSession(scenario, template).run().frames;

        float[] firstLeg = null;
        boolean legsMoved = false;
        for (FramePose frame : frames.subList(frames.size() / 2, frames.size()))
        {
            for (String arm : new String[] { "rightArm", "leftArm" })
            {
                float[] q = frame.bones.get(arm).rt;
                double angle = Math.toDegrees(2 * Math.atan2(q[0], q[3]));
                assertTrue(Math.abs(q[1]) < 1e-4 && Math.abs(q[2]) < 1e-4, arm + " turns about X only");
                assertTrue(Math.abs(angle) > 78 && Math.abs(angle) < 92, arm + " is held forward, not at " + angle + "°");
            }
            BonePose leg = frame.bones.get("rightLeg");
            if (firstLeg == null) firstLeg = leg.rt.clone();
            else if (!Arrays.equals(firstLeg, leg.rt)) legsMoved = true;
        }
        assertTrue(legsMoved, "the rest of the player animator still runs (the legs walk)");
    }

}
