package goblinbob.mobends.lab;

import net.minecraft.world.World;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.client.Minecraft;
import goblinbob.mobends.lab.sim.LabBootstrap;
import goblinbob.mobends.core.types.selector.SelectorExpression;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static goblinbob.mobends.lab.scenarios.Scripts.WALK_SPEED;
import static goblinbob.mobends.lab.scenarios.Scripts.between;
import static goblinbob.mobends.lab.scenarios.Scripts.walk;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Precedence between entity types (misc/kumo-format.md, "Entity types and selectors") and the example type pack. */
public class EntityTypesTest
{

    private static final Path EXAMPLE = LabPaths.root().resolve("../misc/examples/player-name-type/assets/mobends_example/bends");

    private static TypeOrder.Ranked type(String id, int rank, int specificity)
    {
        return new TypeOrder.Ranked()
        {
            private int currentRank = rank;
            @Override public String getId() { return id; }
            @Override public int getRank() { return currentRank; }
            @Override public void setRank(int newRank) { currentRank = newRank; }
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
    void reorderingChangesAsFewRanksAsItCan()
    {
        TypeOrder.Ranked a = type("a", 0, 1);
        TypeOrder.Ranked b = type("b", 0, 1);
        TypeOrder.Ranked shared = type("c:shared", 0, 0);
        TypeOrder.Ranked d = type("d", 5, 1);

        // Another entity's window put d above the shared type.
        assertEquals(Collections.emptyList(), TypeOrder.rankInOrder(Arrays.asList(d, shared)));
        // This one wants b, a, shared: only b needs a higher rank.
        assertEquals(Collections.singletonList(b), TypeOrder.rankInOrder(Arrays.asList(b, a, shared)));
        List<TypeOrder.Ranked> sorted = new ArrayList<>(Arrays.asList(shared, a, b));
        sorted.sort(TypeOrder.PRECEDENCE);
        assertEquals(Arrays.asList(b, a, shared), sorted);
        // d still comes before the shared type.
        assertSame(d, TypeOrder.first(Arrays.asList(shared, d)));
    }

    @Test
    void aTypeFileNeedsTheCurrentFormatVersion()
    {
        assertTypeRefused("needs a \"formatVersion\"", "{\"id\": \"x:y\"}");
        assertTypeRefused("update Mo' Bends", "{\"formatVersion\": " + (EntityTypeDefinition.FORMAT_VERSION + 1) + ", \"id\": \"x:y\"}");
        assertTypeRefused("no longer reads", "{\"formatVersion\": 1, \"id\": \"x:y\"}");
    }

    private static void assertTypeRefused(String message, String json)
    {
        MalformedKumoTemplateException e = assertThrows(MalformedKumoTemplateException.class, () -> EntityTypeDefinition.parse(json));
        assertTrue(e.getMessage().contains(message), e.getMessage());
    }

    private static int specificity(String selector) throws Exception
    {
        return EntityTypeDefinition.parse("{\"formatVersion\": 2, \"id\": \"x:t\", \"selector\": " + selector.replace('\'', '"') + "}").specificity();
    }

    @Test
    void specificityCountsConditionsNotCombinators() throws Exception
    {
        assertEquals(0, EntityTypeDefinition.parse("{\"formatVersion\": 2, \"id\": \"x:none\"}").specificity());
        assertEquals(1, specificity("{'core:entity_type': ['minecraft:player']}"));
        // An "or" is as specific as its broadest branch, a "not" counts one: nesting can't inflate it.
        assertEquals(1, specificity("{'or': [{'core:player_name': ['A']}, {'core:player_name': ['B']}, {'core:player_name': ['C']}]}"));
        assertEquals(1, specificity("{'not': [{'and': [{'core:player_name': ['A']}, {'core:player_name': ['B']}]}]}"));
        assertEquals(2, specificity("{'and': [{'core:entity_type': ['minecraft:player']}, "
                + "{'or': [{'core:player_name': ['A']}, {'not': [{'core:player_name': ['B']}]}]}]}"));
        // An "if" counts its condition and the fewer of its branches'.
        assertEquals(2, specificity("{'if': [{'core:player_name': ['A']}, {'core:entity_type': ['minecraft:player']}, "
                + "{'and': [{'core:player_name': ['B']}, {'mobends:skin_variant': ['slim']}]}]}"));
        assertEquals(0, specificity("true"));
    }

    @Test
    void aSelectorIsAnExpressionOverTheSelectorSafeOperations() throws Exception
    {
        LabBootstrap.ensure();
        SelectorExpression selector = SelectorExpression.compile(json("{'and': [{'core:entity_type': ['minecraft:player', 'minecraft:zombie']}, "
                + "{'not': [{'core:entity_type': ['minecraft:husk']}]}, {'or': [{'core:player_name': ['Notch']}, {'mobends:skin_variant': ['slim']}]}]}"));
        List<ResourceLocation> types = new ArrayList<>();
        selector.collectEntityTypes(types);
        assertEquals(Arrays.asList(new ResourceLocation("minecraft:player"), new ResourceLocation("minecraft:zombie")), types);
        // A skin variant changes when the skin downloads.
        assertFalse(selector.isStable());
        assertTrue(SelectorExpression.compile(json("{'core:player_name': ['Notch']}")).isStable());

        World world = new World();
        Minecraft.getMinecraft().world = world;
        assertTrue(SelectorExpression.compile(json("{'core:entity_type': ['minecraft:zombie']}")).test(new EntityZombie(world)));
        assertFalse(SelectorExpression.compile(json("{'core:player_name': ['Notch']}")).test(new EntityZombie(world)));
    }

    @Test
    void aSelectorReadsNoEntityData() throws Exception
    {
        LabBootstrap.ensure();
        MalformedKumoTemplateException name = assertThrows(MalformedKumoTemplateException.class,
                () -> SelectorExpression.compile(json("{'and': ['entityIsChild', {'core:entity_type': ['minecraft:zombie']}]}")));
        assertTrue(name.getMessage().contains("reads no names ('entityIsChild')"), name.getMessage());
        MalformedKumoTemplateException operation = assertThrows(MalformedKumoTemplateException.class,
                () -> SelectorExpression.compile(json("{'core:holds_any_item': ['main_hand']}")));
        assertTrue(operation.getMessage().contains("'core:holds_any_item' can't be in a type file's selector"), operation.getMessage());
    }

    private static com.google.gson.JsonElement json(String json)
    {
        return new com.google.gson.JsonParser().parse(json.replace('\'', '"'));
    }

    @Test
    void exampleTypeBeatsTheBuiltInPlayerType() throws Exception
    {
        EntityTypeDefinition example = EntityTypeDefinition.parse(new String(Files.readAllBytes(EXAMPLE.resolve("types/bendy_tester.json")), StandardCharsets.UTF_8));
        assertEquals("mobends_example:bendy_tester", example.id);
        assertEquals(2, example.specificity());

        TypeOrder.Ranked exampleType = type(example.id, 0, example.specificity());
        // Built-in types are one condition: "this entity's default model is this bender".
        TypeOrder.Ranked builtIn = type("mobends:player", 0, 1);
        assertSame(exampleType, TypeOrder.first(Arrays.asList(builtIn, exampleType)));
        // Unless the user ranks the built-in type above it.
        TypeOrder.Ranked rankedBuiltIn = type("mobends:player", 1, 1);
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
