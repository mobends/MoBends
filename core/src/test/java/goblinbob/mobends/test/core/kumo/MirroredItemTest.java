package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.math.Quaternion;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/** An item with {@code @mirror} reads the layers below as mirrored as the pose it writes. */
public class MirroredItemTest
{

    private static final String HEAD_BACK = "{'bones': {'head': {'keyframes': [{'position': [-10, 0, 0]}, {'position': [-10, 0, 0]}]}}}";
    private static final String STILL = "{'bones': {'%s': {'keyframes': [{'rotation': [0, 0, 0, 1]}, {'rotation': [0, 0, 0, 1]}]}}}";

    /** A lower layer that poses {@code lower}, and one above, with a mirror rule, that plays {@code clip} at half weight. */
    private static KumoAnimatorState animator(String lower, String clip, boolean mirrored) throws MalformedKumoTemplateException
    {
        Map<String, String> clips = new HashMap<>();
        clips.put("clip", clip.replace('\'', '"'));
        return TestSubject.instance(("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {'pose': [" + lower + "]}}}}, "
                + "{'defaultOnEntry': 'b', 'mirror': {'@when': 'LEFT_HANDED', 'pairs': [['leftArm', 'rightArm']]}, "
                + "'nodes': {'b': {'core:pose': {'pose': [{'core:clip': {'animationKey': 'clip', 'weight': 0.5}, "
                + "'@space': 'override'" + (mirrored ? ", '@mirror': true" : "") + "}]}}}}]}").replace('\'', '"'), clips);
    }

    private static TestSubject subject()
    {
        TestSubject subject = new TestSubject("head", "leftArm", "rightArm");
        subject.states.put("LEFT_HANDED", true);
        return subject;
    }

    private static void assertRotation(Quaternion expected, Quaternion actual)
    {
        float dot = Math.abs(expected.x * actual.x + expected.y * actual.y + expected.z * actual.z + expected.w * actual.w);
        assertEquals("rotation " + actual.x + ", " + actual.y + ", " + actual.z + ", " + actual.w, 1F, dot, 1e-4F);
    }

    @Test
    public void aWeightedClipBlendsAnOffsetFromTheLayersBelow() throws MalformedKumoTemplateException
    {
        String lower = "{'core:translate': {'bone': 'head', 'x': 20}, '@space': 'override'}";
        // The clip's position -10 is an offset of 10: halfway from 20 is 15, and mirrored, from -20 to 10, -5, mirrored back.
        TestSubject plain = subject();
        animator(lower, HEAD_BACK, false).update(plain, 1F);
        assertEquals(15F, plain.offsets.get("head").x, 1e-4F);

        TestSubject mirrored = subject();
        animator(lower, HEAD_BACK, true).update(mirrored, 1F);
        assertEquals(5F, mirrored.offsets.get("head").x, 1e-4F);
    }

    @Test
    public void aWeightedClipBlendsARotationFromTheLayersBelow() throws MalformedKumoTemplateException
    {
        TestSubject subject = subject();
        animator("{'core:rotate': {'bone': 'head', 'axis': 'y', 'angle': 30}, '@space': 'override'}", String.format(STILL, "head"), true)
                .update(subject, 1F);

        // Mirrored, from -30 degrees halfway to the clip's rest, mirrored back.
        assertRotation(TestSubject.axisAngle(0, 1, 0, 15), subject.target("head"));
    }

    @Test
    public void aWeightedClipOnAPairedBoneBlendsFromItsPairBelow() throws MalformedKumoTemplateException
    {
        TestSubject subject = subject();
        animator("{'core:rotate': {'bone': 'leftArm', 'axis': 'x', 'angle': 40}, '@space': 'override'}", String.format(STILL, "rightArm"), true)
                .update(subject, 1F);

        // The clip's right arm lands on the left one, halfway from where the layer below has it.
        assertRotation(TestSubject.axisAngle(1, 0, 0, 20), subject.target("leftArm"));
        assertRotation(TestSubject.axisAngle(1, 0, 0, 0), subject.target("rightArm"));
    }

}
