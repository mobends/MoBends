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
    private static final String RAISE = "{\"formatVersion\": 2, \"layers\": [{\"defaultOnEntry\": \"through\", \"nodes\": {"
            + "\"through\": {\"type\": \"core:fallthrough\", \"connections\": [{\"target\": \"raise\", \"transitionDuration\": 5, \"triggerCondition\": {\"type\": \"core:ticks_passed\", \"ticksToPass\": 10}}]},"
            + "\"raise\": {\"pose\": [{\"driver\": \"core:axis_rotate\", \"bone\": \"rightArm\", \"axis\": \"X\", \"angle\": -90, \"space\": \"OVERRIDE\"}],"
            + "  \"connections\": [{\"target\": \"back\", \"transitionDuration\": 5, \"triggerCondition\": {\"type\": \"core:ticks_passed\", \"ticksToPass\": 20}}]},"
            + "\"back\": {\"type\": \"core:fallthrough\"}"
            + "}}]}";

    @Test
    void definitionsNeedAnIdATypeAndAnAnimator() throws Exception
    {
        assertMalformed("needs an 'id'", "{\"formatVersion\": 2, \"type\": \"a:b\", \"animator\": \"a:c.json\"}");
        assertMalformed("needs the 'type'", "{\"formatVersion\": 2, \"id\": \"x:y\", \"animator\": \"a:c.json\"}");
        assertMalformed("needs a \"formatVersion\"", "{\"id\": \"x:y\", \"type\": \"a:b\", \"animator\": \"a:c.json\"}");
        assertMalformed("needs an 'animator'", "{\"formatVersion\": 2, \"id\": \"x:y\", \"type\": \"a:b\"}");

        ExtensionDefinition wave = ExtensionDefinition.parse(new String(Files.readAllBytes(EXAMPLE.resolve("extensions/wave.json")), StandardCharsets.UTF_8));
        assertEquals("mobends_wave:wave", wave.id);
        // A built-in type's id is its model's key: the registering mod and the name it gave the model
        // (PlayerBender's "player"), or else the entity's id.
        assertEquals("mobends:player", wave.type);
        assertEquals("mobends:player", EntityKind.PLAYER.benderKey);
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

    /** Vanilla from tick 10 to tick 30; a second layer that would go vanilla is disabled by its "when". */
    private static final String VANILLA = "{\"formatVersion\": 2, \"layers\": ["
            + "{\"defaultOnEntry\": \"animated\", \"nodes\": {"
            + "\"animated\": {\"type\": \"core:fallthrough\", \"connections\": [{\"target\": \"vanilla\", \"triggerCondition\": {\"type\": \"core:ticks_passed\", \"ticksToPass\": 10}}]},"
            + "\"vanilla\": {\"type\": \"core:vanilla\", \"tags\": [\"vanilla\"], \"connections\": [{\"target\": \"again\", \"triggerCondition\": {\"type\": \"core:ticks_passed\", \"ticksToPass\": 20}}]},"
            + "\"again\": {\"type\": \"core:fallthrough\"}}},"
            + "{\"when\": {\"type\": \"core:state\", \"state\": \"SPRINTING\"}, \"defaultOnEntry\": \"vanilla\", \"nodes\": {\"vanilla\": {\"type\": \"core:vanilla\"}}}"
            + "]}";

    @Test
    void aVanillaNodeAsksForVanillaWhileTheAnimatorKeepsRunning() throws Exception
    {
        Scenario scenario = new Scenario(EntityKind.PLAYER, "extension_vanilla", Scenarios.FPS, 40, (tick, in) -> Scripts.walk(in, Scripts.WALK_SPEED));
        KumoSession session = new KumoSession(scenario, KumoSession.loadAnimator(Animators.forKind(EntityKind.PLAYER)),
                Collections.singletonList(KumoSerializer.INSTANCE.gson.fromJson(VANILLA, AnimatorTemplate.class)));

        float[] legWhenVanillaStarted = null;
        boolean legMovedWhileVanilla = false;
        for (int frame = 0; frame < scenario.frameCount(); frame++)
        {
            float[] leg = session.step().bones.get("rightLeg").rt;
            float tick = frame * 20F / Scenarios.FPS;
            boolean vanilla = session.animator.wantsVanilla();
            if (tick < 9 || tick > 32)
            {
                assertTrue(!vanilla, "animated on tick " + tick);
            }
            else if (tick > 12 && tick < 29)
            {
                assertTrue(vanilla, "vanilla on tick " + tick);
                assertTrue(session.animator.getActions().contains("vanilla"));
                if (legWhenVanillaStarted == null) legWhenVanillaStarted = leg.clone();
                else legMovedWhileVanilla |= angleBetween(legWhenVanillaStarted, leg) > 5;
            }
        }
        assertTrue(legMovedWhileVanilla, "the player's own animation keeps running underneath");
    }

    @Test
    void theVanillaSwimExampleIsVanillaInWaterOnly() throws Exception
    {
        Path pack = LabPaths.root().resolve("../misc/examples/vanilla-swim-extension/assets/mobends_vanilla_swim/bends");
        ExtensionDefinition extension = ExtensionDefinition.parse(new String(Files.readAllBytes(pack.resolve("extensions/vanilla_swim.json")), StandardCharsets.UTF_8));
        assertEquals("mobends:player", extension.type);
        AnimatorTemplate swim;
        try (Reader reader = Files.newBufferedReader(pack.resolve("animators/vanilla_swim.json")))
        {
            swim = KumoSerializer.INSTANCE.gson.fromJson(reader, AnimatorTemplate.class);
        }

        Scenario scenario = new Scenario(EntityKind.PLAYER, "extension_vanilla_swim", Scenarios.FPS, 40, (tick, in) -> {
            Scripts.walk(in, Scripts.WALK_SPEED);
            in.inWater = Scripts.between(tick, 10, 30);
        });
        KumoSession session = new KumoSession(scenario, KumoSession.loadAnimator(Animators.forKind(EntityKind.PLAYER)), Collections.singletonList(swim));
        for (int frame = 0; frame < scenario.frameCount(); frame++)
        {
            session.step();
            float tick = frame * 20F / Scenarios.FPS;
            if (tick < 9 || tick > 31) assertTrue(!session.animator.wantsVanilla(), "animated out of water, tick " + tick);
            else if (tick > 11 && tick < 29) assertTrue(session.animator.wantsVanilla(), "vanilla in water, tick " + tick);
        }
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
