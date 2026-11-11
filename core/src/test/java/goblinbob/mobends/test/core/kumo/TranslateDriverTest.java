package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TranslateDriverTest
{

    private static KumoAnimatorState animator(String pose) throws MalformedKumoTemplateException
    {
        return TestSubject.instance("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {'pose': [" + pose + "]}}}}]}");
    }

    private static void assertVector(float x, float y, float z, Vec3f actual)
    {
        assertEquals("x", x, actual.x, 1e-4F);
        assertEquals("y", y, actual.y, 1e-4F);
        assertEquals("z", z, actual.z, 1e-4F);
    }

    @Test
    public void movesAPartsOffset() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'arm', 'x': 1, 'y': 2, 'z': 3}, '@space': 'override'}");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertVector(1, 2, 3, subject.offsets.get("arm"));
    }

    @Test
    public void movesAnEntityLevelVector() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'root', 'x': 1, 'y': 2, 'z': 3}, '@space': 'override'}");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertVector(1, 2, 3, subject.root.position.end);
    }

    @Test
    public void anAxisLeftOutKeepsWhatThePartHasSoFar() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'arm', 'x': 1, 'y': 2, 'z': 3}, '@space': 'override'}, "
                + "{'core:translate': {'bone': 'arm', 'y': 5}, '@space': 'override'}");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertVector(1, 5, 3, subject.offsets.get("arm"));
    }

    @Test
    public void anAxisLeftOutKeepsWhatTheItemsBeforeGaveIt() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'arm', 'x': 1, 'y': 2, 'z': 3}, '@space': 'override'}, "
                + "{'core:translate': {'bone': 'arm', 'y': 5}, '@space': 'override'}");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertVector(1, 5, 3, subject.offsets.get("arm"));
    }

    @Test
    public void anAxisLeftOutWithNothingBeneathIsAtRest() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'arm', 'y': 5}, '@space': 'override'}");
        TestSubject subject = new TestSubject("arm");
        subject.offsets.get("arm").set(1, 0, 3);
        subject.smoothOffsets.get("arm").set(1, 0, 3);

        animator.update(subject, 1F);

        // Not the part's offset from before: the rest offset.
        assertVector(0, 5, 0, subject.offsets.get("arm"));
    }

    @Test
    public void anAxisThatIsntANumberIsAtRestNotWhereItWas() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'arm', 'x': {'div': [0, 0]}, 'y': 5}}");
        TestSubject subject = new TestSubject("arm");
        subject.offsets.get("arm").set(1, 0, 3);
        subject.smoothOffsets.get("arm").set(1, 0, 3);

        animator.update(subject, 1F);

        assertVector(0, 5, 0, subject.offsets.get("arm"));
    }

    @Test
    public void movesInPreSpaceByDefault() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'arm', 'x': 1, 'y': 2, 'z': 3}, '@space': 'override'}, "
                + "{'core:translate': {'bone': 'arm', 'x': 1, 'y': 1}}");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertVector(2, 3, 3, subject.offsets.get("arm"));
    }

    @Test
    public void aRelativeAxisLeftOutMovesByNothing() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'arm', 'x': 1, 'y': 2, 'z': 3}, '@space': 'override'}, "
                + "{'core:translate': {'bone': 'arm', 'y': 5}, '@space': 'pre'}");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertVector(1, 7, 3, subject.offsets.get("arm"));
    }

    @Test
    public void aSmoothedOffsetIsSmoothed() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'arm', 'x': 0, 'y': 10, 'z': 0}, '@space': 'override', '@smoothing': {'arm': 4}}");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);
        assertVector(0, 0, 0, subject.offsets.get("arm"));
        subject.updateOffsets(1F);
        float first = subject.offsets.get("arm").y;
        assertTrue("on its way: " + first, first > 0 && first < 10);

        for (int frame = 0; frame < 40; frame++)
        {
            animator.update(subject, 1F);
            subject.updateOffsets(1F);
        }
        assertVector(0, 10, 0, subject.offsets.get("arm"));
    }

    @Test
    public void aSmoothedOffsetSnaps() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'arm', 'x': 0, 'y': 10, 'z': 0}, '@space': 'override', '@smoothing': {'arm': 4}, '@snap': true}");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertVector(0, 10, 0, subject.offsets.get("arm"));
    }

    @Test
    public void aSmoothedOffsetSlides() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'arm', 'x': 0, 'y': 10, 'z': 0}, '@space': 'override', '@smoothing': {'arm': 4}, '@vectorModes': {'arm': 'slide'}}");
        TestSubject subject = new TestSubject("arm");

        // A linear tween of 4 ticks.
        for (int frame = 0; frame < 2; frame++)
        {
            animator.update(subject, 1F);
            subject.updateOffsets(1F);
        }
        assertVector(0, 5, 0, subject.offsets.get("arm"));
    }

    @Test
    public void aSmoothedTranslateLeavesTheRotationsSmoothingAlone() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'arm', 'x': 0, 'y': 10, 'z': 0}, '@space': 'override', '@smoothing': {'arm': 4}}, "
                + "{'core:rotate': {'bone': 'arm', 'axis': 'x', 'angle': 90}, '@space': 'override'}");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);
        subject.bones.get("arm").update(1F);

        // The rotation's rate is still 1 per tick: there in one tick.
        Quaternion shown = subject.bones.get("arm").getSmooth();
        Quaternion target = subject.target("arm");
        float dot = Math.abs(shown.x * target.x + shown.y * target.y + shown.z * target.z + shown.w * target.w);
        assertEquals(1F, dot, 1e-4F);
    }

    @Test
    public void anUnsmoothedOffsetIsAppliedAsIs() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:rotate': {'bone': 'arm', 'axis': 'x', 'angle': 90}, '@space': 'override', '@smoothing': {'arm': 10}}, "
                + "{'core:translate': {'bone': 'arm', 'x': 0, 'y': 10, 'z': 0}, '@space': 'override'}");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        // A rotation's smoothing doesn't reach the offset.
        assertVector(0, 10, 0, subject.offsets.get("arm"));
    }

    private static KumoAnimatorState animator(String layerSmoothing, String nodeSmoothing, String pose) throws MalformedKumoTemplateException
    {
        return TestSubject.instance("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a'" + (layerSmoothing == null ? "" : ", 'smoothing': " + layerSmoothing)
                + ", 'nodes': {'a': {'core:pose': {'pose': [" + pose + "]" + (nodeSmoothing == null ? "" : ", 'smoothing': " + nodeSmoothing) + "}}}}]}");
    }

    /** After one frame, the part is on its way to y = 10, not there yet. */
    private static void assertOnItsWay(KumoAnimatorState animator) throws MalformedKumoTemplateException
    {
        TestSubject subject = new TestSubject("arm");
        animator.update(subject, 1F);
        subject.updateOffsets(1F);
        float y = subject.offsets.get("arm").y;
        assertTrue("on its way: " + y, y > 0 && y < 10);

        for (int frame = 0; frame < 60; frame++)
        {
            animator.update(subject, 1F);
            subject.updateOffsets(1F);
        }
        assertVector(0, 10, 0, subject.offsets.get("arm"));
    }

    private static final String MOVE_ARM = "{'core:translate': {'bone': 'arm', 'x': 0, 'y': 10, 'z': 0}, '@space': 'override'}";

    @Test
    public void aNodesPositionSmoothingSmoothsAnOffset() throws MalformedKumoTemplateException
    {
        assertOnItsWay(animator(null, "{'arm': {'position': 4}}", MOVE_ARM));
    }

    @Test
    public void aNodesPerAxisPositionSmoothingSmoothsAnOffset() throws MalformedKumoTemplateException
    {
        assertOnItsWay(animator(null, "{'arm': {'position': [0, 4, 0]}}", MOVE_ARM));
    }

    @Test
    public void aNodesSmoothingSmoothsAnOffset() throws MalformedKumoTemplateException
    {
        assertOnItsWay(animator(null, "{'arm': 4}", MOVE_ARM));
    }

    @Test
    public void aNodesDefaultSmoothingSmoothsAnOffset() throws MalformedKumoTemplateException
    {
        assertOnItsWay(animator(null, "{'@default': 4}", MOVE_ARM));
    }

    @Test
    public void aLayersPositionSmoothingSmoothsAnOffset() throws MalformedKumoTemplateException
    {
        assertOnItsWay(animator("{'arm': {'position': 4}}", null, MOVE_ARM));
    }

    @Test
    public void aLayersDefaultSmoothingSmoothsAnOffset() throws MalformedKumoTemplateException
    {
        assertOnItsWay(animator("{'@default': 4}", null, MOVE_ARM));
    }

    @Test
    public void aNodesRotationSmoothingLeavesAnOffsetAlone() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator(null, "{'arm': {'rotation': 4}}", MOVE_ARM);
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertVector(0, 10, 0, subject.offsets.get("arm"));
    }

    @Test
    public void anItemsSmoothingWinsOverTheNodes() throws MalformedKumoTemplateException
    {
        // The item's slide of 4 ticks, not the node's 100.
        KumoAnimatorState animator = animator("{'arm': {'position': 100}}", "{'arm': {'position': 100}}",
                "{'core:translate': {'bone': 'arm', 'x': 0, 'y': 10, 'z': 0}, '@space': 'override', '@smoothing': {'arm': 4}, '@vectorModes': {'arm': 'slide'}}");
        TestSubject subject = new TestSubject("arm");

        for (int frame = 0; frame < 2; frame++)
        {
            animator.update(subject, 1F);
            subject.updateOffsets(1F);
        }
        assertVector(0, 5, 0, subject.offsets.get("arm"));
    }

}
