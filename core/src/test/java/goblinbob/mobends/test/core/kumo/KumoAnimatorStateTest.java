package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.math.Quaternion;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class KumoAnimatorStateTest
{

    private static final String STILL_CLIP = "{\"bones\": {\"arm\": {\"keyframes\": [{\"rotation\": [0, 0, 0, 1]}, {\"rotation\": [0, 0, 0, 1]}]}}}";

    private static void assertRotation(Quaternion expected, Quaternion actual)
    {
        float dot = Math.abs(expected.x * actual.x + expected.y * actual.y + expected.z * actual.z + expected.w * actual.w);
        assertEquals("rotation " + actual.x + ", " + actual.y + ", " + actual.z + ", " + actual.w, 1F, dot, 1e-4F);
    }

    @Test
    public void animationFinishedWaitsForTheTimedClipEvenWhenADriverComesFirst() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"entryNode\": \"play\", \"nodes\": {"
                + "\"play\": {\"tags\": [\"play\"], \"pose\": ["
                + "  {\"driver\": \"core:axis_rotate\", \"bone\": \"arm\", \"axis\": \"X\", \"angle\": 10},"
                + "  {\"animationKey\": \"clip\", \"duration\": 5}],"
                + " \"connections\": [{\"target\": \"done\", \"triggerCondition\": {\"type\": \"core:animation_finished\"}}]},"
                + "\"done\": {\"tags\": [\"done\"]}}}]}", Collections.singletonMap("clip", STILL_CLIP));
        TestSubject subject = new TestSubject("arm");

        for (int frame = 0; frame < 5; frame++)
        {
            animator.update(subject, 1F);
            assertEquals("frame " + frame, Collections.singletonList("play"), animator.getActions());
        }
        animator.update(subject, 1F);
        assertEquals(Collections.singletonList("done"), animator.getActions());
    }

    @Test
    public void aNodeOfUntimedItemsNeverFinishes() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"entryNode\": \"loop\", \"nodes\": {"
                + "\"loop\": {\"tags\": [\"loop\"], \"pose\": [{\"animationKey\": \"clip\"}],"
                + " \"connections\": [{\"target\": \"done\", \"triggerCondition\": {\"type\": \"core:animation_finished\"}}]},"
                + "\"done\": {\"tags\": [\"done\"]}}}]}", Collections.singletonMap("clip", STILL_CLIP));
        TestSubject subject = new TestSubject("arm");

        for (int frame = 0; frame < 50; frame++)
        {
            animator.update(subject, 1F);
        }
        assertEquals(Collections.singletonList("loop"), animator.getActions());
    }

    @Test
    public void anItemsTicksPassedCountsFromItsNode() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"entryNode\": \"wait\", \"nodes\": {"
                + "\"wait\": {\"connections\": [{\"target\": \"raise\", \"triggerCondition\": {\"type\": \"core:ticks_passed\", \"ticksToPass\": 10}}]},"
                + "\"raise\": {\"pose\": [{\"driver\": \"core:axis_rotate\", \"bone\": \"arm\", \"axis\": \"X\", \"angle\": 45, \"space\": \"OVERRIDE\","
                + "  \"when\": {\"type\": \"core:ticks_passed\", \"ticksToPass\": 3}}]}}}]}");
        TestSubject subject = new TestSubject("arm");

        for (int frame = 0; frame < 12; frame++)
        {
            animator.update(subject, 1F);
        }
        // "raise" was entered a frame ago: well past 3 ticks on the layer's clock, not on the node's.
        assertRotation(new Quaternion(), subject.target("arm"));

        for (int frame = 0; frame < 4; frame++)
        {
            animator.update(subject, 1F);
        }
        assertRotation(TestSubject.axisAngle(1, 0, 0, 45), subject.target("arm"));
    }

    @Test
    public void aCrossFadeFromARelativeToAnAbsolutePoseStartsWhereItWas() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = TestSubject.instance("{\"formatVersion\": 2, \"layers\": ["
                + "{\"entryNode\": \"base\", \"nodes\": {\"base\": {\"pose\": ["
                + "  {\"driver\": \"core:axis_rotate\", \"bone\": \"arm\", \"axis\": \"X\", \"angle\": 90, \"space\": \"OVERRIDE\"}]}}},"
                + "{\"entryNode\": \"relative\", \"nodes\": {"
                + "  \"relative\": {\"pose\": [{\"driver\": \"core:axis_rotate\", \"bone\": \"arm\", \"axis\": \"Y\", \"angle\": 90, \"space\": \"PRE\"}],"
                + "   \"connections\": [{\"target\": \"absolute\", \"transitionDuration\": 10, \"transitionEasing\": \"LINEAR\","
                + "     \"triggerCondition\": {\"type\": \"core:ticks_passed\", \"ticksToPass\": 2}}]},"
                + "  \"absolute\": {\"pose\": [{\"driver\": \"core:axis_rotate\", \"bone\": \"arm\", \"axis\": \"Z\", \"angle\": 0, \"space\": \"OVERRIDE\"}]}}}]}");
        TestSubject subject = new TestSubject("arm");

        Quaternion relative = new Quaternion();
        Quaternion.mul(TestSubject.axisAngle(0, 1, 0, 90), TestSubject.axisAngle(1, 0, 0, 90), relative);
        for (int frame = 0; frame < 3; frame++)
        {
            animator.update(subject, 1F);
            assertRotation(relative, subject.target("arm"));
        }
        // The transition starts: at t = 0 the relative side, resolved against the layer below, shows.
        animator.update(subject, 1F);
        assertRotation(relative, subject.target("arm"));

        for (int frame = 0; frame < 10; frame++)
        {
            animator.update(subject, 1F);
        }
        assertRotation(new Quaternion(), subject.target("arm"));
    }

    @Test
    public void anUnknownVariableFailsTheAnimatorInsteadOfEscaping()
    {
        try
        {
            KumoAnimatorState animator = TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"entryNode\": \"a\", \"nodes\": {\"a\": {\"pose\": ["
                    + "{\"driver\": \"core:axis_rotate\", \"bone\": \"arm\", \"axis\": \"X\", \"angle\": \"noSuchVariable\"}]}}}]}");
            animator.update(new TestSubject("arm"), 1F);
            fail("The animator should have failed.");
        }
        catch (MalformedKumoTemplateException e)
        {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    public void anUnknownStateFailsTheAnimatorInsteadOfEscaping()
    {
        try
        {
            KumoAnimatorState animator = TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"entryNode\": \"a\", \"nodes\": {\"a\": {"
                    + "\"connections\": [{\"target\": \"a\", \"triggerCondition\": {\"type\": \"core:state\", \"state\": \"FLYING\"}}]}}}]}");
            animator.update(new TestSubject("arm"), 1F);
            fail("The animator should have failed.");
        }
        catch (MalformedKumoTemplateException e)
        {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    public void aComparisonTakesExpressionsAndSeesTheNodesNamedExpressions() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"entryNode\": \"walk\", \"nodes\": {"
                + "\"walk\": {\"tags\": [\"walk\"], \"expressions\": {\"doubled\": {\"mul\": [\"speed\", 2]}},"
                + " \"connections\": [{\"target\": \"run\", \"triggerCondition\": {\"type\": \"core:compare\", \"left\": \"doubled\", \"op\": \">\", \"right\": {\"add\": [\"limit\", 1]}}}]},"
                + "\"run\": {\"tags\": [\"run\"]}}}]}");
        TestSubject subject = new TestSubject("arm");
        subject.variables.put("limit", 2D);

        subject.variables.put("speed", 1.5D);
        animator.update(subject, 1F);
        assertEquals(Collections.singletonList("walk"), animator.getActions());

        subject.variables.put("speed", 2D);
        animator.update(subject, 1F);
        assertEquals(Collections.singletonList("run"), animator.getActions());
    }

    @Test
    public void decreasedWatchesAnExpression() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"entryNode\": \"a\", \"nodes\": {"
                + "\"a\": {\"tags\": [\"a\"], \"connections\": [{\"target\": \"b\", \"triggerCondition\": {\"type\": \"core:decreased\", \"value\": {\"abs\": [\"x\"]}}}]},"
                + "\"b\": {\"tags\": [\"b\"]}}}]}");
        TestSubject subject = new TestSubject("arm");

        subject.variables.put("x", 1D);
        animator.update(subject, 1F);
        // x falls, but its absolute value rises.
        subject.variables.put("x", -3D);
        animator.update(subject, 1F);
        assertEquals(Collections.singletonList("a"), animator.getActions());

        subject.variables.put("x", 2D);
        animator.update(subject, 1F);
        assertEquals(Collections.singletonList("b"), animator.getActions());
    }

    private static final String QUARTER_TURN_CLIP = "{\"bones\": {\"arm\": {\"keyframes\": [{\"rotation\": [0.70710677, 0, 0, 0.70710677]}, {\"rotation\": [0.70710677, 0, 0, 0.70710677]}]}}}";

    @Test
    public void aWeightedClipThatReplacesBlendsFromTheLayersBelow() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = TestSubject.instance("{\"formatVersion\": 2, \"layers\": ["
                + "{\"entryNode\": \"a\", \"nodes\": {\"a\": {\"pose\": [{\"driver\": \"core:axis_rotate\", \"bone\": \"arm\", \"axis\": \"X\", \"angle\": 90, \"space\": \"OVERRIDE\"}]}}},"
                + "{\"entryNode\": \"b\", \"nodes\": {\"b\": {\"pose\": [{\"animationKey\": \"clip\", \"weight\": 0.5, \"space\": \"OVERRIDE\"}]}}}]}",
                Collections.singletonMap("clip", STILL_CLIP));
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);
        // Halfway between the 90 degrees below and the clip's rest rotation.
        assertRotation(TestSubject.axisAngle(1, 0, 0, 45), subject.target("arm"));
    }

    @Test
    public void aWeightedClipWithNothingBelowBlendsFromTheRestPose() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = TestSubject.instance("{\"formatVersion\": 2, \"layers\": ["
                + "{\"entryNode\": \"b\", \"nodes\": {\"b\": {\"pose\": [{\"animationKey\": \"clip\", \"weight\": 0.5}]}}}]}",
                Collections.singletonMap("clip", QUARTER_TURN_CLIP));
        TestSubject subject = new TestSubject("arm");

        for (int frame = 0; frame < 3; frame++)
        {
            animator.update(subject, 1F);
        }
        // Not from last frame's result, which would make the weight a smoothing rate.
        assertRotation(TestSubject.axisAngle(1, 0, 0, 45), subject.target("arm"));
    }

    @Test(expected = MalformedKumoTemplateException.class)
    public void aMaskWithoutAModeIsRefused() throws MalformedKumoTemplateException
    {
        TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"entryNode\": \"a\", \"mask\": {\"includedParts\": [\"arm\"]}, \"nodes\": {\"a\": {}}}]}");
    }

    @Test(expected = MalformedKumoTemplateException.class)
    public void aVanillaNodeCantPose() throws MalformedKumoTemplateException
    {
        TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"entryNode\": \"a\", \"nodes\": {\"a\": {\"type\": \"core:vanilla\", \"snapOnEnter\": [\"arm\"]}}}]}");
    }

    @Test(expected = MalformedKumoTemplateException.class)
    public void anAndWithoutConditionsIsRefused() throws MalformedKumoTemplateException
    {
        TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"entryNode\": \"a\", \"nodes\": {\"a\": {"
                + "\"connections\": [{\"target\": \"a\", \"triggerCondition\": {\"type\": \"core:and\"}}]}}}]}");
    }

}
