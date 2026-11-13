package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.bind.IVanillaPose;
import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class VanillaPoseNodeTest
{

    /** A vanilla pose that has what a test puts in it, and counts the reads. */
    private static class FakeVanillaPose implements IVanillaPose
    {
        final Map<String, Quaternion> rotations = new HashMap<>();
        final Map<String, Vec3f> offsets = new HashMap<>();
        final Map<String, Vec3f> scales = new HashMap<>();
        int reads;

        FakeVanillaPose put(String bone, Quaternion rotation, float x, float y, float z)
        {
            rotations.put(bone, rotation);
            offsets.put(bone, new Vec3f(x, y, z));
            return this;
        }

        FakeVanillaPose scale(String bone, float x, float y, float z)
        {
            scales.put(bone, new Vec3f(x, y, z));
            return this;
        }

        @Override
        public boolean read(String bone, Quaternion rotation, Vec3f offset, Vec3f scale)
        {
            reads++;
            Quaternion r = rotations.get(bone);
            if (r == null)
            {
                return false;
            }
            rotation.set(r);
            offset.set(offsets.get(bone));
            Vec3f s = scales.get(bone);
            scale.set(s != null ? s : new Vec3f(1, 1, 1));
            return true;
        }
    }

    /** A layer posing {@code lower}, under one in a {@code core:vanilla_pose} node of {@code fields}, under one posing {@code upper}. */
    private static KumoAnimatorState animator(String lower, String fields, String upper) throws MalformedKumoTemplateException
    {
        return TestSubject.instance(("{'formatVersion': 2, 'layers': ["
                + "{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {'pose': [" + lower + "]}}}}, "
                + "{'defaultOnEntry': 'b', 'nodes': {'b': {'core:vanilla_pose': {" + fields + "}}}}, "
                + "{'defaultOnEntry': 'c', 'nodes': {'c': {'core:pose': {'pose': [" + upper + "]}}}}]}").replace('\'', '"'));
    }

    private static void assertRotation(Quaternion expected, Quaternion actual)
    {
        float dot = Math.abs(expected.x * actual.x + expected.y * actual.y + expected.z * actual.z + expected.w * actual.w);
        assertEquals("rotation " + actual.x + ", " + actual.y + ", " + actual.z + ", " + actual.w, 1F, dot, 1e-4F);
    }

    private static void assertVector(float x, float y, float z, Vec3f actual)
    {
        assertEquals("x", x, actual.x, 1e-4F);
        assertEquals("y", y, actual.y, 1e-4F);
        assertEquals("z", z, actual.z, 1e-4F);
    }

    private static Quaternion pitch(float degrees)
    {
        return TestSubject.axisAngle(1, 0, 0, degrees);
    }

    @Test
    public void posesTheBonesAsTheVanillaPoseHasThem() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("", "'bones': ['arm', 'leg']", "");
        TestSubject subject = new TestSubject("arm", "leg");
        subject.vanillaPose = new FakeVanillaPose().put("arm", pitch(-60), 0, 1, 0).put("leg", TestSubject.axisAngle(0, 0, 1, 20), 0, 0, 2);

        animator.update(subject, 1F);

        assertRotation(pitch(-60), subject.target("arm"));
        assertVector(0, 1, 0, subject.offsets.get("arm"));
        assertRotation(TestSubject.axisAngle(0, 0, 1, 20), subject.target("leg"));
        assertVector(0, 0, 2, subject.offsets.get("leg"));
    }

    @Test
    public void withoutBonesItPosesEveryBoneTheAnimatorNames() throws MalformedKumoTemplateException
    {
        // The leg is named by a layer below, the arm by one above; the tail by none.
        KumoAnimatorState animator = animator("{'core:rotate': {'bone': 'leg', 'axis': 'x', 'angle': 10}}", "",
                "{'core:rotate': {'bone': 'arm', 'axis': 'x', 'angle': 0}}");
        TestSubject subject = new TestSubject("arm", "leg", "tail");
        subject.vanillaPose = new FakeVanillaPose().put("arm", pitch(-60), 0, 0, 0).put("leg", pitch(30), 0, 0, 0)
                .put("tail", pitch(50), 0, 0, 0);

        animator.update(subject, 1F);

        assertRotation(pitch(-60), subject.target("arm"));
        assertRotation(pitch(30), subject.target("leg"));
        assertRotation(new Quaternion(), subject.target("tail"));
    }

    @Test
    public void scalesTheBonesAsTheVanillaPoseHasThem() throws MalformedKumoTemplateException
    {
        // The layer below scales both; the vanilla pose has the arm scaled and the leg as modelled.
        KumoAnimatorState animator = animator("{'core:scale': {'bone': 'arm', 'all': 3}}, {'core:scale': {'bone': 'leg', 'all': 3}}", "", "");
        TestSubject subject = new TestSubject("arm", "leg");
        subject.vanillaPose = new FakeVanillaPose().put("arm", pitch(0), 0, 0, 0).scale("arm", 1, 1.5F, 0.5F)
                .put("leg", pitch(0), 0, 0, 0);

        animator.update(subject, 1F);

        assertVector(1, 1.5F, 0.5F, subject.scales.get("arm"));
        assertVector(1, 1, 1, subject.scales.get("leg"));
    }

    @Test
    public void replacesWhatTheLayersBelowPosed() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:rotate': {'bone': 'arm', 'axis': 'x', 'angle': 45}}, "
                + "{'core:translate': {'bone': 'arm', 'x': 5}}", "", "");
        TestSubject subject = new TestSubject("arm");
        subject.vanillaPose = new FakeVanillaPose().put("arm", pitch(-60), 0, 0, 0);

        animator.update(subject, 1F);

        assertRotation(pitch(-60), subject.target("arm"));
        assertVector(0, 0, 0, subject.offsets.get("arm"));
    }

    @Test
    public void aBoneTheVanillaPoseLacksIsLeftToTheLayersBelow() throws MalformedKumoTemplateException
    {
        // A forearm, which no vanilla part stands for.
        KumoAnimatorState animator = animator("{'core:rotate': {'bone': 'foreArm', 'axis': 'x', 'angle': 45}}", "", "");
        TestSubject subject = new TestSubject("arm", "foreArm");
        subject.vanillaPose = new FakeVanillaPose().put("arm", pitch(-60), 0, 0, 0);

        animator.update(subject, 1F);

        assertRotation(pitch(45), subject.target("foreArm"));
    }

    @Test
    public void theLayersAboveBuildOnIt() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("", "", "{'core:rotate': {'bone': 'arm', 'axis': 'x', 'angle': -20}}, "
                + "{'core:rotate': {'bone': 'foreArm', 'axis': 'x', 'angle': -30}}");
        TestSubject subject = new TestSubject("arm", "foreArm");
        subject.vanillaPose = new FakeVanillaPose().put("arm", pitch(-60), 0, 0, 0).put("foreArm", new Quaternion(), 0, 0, 0);

        animator.update(subject, 1F);

        assertRotation(pitch(-80), subject.target("arm"));
        assertRotation(pitch(-30), subject.target("foreArm"));
    }

    @Test
    public void itsOwnItemsBuildOnIt() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("", "'pose': [{'core:rotate': {'bone': 'arm', 'axis': 'x', 'angle': -20}}]", "");
        TestSubject subject = new TestSubject("arm");
        subject.vanillaPose = new FakeVanillaPose().put("arm", pitch(-60), 0, 0, 0);

        animator.update(subject, 1F);

        assertRotation(pitch(-80), subject.target("arm"));
    }

    @Test
    public void bonesKeepsItToThoseBones() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:rotate': {'bone': 'leg', 'axis': 'x', 'angle': 10}}", "'bones': ['arm']", "");
        TestSubject subject = new TestSubject("arm", "leg");
        subject.vanillaPose = new FakeVanillaPose().put("arm", pitch(-60), 0, 0, 0).put("leg", pitch(30), 0, 0, 0);

        animator.update(subject, 1F);

        assertRotation(pitch(-60), subject.target("arm"));
        assertRotation(pitch(10), subject.target("leg"));
    }

    @Test
    public void withoutAVanillaPoseItPosesNothing() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:rotate': {'bone': 'arm', 'axis': 'x', 'angle': 45}}", "", "");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);

        assertRotation(pitch(45), subject.target("arm"));
    }

    @Test
    public void followsTheVanillaPoseEveryFrame() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("", "'bones': ['arm']", "");
        TestSubject subject = new TestSubject("arm");
        FakeVanillaPose vanilla = new FakeVanillaPose().put("arm", pitch(-60), 0, 0, 0);
        subject.vanillaPose = vanilla;

        animator.update(subject, 1F);
        vanilla.put("arm", pitch(-10), 0, 0, 0);
        animator.update(subject, 1F);

        assertRotation(pitch(-10), subject.target("arm"));
    }

    @Test
    public void putsTheEntitysOwnBonesAtRest() throws MalformedKumoTemplateException
    {
        // Vanilla's parts are drawn in the entity's frame: the root the layers below moved goes back.
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'root', 'x': 3}, '@space': 'override'}", "",
                "{'core:rotate': {'bone': 'arm', 'axis': 'x', 'angle': 0}}");
        TestSubject subject = new TestSubject("arm");
        // A pose the vanilla model can't have, which isn't read.
        subject.vanillaPose = new FakeVanillaPose().put("arm", pitch(-60), 0, 0, 0).put("root", pitch(90), 7, 7, 7);

        animator.update(subject, 1F);

        assertVector(0, 0, 0, subject.root.position.end);
        assertEquals(1F, subject.entityFrameVanilla, 1e-6F);
    }

    @Test
    public void leavesTheEntitysOwnBonesWithoutAVanillaPose() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'root', 'x': 3}, '@space': 'override'}", "", "");
        TestSubject subject = new TestSubject("arm");
        subject.vanillaPose = new FakeVanillaPose();

        animator.update(subject, 1F);

        assertVector(3, 0, 0, subject.root.position.end);
        assertEquals(0F, subject.entityFrameVanilla, 1e-6F);
    }

    @Test
    public void bonesCanLeaveTheEntitysOwnBonesOut() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'root', 'x': 3}, '@space': 'override'}", "'bones': ['arm']", "");
        TestSubject subject = new TestSubject("arm");
        subject.vanillaPose = new FakeVanillaPose().put("arm", pitch(-60), 0, 0, 0);

        animator.update(subject, 1F);

        assertVector(3, 0, 0, subject.root.position.end);
        // The animator keeps the entity's frame.
        assertEquals(0F, subject.entityFrameVanilla, 1e-6F);
    }

    @Test
    public void bonesCanListTheEntitysOwnBones() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:translate': {'bone': 'root', 'x': 3}, '@space': 'override'}", "'bones': ['arm', 'root']", "");
        TestSubject subject = new TestSubject("arm");
        subject.vanillaPose = new FakeVanillaPose().put("arm", pitch(-60), 0, 0, 0);

        animator.update(subject, 1F);

        assertVector(0, 0, 0, subject.root.position.end);
    }

    @Test
    public void theEntitysFrameFadesWithTheLayer() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = TestSubject.instance(("{'formatVersion': 2, 'layers': ["
                + "{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {'pose': [{'core:translate': {'bone': 'root', 'x': 3}}, "
                + "{'core:rotate': {'bone': 'arm', 'axis': 'x', 'angle': 0}}]}}}}, "
                + "{'@when': 'GO', '@fade': 4, 'defaultOnEntry': 'b', 'nodes': {'b': {'core:vanilla_pose': {}}}}]}").replace('\'', '"'));
        TestSubject subject = new TestSubject("arm");
        subject.vanillaPose = new FakeVanillaPose().put("arm", pitch(-60), 0, 0, 0);
        subject.states.put("GO", false);

        animator.update(subject, 1F);
        assertEquals(0F, subject.entityFrameVanilla, 1e-6F);
        subject.states.put("GO", true);
        animator.update(subject, 1F);
        animator.update(subject, 1F);
        // Half way in, as the bones are.
        assertEquals(0.5F, subject.entityFrameVanilla, 1e-4F);
        animator.update(subject, 1F);
        animator.update(subject, 1F);
        assertEquals(1F, subject.entityFrameVanilla, 1e-6F);
        subject.states.put("GO", false);
        animator.update(subject, 1F);
        animator.update(subject, 1F);
        assertEquals(0.5F, subject.entityFrameVanilla, 1e-4F);
        animator.update(subject, 1F);
        animator.update(subject, 1F);
        assertEquals(0F, subject.entityFrameVanilla, 1e-6F);
    }

    @Test
    public void theEntitysFrameFollowsATransition() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = TestSubject.instance(("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'own', 'nodes': {"
                + "'own': {'core:pose': {'pose': [{'core:translate': {'bone': 'root', 'x': 3}}, "
                + "{'core:rotate': {'bone': 'arm', 'axis': 'x', 'angle': 0}}]}, "
                + "'@connections': [{'when': {'gt': ['nodeTicksElapsed', 2]}, 'then': 'theirs', 'transitionDuration': 10, 'transitionEasing': 'linear'}]}, "
                + "'theirs': {'core:vanilla_pose': {}}}}]}").replace('\'', '"'));
        TestSubject subject = new TestSubject("arm");
        subject.vanillaPose = new FakeVanillaPose().put("arm", pitch(-60), 0, 0, 0);

        // Three frames in the first node, then the one the transition starts on.
        for (int frame = 0; frame < 4; frame++)
        {
            animator.update(subject, 1F);
            assertEquals(0F, subject.entityFrameVanilla, 1e-6F);
        }
        for (int frame = 0; frame < 5; frame++)
        {
            animator.update(subject, 1F);
        }
        assertEquals(0.5F, subject.entityFrameVanilla, 1e-4F);
        for (int frame = 0; frame < 5; frame++)
        {
            animator.update(subject, 1F);
        }
        assertEquals(1F, subject.entityFrameVanilla, 1e-6F);
    }

    @Test
    public void takesCorePosesFields() throws MalformedKumoTemplateException
    {
        // Smoothing, as a core:pose node's, smooths what the vanilla pose writes.
        KumoAnimatorState animator = animator("", "'smoothing': {'arm': 10}", "");
        TestSubject subject = new TestSubject("arm");
        subject.vanillaPose = new FakeVanillaPose().put("arm", pitch(-60), 0, 0, 0);

        animator.update(subject, 1F);

        assertRotation(pitch(-60), subject.target("arm"));
        assertEquals(10F, subject.bones.get("arm").getSmoothing(), 1e-4F);
    }

}
