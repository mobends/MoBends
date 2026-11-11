package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * {@code core:two_bone_ik} and {@code core:three_bone_ik} on a chain hanging from a scaled body,
 * checked by posing the bones as the renderer draws them (each part moved, turned, then scaled):
 * the end lands on the target in the world, and {@code stretch} is how many blocks it misses by.
 */
public class ChainIkScaleTest
{

    private static final double TOLERANCE = 1e-3;
    /** Where vanilla draws the model's origin above the entity's, in blocks; model units per block. */
    private static final double ORIGIN_Y = 1.501, UNITS = 16;

    /** The body's pivot, in the model; the chain's first pivot, in the body. */
    private static final double[] BODY = {0, 2, 0}, SHOULDER = {2, 1, 0};

    private static final double[][] TWO = {{0, 6, 0}, {0, 6, 0}};
    private static final double[][] THREE = {{0, 5, 0}, {0, 5, 0}, {0, 4, 0}};

    @Test
    public void aTwoBoneChainReachesThroughItsParentsScale() throws MalformedKumoTemplateException
    {
        // Twice its size, the arm reaches 24 units; drawn at its own size it would stop at 12.
        final String body = "{'core:scale': {'bone': 'body', 'all': 2}, '@space': 'override'},";
        final double[] target = toWorld(new double[] {4, 22, -6});
        final Run run = run(body, TWO, target);
        assertAt(target, run.end);
        assertEquals(0, run.stretch, TOLERANCE);
    }

    @Test
    public void aThreeBoneChainReachesThroughAParentTurnedAndScaledUnevenly() throws MalformedKumoTemplateException
    {
        final String body = "{'core:rotate': {'bone': 'body', 'axis': 'x', 'angle': 30}, '@space': 'override'},"
                + "{'core:scale': {'bone': 'body', 'x': 1.5, 'y': 2, 'z': 0.75}, '@space': 'override'},";
        // Each 4 to 13 units from the shoulder in the arm's own frame, which reaches 14.
        for (double[] model : new double[][] {{8, 16, 2}, {2, 10, -3}, {4, 12, 4}})
        {
            final double[] target = toWorld(model);
            final Run run = run(body, THREE, target);
            assertAt(target, run.end);
            assertEquals(0, run.stretch, TOLERANCE);
        }
    }

    @Test
    public void itsStretchIsInBlocksInTheWorld() throws MalformedKumoTemplateException
    {
        // From the shoulder at [4, 4, 0], 30 down and 10 ahead: 31.62 units, of which the arms, twice
        // their size, reach 24 and 28.
        final String body = "{'core:scale': {'bone': 'body', 'all': 2}, '@space': 'override'},";
        final double[] target = toWorld(new double[] {4, 34, -10});
        for (double[][] chain : new double[][][] {TWO, THREE})
        {
            final Run run = run(body, chain, target);
            final double expected = (Math.hypot(30, 10) - 2 * length(chain)) / UNITS;
            assertEquals(expected, distance(run.end, target), TOLERANCE);
            assertEquals(expected, run.stretch, TOLERANCE);
        }
    }

    @Test
    public void itsStretchFollowsAnUnevenScaleAlongTheWayItMisses() throws MalformedKumoTemplateException
    {
        // Squashed to half its length along Y, the arm reaches 6 units down, so it misses a target
        // 16 straight below by 10 units, not the 4 it would in its own frame.
        final String body = "{'core:scale': {'bone': 'body', 'x': 3, 'y': 0.5, 'z': 3}, '@space': 'override'},";
        final double[] target = toWorld(new double[] {6, 2.5 + 16, 0});
        final Run run = run(body, TWO, target);
        final double missed = distance(run.end, target);
        assertTrue("it misses, by " + missed, missed > 0.5);
        assertEquals(missed, run.stretch, TOLERANCE);
    }

    private static final class Run
    {
        double[] end;
        double stretch;
    }

    /**
     * Poses {@code before}, then the chain, hanging from the body, onto {@code target} (blocks,
     * from the entity, which faces +Z at the origin), and poses it back into the world.
     */
    private static Run run(String before, double[][] chain, double[] target) throws MalformedKumoTemplateException
    {
        final String[] bones = chain.length == 2 ? new String[] {"upper", "lower"} : new String[] {"upper", "middle", "lower"};
        final StringBuilder segments = new StringBuilder("{'bone': 'upper', 'pivot': " + vector(SHOULDER) + "}");
        for (int i = 1; i < chain.length; i++)
        {
            segments.append(", {'bone': '").append(bones[i]).append("', 'pivot': ").append(vector(chain[i - 1])).append(", 'bend': 1}");
        }
        final String ik = "{'core:" + (chain.length == 2 ? "two" : "three") + "_bone_ik': {"
                + "'segments': [" + segments + "], 'end': " + vector(chain[chain.length - 1]) + ","
                + "'target': {'x': " + target[0] + ", 'y': " + target[1] + ", 'z': " + target[2] + "},"
                + "'x': 0, 'y': 0, 'z': 0, 'yaw': 0, 'centerPivot': {'x': 0, 'y': 0, 'z': 0},"
                + "'parents': [{'bone': 'body', 'pivot': " + vector(BODY) + "}],"
                + "'out': {'stretch': 'node.stretch'}}},"
                // The stretch, read off a part's scale.
                + "{'core:scale': {'bone': 'gauge', 'all': 'node.stretch'}, '@space': 'override'}";
        final KumoAnimatorState animator = TestSubject.instance(("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', 'nodes': {'a': {"
                + "'@define': {'stretch': {'state': 0}}, 'core:pose': {'pose': [" + before + ik + "]}}}}]}").replace('\'', '"'));
        final TestSubject subject = new TestSubject("center", "body", "upper", "middle", "lower", "gauge");
        animator.update(subject, 1F);

        // The renderer: each part moved to its pivot, turned, then scaled, carrying what comes after.
        double[] at = chain[chain.length - 1];
        for (int i = chain.length - 1; i >= 0; i--)
        {
            at = add(i == 0 ? SHOULDER : chain[i - 1], rotate(subject.target(bones[i]), at));
        }
        final Vec3f scale = subject.scales.get("body");
        at = add(BODY, rotate(subject.target("body"), new double[] {at[0] * scale.x, at[1] * scale.y, at[2] * scale.z}));

        final Run run = new Run();
        run.end = toWorld(at);
        run.stretch = subject.scales.get("gauge").x;
        return run;
    }

    /** A point in the model (units, +Y down, -Z forward) in the world (blocks from the entity, +Y up, +Z ahead). */
    private static double[] toWorld(double[] model)
    {
        return new double[] {model[0] / UNITS, ORIGIN_Y - model[1] / UNITS, -model[2] / UNITS};
    }

    private static String vector(double[] v)
    {
        return "[" + v[0] + ", " + v[1] + ", " + v[2] + "]";
    }

    private static void assertAt(double[] expected, double[] actual)
    {
        assertEquals(String.format("at [%.4f, %.4f, %.4f], not [%.4f, %.4f, %.4f]", actual[0], actual[1], actual[2], expected[0], expected[1], expected[2]),
                     0, distance(expected, actual), TOLERANCE);
    }

    private static double[] rotate(Quaternion q, double[] v)
    {
        // v + w·t + q×t, with t = 2·(q×v).
        final double tx = 2 * (q.y * v[2] - q.z * v[1]);
        final double ty = 2 * (q.z * v[0] - q.x * v[2]);
        final double tz = 2 * (q.x * v[1] - q.y * v[0]);
        return new double[] {
                v[0] + q.w * tx + q.y * tz - q.z * ty,
                v[1] + q.w * ty + q.z * tx - q.x * tz,
                v[2] + q.w * tz + q.x * ty - q.y * tx};
    }

    /** How far a chain reaches, straight. */
    private static double length(double[][] chain)
    {
        double length = 0;
        for (double[] segment : chain)
        {
            length += Math.sqrt(segment[0] * segment[0] + segment[1] * segment[1] + segment[2] * segment[2]);
        }
        return length;
    }

    private static double[] add(double[] a, double[] b)
    {
        return new double[] {a[0] + b[0], a[1] + b[1], a[2] + b[2]};
    }

    private static double distance(double[] a, double[] b)
    {
        return Math.sqrt((a[0] - b[0]) * (a[0] - b[0]) + (a[1] - b[1]) * (a[1] - b[1]) + (a[2] - b[2]) * (a[2] - b[2]));
    }

}
