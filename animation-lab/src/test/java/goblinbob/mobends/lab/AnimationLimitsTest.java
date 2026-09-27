package goblinbob.mobends.lab;

import goblinbob.mobends.core.kumo.AnimationLimits;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.lab.scenarios.Animators;
import goblinbob.mobends.lab.scenarios.Scenarios;
import goblinbob.mobends.lab.scenarios.Scripts;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.Scenario;
import goblinbob.mobends.lab.trace.FramePose;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Animation from untrusted sources (resource packs) under the server's limits: every part's offset
 * and the whole body's offsets stay within the limits of where the trusted animation puts them.
 */
public class AnimationLimitsTest
{

    private static final AnimationLimits LIMITS = new AnimationLimits(4, 16);

    /** Throws the left arm 40 units out and the whole body 100 units up. */
    private static final String FLING = "{\"formatVersion\": 2, \"layers\": [{\"entryNode\": \"fling\", \"nodes\": {\"fling\": {\"pose\": ["
            + "{\"driver\": \"core:offset\", \"bone\": \"leftArm\", \"x\": 40, \"y\": 0, \"z\": 0},"
            + "{\"driver\": \"core:vector\", \"bone\": \"globalOffset\", \"x\": 0, \"y\": 100, \"z\": 0, \"snap\": true}"
            + "]}}}]}";

    /** Raises the right arm: rotations only. */
    private static final String RAISE = "{\"formatVersion\": 2, \"layers\": [{\"entryNode\": \"raise\", \"nodes\": {\"raise\": {\"pose\": ["
            + "{\"driver\": \"core:axis_rotate\", \"bone\": \"rightArm\", \"axis\": \"X\", \"angle\": -150, \"space\": \"OVERRIDE\"}"
            + "]}}}]}";

    private static final Scenario WALK = new Scenario(EntityKind.PLAYER, "limits_walk", Scenarios.FPS, 30, (tick, in) -> Scripts.walk(in, Scripts.WALK_SPEED));

    private static FramePose last(Scenario scenario, AnimatorTemplate base, boolean baseTrusted, String extension, boolean extensionTrusted, boolean limited) throws Exception
    {
        List<AnimatorTemplate> extensions = extension == null ? Collections.emptyList() : Collections.singletonList(json(extension));
        KumoSession session = new KumoSession(scenario, base, baseTrusted, extensions, Collections.nCopies(extensions.size(), extensionTrusted));
        session.animator.setLimits(limited ? LIMITS : null);
        List<FramePose> frames = session.run().frames;
        return frames.get(frames.size() - 1);
    }

    /** The player's own animator, with an untrusted extension (or none), limited. */
    private static List<FramePose> frames(Scenario scenario, String extension) throws Exception
    {
        List<AnimatorTemplate> extensions = extension == null ? Collections.emptyList() : Collections.singletonList(json(extension));
        KumoSession session = new KumoSession(scenario, player(), true, extensions, Collections.nCopies(extensions.size(), false));
        session.animator.setLimits(LIMITS);
        return session.run().frames;
    }

    private static AnimatorTemplate json(String text)
    {
        return KumoSerializer.INSTANCE.gson.fromJson(text, AnimatorTemplate.class);
    }

    private static AnimatorTemplate player() throws Exception
    {
        return KumoSession.loadAnimator(Animators.forKind(EntityKind.PLAYER));
    }

    @Test
    void untrustedExtensionsStayWithinTheLimitsOfTheTrustedPose() throws Exception
    {
        FramePose own = last(WALK, player(), true, null, true, true);
        FramePose limited = last(WALK, player(), true, FLING, false, true);

        assertEquals(4, distance(offset(own, "leftArm"), offset(limited, "leftArm")), 1e-3, "the arm moves out, but only as far as allowed");
        assertEquals(16, distance(body(own), body(limited)), 1e-3, "and so does the whole body");
    }

    @Test
    void trustedExtensionsAndServersWithoutLimitsAreNotLimited() throws Exception
    {
        FramePose own = last(WALK, player(), true, null, true, true);
        assertEquals(40, distance(offset(own, "leftArm"), offset(last(WALK, player(), true, FLING, true, true), "leftArm")), 1e-3, "a mod's extension");
        assertEquals(40, distance(offset(own, "leftArm"), offset(last(WALK, player(), true, FLING, false, false), "leftArm")), 1e-3, "no limits (ALLOW, singleplayer)");
    }

    @Test
    void limitsFollowTheTrustedAnimationSoItsOwnLargeMovesStay() throws Exception
    {
        // Swimming moves the player's whole body about 24 units: more than the limit, but it's the
        // trusted animation's own move, so an untrusted extension on top leaves it alone.
        Scenario swim = Scenarios.byId("player/fly_and_swim");
        List<FramePose> own = frames(swim, null);
        List<FramePose> extended = frames(swim, RAISE);
        double deepest = 0, apart = 0;
        for (int i = 0; i < own.size(); i++)
        {
            deepest = Math.max(deepest, length(body(own.get(i))));
            apart = Math.max(apart, distance(body(own.get(i)), body(extended.get(i))));
        }
        assertTrue(deepest > 16, "the player swims deep: " + deepest);
        assertEquals(0, apart, 1e-4);
    }

    @TestFactory
    List<DynamicTest> theLimitsHoldOnEveryFrameOfEveryScenario()
    {
        List<DynamicTest> tests = new ArrayList<>();
        for (Scenario scenario : Scenarios.all())
        {
            if (!Animators.has(scenario.kind)) continue;
            tests.add(DynamicTest.dynamicTest(scenario.id(), () -> {
                AnimatorTemplate base = KumoSession.loadAnimator(Animators.forKind(scenario.kind));
                List<FramePose> own = new KumoSession(scenario, base).run().frames;
                KumoSession session = new KumoSession(scenario, base, true, Collections.singletonList(flingEverything(own.get(0))), Collections.singletonList(false));
                session.animator.setLimits(LIMITS);
                List<FramePose> limited = session.run().frames;
                for (int i = 0; i < own.size(); i++)
                {
                    for (String vector : own.get(i).vectors.keySet())
                    {
                        double d = distance(own.get(i).vectors.get(vector).vt, limited.get(i).vectors.get(vector).vt);
                        assertTrue(d <= LIMITS.maxBodyOffset + 1e-3, String.format("%s frame %d: %s moved %.3f", scenario.id(), i, vector, d));
                    }
                    for (String bone : own.get(i).bones.keySet())
                    {
                        float[] a = own.get(i).bones.get(bone).o, b = limited.get(i).bones.get(bone).o;
                        if (a == null || b == null) continue;
                        double d = distance(a, b);
                        assertTrue(d <= LIMITS.maxPartOffset + 1e-3, String.format("%s frame %d: %s moved %.3f", scenario.id(), i, bone, d));
                    }
                }
            }));
        }
        return tests;
    }

    /** An extension that pushes every part and both body vectors (under both of the global one's names) far out, every frame. */
    private static AnimatorTemplate flingEverything(FramePose frame)
    {
        StringBuilder items = new StringBuilder();
        for (String bone : frame.bones.keySet())
        {
            if (frame.bones.get(bone).o == null) continue;
            items.append("{\"driver\": \"core:offset\", \"bone\": \"").append(bone).append("\", \"x\": {\"add\": [30, {\"mul\": [\"ticks\", 3]}]}, \"y\": -30, \"z\": 30},");
        }
        for (String vector : new String[] { "root", "globalOffset", "localOffset" })
        {
            items.append("{\"driver\": \"core:vector\", \"bone\": \"").append(vector).append("\", \"x\": 100, \"y\": {\"mul\": [\"ticks\", 5]}, \"z\": -100, \"space\": \"PRE\"},");
        }
        items.setLength(items.length() - 1);
        return json("{\"formatVersion\": 2, \"layers\": [{\"entryNode\": \"fling\", \"nodes\": {\"fling\": {\"pose\": [" + items + "]}}}]}");
    }

    @Test
    void infiniteAndNotANumberValuesDontGetPast() throws Exception
    {
        String broken = "{\"formatVersion\": 2, \"layers\": [{\"entryNode\": \"broken\", \"nodes\": {\"broken\": {\"pose\": ["
                + "{\"driver\": \"core:offset\", \"bone\": \"leftArm\", \"x\": {\"div\": [1, 0]}, \"y\": {\"sqrt\": [-1]}, \"z\": 0},"
                + "{\"driver\": \"core:vector\", \"bone\": \"globalOffset\", \"x\": {\"div\": [-1, 0]}, \"y\": {\"div\": [1, 0]}, \"z\": 0, \"snap\": true}"
                + "]}}}]}";
        FramePose own = last(WALK, player(), true, null, true, true);
        FramePose limited = last(WALK, player(), true, broken, false, true);
        for (float v : body(limited)) assertTrue(Float.isFinite(v), "the body stays finite");
        assertTrue(distance(body(own), body(limited)) <= 16 + 1e-3);
        for (float v : offset(limited, "leftArm")) assertTrue(Float.isFinite(v), "the arm's offset stays finite");
        assertTrue(distance(offset(own, "leftArm"), offset(limited, "leftArm")) <= 4 + 1e-3);

        String badRotation = "{\"formatVersion\": 2, \"layers\": [{\"entryNode\": \"bad\", \"nodes\": {\"bad\": {\"pose\": ["
                + "{\"driver\": \"core:axis_rotate\", \"bone\": \"rightArm\", \"axis\": \"X\", \"angle\": {\"sqrt\": [-1]}, \"space\": \"OVERRIDE\"}]}}}]}";
        for (float v : last(WALK, player(), true, badRotation, false, true).bones.get("rightArm").rt) assertTrue(Float.isFinite(v), "the arm's rotation stays finite");
    }

    @Test
    void anUntrustedAnimatorIsLimitedAroundTheRestPosition() throws Exception
    {
        FramePose limited = last(WALK, json(FLING), false, null, true, true);
        assertEquals(4, length(offset(limited, "leftArm")), 1e-3);
        assertEquals(16, length(body(limited)), 1e-3);
    }

    private static float[] offset(FramePose frame, String bone)
    {
        return frame.bones.get(bone).o;
    }

    private static float[] body(FramePose frame)
    {
        return frame.vectors.get("globalOffset").vt;
    }

    private static double distance(float[] a, float[] b)
    {
        double dx = a[0] - b[0], dy = a[1] - b[1], dz = a[2] - b[2];
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static double length(float[] a)
    {
        return Math.sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2]);
    }

}
