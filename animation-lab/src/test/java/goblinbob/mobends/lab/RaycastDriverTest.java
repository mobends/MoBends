package goblinbob.mobends.lab;

import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
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

/** {@code core:raycast}: where a ray through the blocks hits, and which way the surface faces. */
class RaycastDriverTest
{

    static
    {
        // The rays are the mod's drivers, which the lab registers: before the animators are parsed.
        LabBootstrap.ensure();
    }

    private static final String[] OUTPUTS = {"hitX", "hitY", "hitZ", "normalX", "normalY", "normalZ"};
    private static final String[] BONES = {"rightArm", "leftArm", "rightLeg", "leftLeg", "head", "body"};

    /**
     * What a golem at (0.5, 0, 0.5), with a stone block at (0, 0, 3), casts with the given fields:
     * each output, times 10 (the hit made relative to (0.5, 0, 0.5)), as a bone's X angle.
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
            pose.append(", {'core:rotate': {'bone': '").append(BONES[i]).append("', 'axis': 'x', 'angle': {'mul': [").append(value)
                    .append(", 10]}}, '@space': 'override', '@snap': true}");
        }
        String json = "{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'main', 'nodes': {'main': {'@define': {" + define + "'unused': {'state': 0}},"
                + "'core:pose': {'pose': [{'core:raycast': {" + fields + ", 'out': {" + out + "}}}" + pose + "]}}}}]}";
        AnimatorTemplate template = KumoSerializer.INSTANCE.gson.fromJson(json.replace('\'', '"'), AnimatorTemplate.class);
        List<FramePose> frames = new KumoSession(new Scenario(EntityKind.IRON_GOLEM, "raycast", Scenarios.FPS, 2, (tick, in) -> {},
                data -> LabWorlds.placeStone(data.getEntity().world, 0, 0, 3, 0, 0, 3)), template).run().frames;
        FramePose last = frames.get(frames.size() - 1);
        double[] values = new double[OUTPUTS.length];
        for (int i = 0; i < OUTPUTS.length; i++)
        {
            float[] q = last.bones.get(BONES[i]).rt;
            values[i] = Math.toDegrees(2 * Math.atan2(q[0], q[3])) / 10;
        }
        // Back from relative to the golem.
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
    void aRayDownHitsTheTopOfABlock() throws Exception
    {
        assertCast(new double[] {0.5, 1, 3.5, 0, 1, 0}, cast("'from': {'x': 0.5, 'y': 3, 'z': 3.5}, 'direction': {'y': -1}, 'distance': 5"));
    }

    @Test
    void aRayAheadHitsTheFaceTowardsIt() throws Exception
    {
        // Along +Z at half a block up, of any length: the north face.
        assertCast(new double[] {0.5, 0.5, 3, 0, 0, -1}, cast("'from': {'x': 0.5, 'y': 0.5, 'z': 0.5}, 'direction': {'z': 4}, 'distance': 5"));
    }

    @Test
    void aRayThatHitsNothingEndsWhereItGoesTo() throws Exception
    {
        // Up from beside the block, 2 blocks: nothing there.
        assertCast(new double[] {1.5, 2.5, 0.5, 0, 0, 0}, cast("'from': {'x': 1.5, 'y': 0.5, 'z': 0.5}, 'direction': {'y': 1}, 'distance': 2"));
    }

    @Test
    void aRayTooShortToGetThereHitsNothing() throws Exception
    {
        assertCast(new double[] {0.5, 2, 3.5, 0, 0, 0}, cast("'from': {'x': 0.5, 'y': 3, 'z': 3.5}, 'direction': {'y': -1}, 'distance': 1"));
    }

}
