package goblinbob.mobends.lab;

import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.lab.scenarios.Scenarios;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.Scenario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@code core:two_bone_ik} on the golem's right arm (misc/kumo-format.md, "Drivers"): the hand put
 * on a point in the world, through whatever the items before it did to the whole model.
 */
class ChainIkDriverTest
{

    /** The golem's right arm, as its animator declares it. */
    private static final String ARM = "'segments': [{'bone': 'rightArm', 'pivot': [0, -7, 0]}, "
            + "{'bone': 'rightForeArm', 'pivot': [0, 12.5, 3], 'bend': -1%s}], 'end': [-11, 15, -3]";

    /**
     * Runs a golem at (0.5, 0, 0.5), its body at {@code yaw}, for a few ticks with {@code before}
     * (items, each followed by a comma) then the arm's IK of the given fields; its stretch, times
     * 10, is the left arm's X angle.
     */
    private static KumoSession run(float yaw, String before, String bendDirection, String fields) throws Exception
    {
        String json = "{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'main', 'nodes': {'main': {"
                + "'@define': {'stretch': {'state': 0}},"
                + "'core:pose': {'pose': [" + before
                + "{'core:two_bone_ik': {" + String.format(ARM, bendDirection) + ", " + fields + ", 'out': {'stretch': 'node.stretch'}}},"
                + "{'core:axis_rotate': {'bone': 'leftArm', 'axis': 'x', 'angle': {'mul': ['node.stretch', 10]}}, '@space': 'override'}]}}}}]}";
        AnimatorTemplate template = KumoSerializer.INSTANCE.gson.fromJson(json.replace('\'', '"'), AnimatorTemplate.class);
        KumoSession session = new KumoSession(new Scenario(EntityKind.IRON_GOLEM, "ik", Scenarios.FPS, 5, (tick, in) -> in.bodyYaw = yaw), template);
        session.run();
        return session;
    }

    private static double[] hand(KumoSession session)
    {
        return GolemHands.hand((DefinedEntityData<?>) session.data, true);
    }

    private static double stretch(KumoSession session)
    {
        Quaternion q = ((DefinedEntityData<?>) session.data).getPart("leftArm").rotation.getSmooth();
        return Math.toDegrees(2 * Math.atan2(q.x, q.w)) / 10;
    }

    private static String target(double x, double y, double z)
    {
        return String.format("'target': {'x': %s, 'y': %s, 'z': %s}", x, y, z);
    }

    private static void assertAt(double[] expected, double[] actual)
    {
        assertEquals(0, GolemHands.distance(expected, actual), 1e-3, String.format("at %.4f %.4f %.4f", actual[0], actual[1], actual[2]));
    }

    @Test
    void theHandReachesAPointInTheWorld() throws Exception
    {
        // In front of a golem facing +Z, to its right (-X), at its knees.
        double[] point = {0.1, 0.6, 1.4};
        KumoSession session = run(0, "", "", target(point[0], point[1], point[2]));
        assertAt(point, hand(session));
        assertEquals(0, stretch(session), 1e-3);
    }

    @Test
    void theTargetIsInTheWorldWhicheverWayTheBodyFaces() throws Exception
    {
        // Facing west (yaw 90): ahead is -X, and its right is -Z.
        double[] point = {-0.4, 0.8, 0.1};
        assertAt(point, hand(run(90, "", "", target(point[0], point[1], point[2]))));
    }

    @Test
    void theHandStaysWhereverTheModelIsDrawn() throws Exception
    {
        // The whole model held half a block back and leaning forward: the hand still on the point.
        String before = "{'core:anchor': {'point': {'z': 0}}},"
                + "{'core:axis_rotate': {'bone': 'centerRotation', 'axis': 'x', 'angle': 20}, '@space': 'override', '@snap': true},";
        double[] point = {0.0, 0.9, 1.1};
        assertAt(point, hand(run(30, before, "", target(point[0], point[1], point[2]))));
    }

    @Test
    void theElbowPointsWhereItIsTold() throws Exception
    {
        // A hand at the shoulder's height, ahead: the elbow pointing up, then down.
        String field = target(0.0, 1.6, 1.6);
        double up = elbowY(run(0, "", ", 'bendDirection': {'y': 1}", field));
        double down = elbowY(run(0, "", ", 'bendDirection': {'y': -1}", field));
        assertTrue(up > down + 0.3, "up " + up + ", down " + down);
    }

    private static double elbowY(KumoSession session)
    {
        DefinedEntityData<?> data = (DefinedEntityData<?>) session.data;
        Quaternion arm = data.getPart("rightArm").rotation.getSmooth();
        double[] elbow = GolemHands.add(new double[] {0, -7, 0}, GolemHands.rotate(arm, -11, 12.5, 0));
        return GolemHands.toWorld(data, elbow)[1];
    }

    @Test
    void outOfReachItPointsAtTheTargetAndSaysHowFar() throws Exception
    {
        // Three blocks ahead at the shoulder's height: the arm is 1.72 blocks long.
        KumoSession session = run(0, "", "", target(0.5, 1.94, 3.5));
        double[] hand = hand(session);
        assertTrue(hand[2] > 1.5, "reaching forward");
        assertEquals(0, hand[1] - 1.94, 0.15);
        assertEquals(3.5 - 0.5 - Math.hypot(hand[2] - 0.5, hand[1] - 1.94), stretch(session), 0.1);
    }

    @Test
    void theWeightBlendsItIn() throws Exception
    {
        double[] point = {0.1, 0.6, 1.4};
        double[] full = hand(run(0, "", "", target(point[0], point[1], point[2])));
        double[] none = hand(run(0, "", "", target(point[0], point[1], point[2]) + ", 'weight': 0"));
        assertAt(point, full);
        assertTrue(GolemHands.distance(point, none) > 0.3, "at weight 0 the arm hangs");
    }

    @Test
    void itsMistakesAreInItsOwnWords()
    {
        expectError("{'core:two_bone_ik': {'segments': [{'bone': 'rightArm', 'pivot': [0, -7, 0]}], 'end': [0, 1, 0], 'target': {'x': 0, 'y': 0, 'z': 0}}}",
                "'core:two_bone_ik' needs 2 'segments'");
        expectError("{'core:three_bone_ik': {" + String.format(ARM, "") + ", 'target': {'x': 0, 'y': 0, 'z': 0}}}",
                "'core:three_bone_ik' needs 3 'segments'");
        expectError("{'core:two_bone_ik': {" + String.format(ARM, "") + "}}", "'core:two_bone_ik' needs 'target'");
    }

    private static void expectError(String item, String message)
    {
        String json = "{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'main', 'nodes': {'main': {'core:pose': {'pose': [" + item + "]}}}}]}";
        AnimatorTemplate template = KumoSerializer.INSTANCE.gson.fromJson(json.replace('\'', '"'), AnimatorTemplate.class);
        MalformedKumoTemplateException e = assertThrows(MalformedKumoTemplateException.class,
                () -> new KumoSession(new Scenario(EntityKind.IRON_GOLEM, "ik", Scenarios.FPS, 1, (tick, in) -> {}), template));
        assertTrue(e.getMessage().contains(message), e.getMessage());
    }

}
