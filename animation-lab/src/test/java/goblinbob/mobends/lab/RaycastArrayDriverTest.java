package goblinbob.mobends.lab;

import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.RayGizmos;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.lab.scenarios.Scenarios;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.LabBootstrap;
import goblinbob.mobends.lab.sim.LabWorlds;
import goblinbob.mobends.lab.sim.Scenario;
import goblinbob.mobends.lab.trace.FramePose;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@code core:raycast_array}: a spread of rays round a direction, keeping the hit closest to it. */
class RaycastArrayDriverTest
{

    static
    {
        // The rays are the mod's drivers, which the lab registers: before the animators are parsed.
        LabBootstrap.ensure();
    }

    private static final String[] OUTPUTS = {"hitX", "hitY", "hitZ", "normalX", "normalY", "normalZ"};
    private static final String[] BONES = {"rightArm", "leftArm", "rightLeg", "leftLeg", "head", "body"};

    /**
     * What a golem at (0.5, 0, 0.5), with a stone block at (0, 0, 3) (and the floor's top at y 0),
     * casts with the given fields: each output, times 10 (the hit made relative to (0.5, 0, 0.5)),
     * as a bone's X angle.
     */
    private static double[] cast(String fields) throws Exception
    {
        StringBuilder define = new StringBuilder();
        StringBuilder out = new StringBuilder();
        StringBuilder pose = new StringBuilder();
        for (int i = 0; i < OUTPUTS.length; i++)
        {
            boolean hit = i < 3;
            define.append("'").append(OUTPUTS[i]).append("': {'state': ").append(hit ? "{'toDouble': [0]}" : "0").append("}, ");
            out.append(i > 0 ? ", " : "").append("'").append(OUTPUTS[i]).append("': 'node.").append(OUTPUTS[i]).append("'");
            String value = hit ? "{'toFloat': [{'sub': ['node." + OUTPUTS[i] + "', " + (i == 1 ? 0 : 0.5) + "]}]}" : "'node." + OUTPUTS[i] + "'";
            pose.append(", {'core:axis_rotate': {'bone': '").append(BONES[i]).append("', 'axis': 'x', 'angle': {'mul': [").append(value)
                    .append(", 10]}}, '@space': 'override', '@snap': true}");
        }
        String json = "{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'main', 'nodes': {'main': {'@define': {" + define + "'unused': {'state': 0}},"
                + "'core:pose': {'pose': [{'core:raycast_array': {" + fields + ", 'out': {" + out + "}}}" + pose + "]}}}}]}";
        AnimatorTemplate template = KumoSerializer.INSTANCE.gson.fromJson(json.replace('\'', '"'), AnimatorTemplate.class);
        List<FramePose> frames = new KumoSession(new Scenario(EntityKind.IRON_GOLEM, "raycast_array", Scenarios.FPS, 2, (tick, in) -> {},
                data -> LabWorlds.placeStone(data.getEntity().world, 0, 0, 3, 0, 0, 3)), template).run().frames;
        FramePose last = frames.get(frames.size() - 1);
        double[] values = new double[OUTPUTS.length];
        for (int i = 0; i < OUTPUTS.length; i++)
        {
            float[] q = last.bones.get(BONES[i]).rt;
            values[i] = Math.toDegrees(2 * Math.atan2(q[0], q[3])) / 10;
        }
        values[0] += 0.5;
        values[2] += 0.5;
        return values;
    }

    private static void assertCast(double[] expected, double[] actual)
    {
        for (int i = 0; i < expected.length; i++)
        {
            assertEquals(expected[i], actual[i], 1e-3, OUTPUTS[i]);
        }
    }

    @Test
    void whereTheMiddleRayHitsItKeepsThat() throws Exception
    {
        assertCast(new double[] {0.5, 1, 3.5, 0, 1, 0}, cast("'from': {'x': 0.5, 'y': 3, 'z': 3.5}, 'direction': {'y': -1}, 'distance': 5"));
    }

    @Test
    void whereTheMiddleMissesARayOffItFindsTheBlock() throws Exception
    {
        // Straight down 0.3 short of the block's edge (z 3), 2.5 long: the middle ray ends above the
        // floor; rays leaning towards +Z reach over the edge onto the top.
        double[] hit = cast("'from': {'x': 0.5, 'y': 3, 'z': 2.7}, 'direction': {'y': -1}, 'distance': 2.5, 'count': 24, 'spread': 15");
        assertEquals(1, hit[1], 1e-3, "on the top");
        assertEquals(1, hit[4], 1e-3, "facing up");
        assertTrue(hit[2] >= 3 && hit[2] < 3 + 2 * Math.tan(Math.toRadians(15)), "over the edge: " + hit[2]);
        // The closest to straight down of the rays that reach it: barely over the edge.
        double off = Math.toDegrees(Math.atan2(Math.hypot(hit[0] - 0.5, hit[2] - 2.7), 3 - hit[1]));
        assertTrue(off < 15 * Math.sqrt(12 / 23.0), "one of the inner half of the rays: " + off);
    }

    @Test
    void ofTheRaysThatHitItKeepsTheClosestToTheDirection() throws Exception
    {
        // Down past the edge with rays all round: the ones over the block hit its top, those
        // short of it the floor; the closest to straight down is over the floor.
        double[] hit = cast("'from': {'x': 0.5, 'y': 3, 'z': 2.9}, 'direction': {'y': -1}, 'distance': 5, 'count': 24, 'spread': 15");
        assertCast(new double[] {0.5, 0, 2.9, 0, 1, 0}, hit);
    }

    @Test
    void onlyHitsOnTheSurfaceItAsksForCount() throws Exception
    {
        // Level at the block's face: every ray that hits, hits a face, not a top.
        String level = "'from': {'x': 0.5, 'y': 0.9, 'z': 1}, 'direction': {'z': 1}, 'distance': 4, 'count': 16, 'spread': 10";
        assertCast(new double[] {0.5, 0.9, 3, 0, 0, -1}, cast(level));
        assertCast(new double[] {0.5, 0.9, 5, 0, 0, 0}, cast(level + ", 'surface': {'y': 1}"));
    }

    @Test
    void withDebugItRecordsEveryRayByWhatItHit() throws Exception
    {
        String fields = "'from': {'x': 0.5, 'y': 3, 'z': 2.9}, 'direction': {'y': -1}, 'distance': 5, 'count': 24, 'spread': 15";
        RayGizmos.beginFrame();
        cast(fields);
        assertEquals(0, RayGizmos.count(), "nothing without debug");

        cast(fields + ", 'debug': true");
        // Every frame casts all 24; the last frame's: the middle (on the floor) kept, the rest hits.
        int frame = RayGizmos.count() - 24;
        assertTrue(frame >= 0 && frame % 24 == 0, "whole frames: " + RayGizmos.count());
        assertEquals(RayGizmos.Kind.KEPT, RayGizmos.kind(frame));
        assertEquals(1, RayGizmos.get(frame, 7), 1e-9, "the kept hit's normal");
        for (int i = 1; i < 24; i++)
        {
            assertEquals(RayGizmos.Kind.HIT, RayGizmos.kind(frame + i), "ray " + i);
        }

        RayGizmos.beginFrame();
        cast(fields + ", 'debug': true, 'surface': {'x': 1}");
        for (int i = 0; i < RayGizmos.count(); i++)
        {
            assertEquals(RayGizmos.Kind.WRONG_SURFACE, RayGizmos.kind(i), "ray " + i);
        }

        RayGizmos.beginFrame();
        cast(fields.replace("'distance': 5", "'distance': 1") + ", 'debug': true");
        for (int i = 0; i < RayGizmos.count(); i++)
        {
            assertEquals(RayGizmos.Kind.MISS, RayGizmos.kind(i), "ray " + i);
            double length = Math.sqrt(Math.pow(RayGizmos.get(i, 3) - RayGizmos.get(i, 0), 2)
                    + Math.pow(RayGizmos.get(i, 4) - RayGizmos.get(i, 1), 2) + Math.pow(RayGizmos.get(i, 5) - RayGizmos.get(i, 2), 2));
            assertEquals(1, length, 1e-9, "drawn its whole length");
        }
        RayGizmos.beginFrame();
    }

    @Test
    void itsMistakesAreInItsOwnWords()
    {
        expectError("'from': {'x': 0, 'y': 0, 'z': 0}, 'direction': {'y': -1}, 'distance': 1, 'count': 0", "'count' is how many rays it casts, 1 to 64.");
        expectError("'from': {'x': 0, 'y': 0, 'z': 0}, 'direction': {'y': -1}, 'distance': 1, 'spread': 90", "'spread' is the angle to the edge of the cone");
        expectError("'direction': {'y': -1}, 'distance': 1", "'core:raycast_array' needs 'from'");
    }

    private static void expectError(String fields, String message)
    {
        String json = "{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'main', 'nodes': {'main': {'core:pose': {'pose': [{'core:raycast_array': {" + fields + "}}]}}}}]}";
        AnimatorTemplate template = KumoSerializer.INSTANCE.gson.fromJson(json.replace('\'', '"'), AnimatorTemplate.class);
        MalformedKumoTemplateException e = assertThrows(MalformedKumoTemplateException.class,
                () -> new KumoSession(new Scenario(EntityKind.IRON_GOLEM, "raycast_array", Scenarios.FPS, 1, (tick, in) -> {}), template));
        assertTrue(e.getMessage().contains(message), e.getMessage());
    }

}
