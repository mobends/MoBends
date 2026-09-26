package goblinbob.mobends.lab;

import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.types.Extension;
import goblinbob.mobends.core.types.ExtensionDefinition;
import goblinbob.mobends.lab.scenarios.Animators;
import goblinbob.mobends.lab.scenarios.Scenarios;
import goblinbob.mobends.lab.scenarios.Scripts;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.Scenario;
import goblinbob.mobends.lab.trace.FramePose;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Extensions (misc/kumo-format.md, "Extensions") and the fallthrough node, on a player. */
public class ExtensionsTest
{

    private static final Path EXAMPLE = LabPaths.root().resolve("../misc/examples/wave-extension/assets/mobends_wave/bends");

    /**
     * Lets the player's own pose through for 10 ticks, fades (over 5 ticks) to holding the right
     * arm out, holds it for 20 ticks, then fades back.
     */
    private static final String RAISE = "{\"formatVersion\": 2, \"layers\": [{\"type\": \"KEYFRAME\", \"entryNode\": \"through\", \"nodes\": {"
            + "\"through\": {\"type\": \"core:fallthrough\", \"connections\": [{\"target\": \"raise\", \"transitionDuration\": 5, \"triggerCondition\": {\"type\": \"core:ticks_passed\", \"ticksToPass\": 10}}]},"
            + "\"raise\": {\"pose\": [{\"driver\": \"core:axis_rotate\", \"bone\": \"rightArm\", \"axis\": \"X\", \"angle\": -90, \"space\": \"OVERRIDE\"}],"
            + "  \"connections\": [{\"target\": \"back\", \"transitionDuration\": 5, \"triggerCondition\": {\"type\": \"core:ticks_passed\", \"ticksToPass\": 20}}]},"
            + "\"back\": {\"type\": \"core:fallthrough\"}"
            + "}}]}";

    @Test
    void definitionsNeedAnIdATypeAndAnAnimator() throws Exception
    {
        assertMalformed("needs an 'id'", "{\"type\": \"a:b\", \"animator\": \"a:c.json\"}");
        assertMalformed("needs the 'type'", "{\"id\": \"x:y\", \"animator\": \"a:c.json\"}");
        assertMalformed("needs an 'animator'", "{\"id\": \"x:y\", \"type\": \"a:b\"}");

        ExtensionDefinition wave = ExtensionDefinition.parse(new String(Files.readAllBytes(EXAMPLE.resolve("extensions/wave.json")), StandardCharsets.UTF_8));
        assertEquals("mobends_wave:wave", wave.id);
        // A built-in type's id is its model's key; the player's is "mobends-" + "player" (PlayerBender),
        // not "mobends-minecraft:player" like the entities keyed by their registry name.
        assertEquals("mobends-player", wave.type);
        assertEquals("mobends-player", EntityKind.PLAYER.benderKey);
    }

    @Test
    void theFirstExtensionInPrecedenceOrderGoesOnTop()
    {
        Extension early = new Extension("a:early", "test", "t", new ResourceLocation("a", "early.json"));
        Extension late = new Extension("b:late", "test", "t", new ResourceLocation("b", "late.json"));

        assertEquals(Arrays.asList(late.getAnimator(), early.getAnimator()), Extension.layerOrder(Arrays.asList(early, late)),
                "same rank: the lexically first id is first in the list, so its layers go on last, on top");

        late.setRank(1);
        assertEquals(Arrays.asList(early.getAnimator(), late.getAnimator()), Extension.layerOrder(Arrays.asList(early, late)),
                "a higher rank goes on top");
    }

    private static void assertMalformed(String message, String json)
    {
        MalformedKumoTemplateException e = assertThrows(MalformedKumoTemplateException.class, () -> ExtensionDefinition.parse(json));
        assertTrue(e.getMessage().contains(message), e.getMessage());
    }

    @Test
    void aFallthroughNodeLetsTheLayersBelowShowAndTransitionsFadeToThem() throws Exception
    {
        Scenario scenario = new Scenario(EntityKind.PLAYER, "extension_raise", Scenarios.FPS, 50, (tick, in) -> Scripts.lookAround(in, tick));
        List<FramePose> base = run(scenario, Collections.emptyList());
        List<FramePose> extended = run(scenario, Collections.singletonList(KumoSerializer.INSTANCE.gson.fromJson(RAISE, AnimatorTemplate.class)));

        int through = frameAt(8), raised = frameAt(25), back = frameAt(45);
        assertArrayEquals(target(base, through), target(extended, through), "before the transition the player's own arm shows");
        assertEquals(-90, xAngle(target(extended, raised)), 1e-3, "then the extension's arm");
        assertArrayEquals(target(base, back), target(extended, back), "after fading back to a fallthrough node, the player's own arm again");

        // During each fade the arm passes between the two instead of jumping.
        assertFades(base, extended, 10, 18);
        assertFades(base, extended, 30, 38);
    }

    private static void assertFades(List<FramePose> base, List<FramePose> extended, float fromTick, float toTick)
    {
        for (int frame = frameAt(fromTick); frame <= frameAt(toTick); frame++)
        {
            double toBase = angleBetween(target(base, frame), target(extended, frame));
            double toRaised = angleBetween(quaternionX(-90), target(extended, frame));
            if (toBase > 20 && toRaised > 20)
            {
                return;
            }
        }
        throw new AssertionError(String.format("Between ticks %s and %s the arm never was between the player's and the raised one.", fromTick, toTick));
    }

    @Test
    void theExampleWavesWhileStandingStillOnly() throws Exception
    {
        AnimatorTemplate wave;
        try (Reader reader = Files.newBufferedReader(EXAMPLE.resolve("animators/wave.json")))
        {
            wave = KumoSerializer.INSTANCE.gson.fromJson(reader, AnimatorTemplate.class);
        }
        List<AnimatorTemplate> extensions = Collections.singletonList(wave);

        Scenario standing = new Scenario(EntityKind.PLAYER, "extension_standing", Scenarios.FPS, 40, (tick, in) -> {});
        int last = frameAt(39);
        assertTrue(angleBetween(target(run(standing, Collections.emptyList()), last), target(run(standing, extensions), last)) > 90, "the arm is raised");

        Scenario walking = new Scenario(EntityKind.PLAYER, "extension_walking", Scenarios.FPS, 40, (tick, in) -> Scripts.walk(in, Scripts.WALK_SPEED));
        assertArrayEquals(target(run(walking, Collections.emptyList()), last), target(run(walking, extensions), last), "a walking player's own arm");
    }

    private static List<FramePose> run(Scenario scenario, List<AnimatorTemplate> extensions) throws Exception
    {
        return new KumoSession(scenario, KumoSession.loadAnimator(Animators.forKind(EntityKind.PLAYER)), extensions).run().frames;
    }

    private static int frameAt(float tick)
    {
        return Math.round(tick * Scenarios.FPS / 20F);
    }

    private static float[] target(List<FramePose> frames, int frame)
    {
        return frames.get(frame).bones.get("rightArm").rt;
    }

    private static float[] quaternionX(double degrees)
    {
        double half = Math.toRadians(degrees) / 2;
        return new float[] { (float) Math.sin(half), 0, 0, (float) Math.cos(half) };
    }

    private static double xAngle(float[] q)
    {
        return Math.toDegrees(2 * Math.atan2(q[0], q[3]));
    }

    private static double angleBetween(float[] a, float[] b)
    {
        double dot = Math.abs(a[0] * b[0] + a[1] * b[1] + a[2] * b[2] + a[3] * b[3]);
        return Math.toDegrees(2 * Math.acos(Math.min(1, dot)));
    }

}
