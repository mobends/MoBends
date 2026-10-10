package goblinbob.mobends.lab;

import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.lab.scenarios.Scenarios;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.Scenario;
import goblinbob.mobends.lab.trace.FramePose;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** {@code core:anchor}: the entity drawn at a point in the world (misc/kumo-format.md, "Drivers"). */
class AnchorDriverTest
{

    /**
     * Runs a golem (starting at 0.5, 0, 0.5, or where {@code setup} puts it) for {@code ticks} with an
     * anchor of the given fields, its position difference written, times 10, to the right arm's X
     * angle. {@code define} adds node definitions ({@code "'name': {...}, "}).
     */
    private static List<FramePose> run(int ticks, String define, String anchor, Scenario.InputScript script,
                                       java.util.function.Consumer<goblinbob.mobends.core.data.LivingEntityData<?>> setup) throws Exception
    {
        String json = "{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'main', 'nodes': {'main': {"
                + "'@define': {" + define + "'difference': {'state': 0}},"
                + "'core:pose': {'pose': [{'core:anchor': {" + anchor + ", 'out': {'positionDifference': 'node.difference'}}},"
                + "{'core:axis_rotate': {'bone': 'rightArm', 'axis': 'x', 'angle': {'mul': ['node.difference', 10]}}, '@space': 'override'}]}}}}]}";
        AnimatorTemplate template = KumoSerializer.INSTANCE.gson.fromJson(json.replace('\'', '"'), AnimatorTemplate.class);
        return new KumoSession(new Scenario(EntityKind.IRON_GOLEM, "anchor", Scenarios.FPS, ticks, script, setup), template).run().frames;
    }

    private static List<FramePose> run(int ticks, String anchor, Scenario.InputScript script) throws Exception
    {
        return run(ticks, "", anchor, script, null);
    }

    private static float[] root(FramePose frame)
    {
        return frame.vectors.get("globalOffset").v;
    }

    private static float difference(FramePose frame)
    {
        float[] q = frame.bones.get("rightArm").rt;
        return (float) Math.toDegrees(2 * Math.atan2(q[0], q[3])) / 10;
    }

    @Test
    void theEntityIsDrawnAtThePoint() throws Exception
    {
        // The middle of the top of the block ahead (+Z): a block forward, a block up.
        List<FramePose> frames = run(5, "'point': {'x': 0.5, 'y': 1, 'z': 1.5}", (tick, in) -> {});
        FramePose last = frames.get(frames.size() - 1);
        assertEquals(0, root(last)[0], 1e-3);
        assertEquals(16, root(last)[1], 1e-3);
        assertEquals(16, root(last)[2], 1e-3, "+Z is ahead of a body at yaw 0");
        assertEquals(Math.sqrt(2), difference(last), 1e-3);
    }

    @Test
    void anAxisLeftOutFollowsTheEntity() throws Exception
    {
        List<FramePose> frames = run(5, "'point': {'z': 1.5}", (tick, in) -> {});
        FramePose last = frames.get(frames.size() - 1);
        assertEquals(0, root(last)[0], 1e-3);
        assertEquals(0, root(last)[1], 1e-3);
        assertEquals(16, root(last)[2], 1e-3);
    }

    @Test
    void theOffsetIsInTheBodysFrame() throws Exception
    {
        // Facing west (yaw 90), the block to the south is on the body's left (+X).
        List<FramePose> frames = run(5, "'point': {'x': 0.5, 'z': 1.5}", (tick, in) -> in.bodyYaw = 90);
        FramePose last = frames.get(frames.size() - 1);
        assertEquals(16, root(last)[0], 1e-2);
        assertEquals(0, root(last)[2], 1e-2);
    }

    @Test
    void aPointTakenWhenTheNodeStartsStaysThere() throws Exception
    {
        // Walking ahead, the point (node constants, the block the feet were in) stays where it was:
        // the entity is drawn back at the start.
        List<FramePose> frames = run(20, "'blockX': {'constant': {'floor': ['entityWorldX']}}, 'blockZ': {'constant': {'floor': ['entityWorldZ']}}, ",
                "'point': {'x': {'add': ['node.blockX', 0.5]}, 'z': {'add': ['node.blockZ', 0.5]}}", (tick, in) -> in.forwardSpeed = 0.1, null);
        FramePose last = frames.get(frames.size() - 1);
        float walked = -root(last)[2] / 16;
        assertEquals(walked, difference(last), 1e-3);
        assertEquals(2.0, walked, 0.2);
    }

    @Test
    void thePointKeepsItsPrecisionFarFromTheOrigin() throws Exception
    {
        // Ten million blocks out a float steps by a whole block: in floats, the point would be 0.3
        // blocks off the entity (4.8 model units), in doubles it is where the entity is.
        List<FramePose> frames = run(5, "'blockX': {'constant': {'floor': ['entityWorldX']}}, ",
                "'point': {'x': {'add': ['node.blockX', 0.3]}}", (tick, in) -> {},
                data -> {
                    net.minecraft.entity.EntityLivingBase entity = (net.minecraft.entity.EntityLivingBase) data.getEntity();
                    entity.setLocationAndAngles(10_000_000.3D, entity.posY, 0.5D, 0.0F, 0.0F);
                });
        FramePose last = frames.get(frames.size() - 1);
        assertEquals(0, root(last)[0], 1e-3);
        assertEquals(0, difference(last), 1e-3);
    }

    @Test
    void theWeightEasesBetweenTheEntityAndThePoint() throws Exception
    {
        List<FramePose> frames = run(5, "'point': {'x': 0.5, 'z': 1.5}, 'weight': 0.25", (tick, in) -> {});
        assertEquals(4, root(frames.get(frames.size() - 1))[2], 1e-3);
    }

}
