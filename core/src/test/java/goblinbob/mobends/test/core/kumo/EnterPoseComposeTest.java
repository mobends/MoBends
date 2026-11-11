package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** A node's {@code enterPose} builds on the layers below, as its {@code pose} does. */
public class EnterPoseComposeTest
{

    /** A lower layer that poses {@code lower}, and one above whose node enters with {@code enter} and poses {@code pose}. */
    private static KumoAnimatorState onTopOf(String lower, String enter, String pose) throws MalformedKumoTemplateException
    {
        return TestSubject.instance(("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {'pose': [" + lower + "]}}}}, "
                + "{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {'enterPose': [" + enter + "], 'pose': [" + pose + "]}}}}]}").replace('\'', '"'));
    }

    private static String translate(String bone, String axes, String space)
    {
        return "{'core:translate': {'bone': '" + bone + "', " + axes + "}, '@space': '" + space + "'}";
    }

    private static String scale(String axes, String space)
    {
        return "{'core:scale': {'bone': 'arm', " + axes + "}, '@space': '" + space + "'}";
    }

    private static String rotate(float angle, String space)
    {
        return "{'core:rotate': {'bone': 'arm', 'axis': 'x', 'angle': " + angle + "}, '@space': '" + space + "'}";
    }

    private static void assertVector(float x, float y, float z, Vec3f actual)
    {
        assertEquals("x", x, actual.x, 1e-4F);
        assertEquals("y", y, actual.y, 1e-4F);
        assertEquals("z", z, actual.z, 1e-4F);
    }

    private static void assertRotation(Quaternion expected, Quaternion actual)
    {
        float dot = expected.x * actual.x + expected.y * actual.y + expected.z * actual.z + expected.w * actual.w;
        assertEquals("the same rotation", 1F, Math.abs(dot), 1e-4F);
    }

    @Test
    public void aRelativeMoveThenAPartialOverrideKeepsBothOnTheRoot() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = onTopOf(translate("root", "'y': 20", "override"),
                translate("root", "'y': 5", "pre") + ", " + translate("root", "'x': 10", "override"), "");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        // (10, 25, 0), not (10, 20, 0): the override keeps the y the relative move gave it.
        assertVector(10, 25, 0, subject.root.position.end);
    }

    @Test
    public void aRelativeMoveThenAPartialOverrideKeepsBothOnAPart() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = onTopOf(translate("arm", "'y': 20", "override"),
                translate("arm", "'y': 5", "pre") + ", " + translate("arm", "'x': 10", "override"), "");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertVector(10, 25, 0, subject.offsets.get("arm"));
    }

    @Test
    public void aRelativeScaleThenAPartialOverrideKeepsBoth() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = onTopOf(scale("'y': 1.2", "override"),
                scale("'y': 0.5", "pre") + ", " + scale("'x': 2", "override"), "");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertVector(2, 0.6F, 1, subject.scales.get("arm"));
    }

    @Test
    public void aRelativeRotationTurnsFromTheLayersBelow() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = onTopOf(rotate(30, "override"), rotate(10, "pre"), "");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertRotation(TestSubject.axisAngle(1, 0, 0, 40), subject.target("arm"));
    }

    @Test
    public void aWeightedClipBlendsFromTheLayersBelow() throws MalformedKumoTemplateException
    {
        String clip = "{'bones': {'arm': {'keyframes': [{'rotation': [0.38268343, 0, 0, 0.9238795]}]}}}";
        java.util.Map<String, String> clips = new java.util.HashMap<>();
        clips.put("turn", clip.replace('\'', '"'));
        KumoAnimatorState animator = TestSubject.instance(("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {'pose': ["
                + rotate(30, "override") + "]}}}}, "
                + "{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {'enterPose': [{'core:clip': {'animationKey': 'turn', 'weight': 0.5}, '@space': 'override'}]}}}}]}")
                .replace('\'', '"'), clips);
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        // Halfway from the layer below's 30 degrees to the clip's 45, not from rest.
        assertRotation(TestSubject.axisAngle(1, 0, 0, 37.5F), subject.target("arm"));
    }

    @Test
    public void thePoseMovesFromWhereTheComposedEntryPutTheBone() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = onTopOf(translate("root", "'y': 20", "override"),
                translate("root", "'y': 5", "pre") + ", " + translate("root", "'x': 10", "override"),
                "{'core:translate': {'bone': 'root', 'x': 0, 'y': 0, 'z': 0}, '@space': 'override', '@smoothing': {'root': 4}, '@vectorModes': {'root': 'slide'}}");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertVector(10, 25, 0, subject.root.position.start);
        assertVector(0, 0, 0, subject.root.position.end);
    }

    @Test
    public void whatAnEntryDriverWritesIsThereForTheEntryFrame() throws MalformedKumoTemplateException
    {
        // The entry's driver writes node.ground; the update list and the pose read it on the same frame.
        KumoAnimatorState animator = TestSubject.instance(("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', 'nodes': {'a': {"
                + "'core:pose': {'enterPose': [{'core:accumulate': {'inout': 'node.ground', 'rate': 1, 'max': 10}}], 'pose': ["
                + "{'core:translate': {'bone': 'root', 'y': {'mul': ['node.ground', 10]}}, '@space': 'override'}, "
                + "{'core:translate': {'bone': 'arm', 'y': {'mul': ['node.seen', 10]}}, '@space': 'override'}]}, "
                + "'@define': {'ground': {'state': 0}, 'seen': {'state': 0}}, "
                + "'@on': {'update': [{'set': ['node.seen', 'node.ground']}]}}}}]}").replace('\'', '"'));
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertEquals("the pose reads it", 10F, subject.root.position.end.y, 1e-4F);
        assertEquals("the update list reads it", 10F, subject.offsets.get("arm").y, 1e-4F);
    }

}
