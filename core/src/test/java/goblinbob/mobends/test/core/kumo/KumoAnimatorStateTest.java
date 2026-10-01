package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.math.Quaternion;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

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
        KumoAnimatorState animator = TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"defaultOnEntry\": \"play\", \"nodes\": {"
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
        KumoAnimatorState animator = TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"defaultOnEntry\": \"loop\", \"nodes\": {"
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
        KumoAnimatorState animator = TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"defaultOnEntry\": \"wait\", \"nodes\": {"
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
                + "{\"defaultOnEntry\": \"base\", \"nodes\": {\"base\": {\"pose\": ["
                + "  {\"driver\": \"core:axis_rotate\", \"bone\": \"arm\", \"axis\": \"X\", \"angle\": 90, \"space\": \"OVERRIDE\"}]}}},"
                + "{\"defaultOnEntry\": \"relative\", \"nodes\": {"
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
            KumoAnimatorState animator = TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"defaultOnEntry\": \"a\", \"nodes\": {\"a\": {\"pose\": ["
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
            KumoAnimatorState animator = TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"defaultOnEntry\": \"a\", \"nodes\": {\"a\": {"
                    + "\"connections\": [{\"target\": \"a\", \"triggerCondition\": {\"type\": \"core:state\", \"state\": \"FLYING\"}}]}}}]}");
            TestSubject subject = new TestSubject("arm");
            // Entering the layer is the first frame's decision: its connections are checked from the second.
            animator.update(subject, 1F);
            animator.update(subject, 1F);
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
        KumoAnimatorState animator = TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"defaultOnEntry\": \"walk\", \"nodes\": {"
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
        KumoAnimatorState animator = TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"defaultOnEntry\": \"a\", \"nodes\": {"
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
                + "{\"defaultOnEntry\": \"a\", \"nodes\": {\"a\": {\"pose\": [{\"driver\": \"core:axis_rotate\", \"bone\": \"arm\", \"axis\": \"X\", \"angle\": 90, \"space\": \"OVERRIDE\"}]}}},"
                + "{\"defaultOnEntry\": \"b\", \"nodes\": {\"b\": {\"pose\": [{\"animationKey\": \"clip\", \"weight\": 0.5, \"space\": \"OVERRIDE\"}]}}}]}",
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
                + "{\"defaultOnEntry\": \"b\", \"nodes\": {\"b\": {\"pose\": [{\"animationKey\": \"clip\", \"weight\": 0.5}]}}}]}",
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
        TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"defaultOnEntry\": \"a\", \"mask\": {\"includedParts\": [\"arm\"]}, \"nodes\": {\"a\": {}}}]}");
    }

    @Test(expected = MalformedKumoTemplateException.class)
    public void aVanillaNodeCantPose() throws MalformedKumoTemplateException
    {
        TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"defaultOnEntry\": \"a\", \"nodes\": {\"a\": {\"type\": \"core:vanilla\", \"snapOnEnter\": [\"arm\"]}}}]}");
    }

    @Test(expected = MalformedKumoTemplateException.class)
    public void anAndWithoutConditionsIsRefused() throws MalformedKumoTemplateException
    {
        TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"defaultOnEntry\": \"a\", \"nodes\": {\"a\": {"
                + "\"connections\": [{\"target\": \"a\", \"triggerCondition\": {\"type\": \"core:and\"}}]}}}]}");
    }

    /** An animator from JSON written with single quotes. */
    private static KumoAnimatorState animator(String json) throws MalformedKumoTemplateException
    {
        return TestSubject.instance(json.replace('\'', '"'));
    }

    private static List<String> frame(KumoAnimatorState animator, TestSubject subject) throws MalformedKumoTemplateException
    {
        animator.update(subject, 1F);
        return animator.getActions();
    }

    private static final String LOCOMOTION = "{'formatVersion': 2,"
            + " 'conditions': {'jumping': {'type': 'core:state', 'state': 'AIRBORNE'}},"
            + " 'layers': [{'defaultOnEntry': 'stand', 'select': ["
            + "  {'when': 'jumping', 'then': ["
            + "    {'when': {'type': 'core:state', 'state': 'FLYING'}, 'then': 'fly'},"
            + "    {'when': {'type': 'core:compare', 'left': 'fall', 'op': '>', 'right': 10}, 'then': 'fall'}]},"
            + "  {'when': {'type': 'core:state', 'state': 'STANDING_STILL'}, 'then': 'stand'},"
            + "  {'when': {'type': 'core:compare', 'left': 'speed', 'op': '>=', 'right': 2}, 'then': 'walk'}],"
            + " 'nodes': {'stand': {'tags': ['stand']}, 'walk': {'tags': ['walk']}, 'fly': {'tags': ['fly']}, 'fall': {'tags': ['fall']}}}]}";

    private static TestSubject locomotionSubject()
    {
        TestSubject subject = new TestSubject("arm");
        subject.states.put("AIRBORNE", false);
        subject.states.put("FLYING", false);
        subject.states.put("STANDING_STILL", true);
        subject.variables.put("fall", 0D);
        subject.variables.put("speed", 0D);
        return subject;
    }

    @Test
    public void aSelectorTakesTheFirstBranchThatHolds() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator(LOCOMOTION);
        TestSubject subject = locomotionSubject();
        assertEquals(Collections.singletonList("stand"), frame(animator, subject));

        subject.states.put("STANDING_STILL", false);
        subject.variables.put("speed", 3D);
        assertEquals(Collections.singletonList("walk"), frame(animator, subject));

        // Airborne and flying: the jumping branch comes first, whatever the speed.
        subject.states.put("AIRBORNE", true);
        subject.states.put("FLYING", true);
        assertEquals(Collections.singletonList("fly"), frame(animator, subject));
    }

    @Test
    public void whereTheSelectorChoosesNothingTheLayerStays() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator(LOCOMOTION);
        TestSubject subject = locomotionSubject();
        subject.states.put("STANDING_STILL", false);
        subject.variables.put("speed", 3D);
        assertEquals(Collections.singletonList("walk"), frame(animator, subject));

        // Slower than the walk's branch, not still: no branch holds.
        subject.variables.put("speed", 1D);
        assertEquals(Collections.singletonList("walk"), frame(animator, subject));

        // Airborne, but neither flying nor falling long: the taken branch decides nothing, and the
        // branches after it are not tried.
        subject.states.put("AIRBORNE", true);
        subject.states.put("STANDING_STILL", true);
        assertEquals(Collections.singletonList("walk"), frame(animator, subject));

        subject.variables.put("fall", 11D);
        assertEquals(Collections.singletonList("fall"), frame(animator, subject));
    }

    private static final String SWORD = "{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'idle', 'variables': {'combo': 0, 'entered': 0},"
            + " 'select': [{'when': {'type': 'core:state', 'state': 'SWORD'}, 'then': 'sword', 'set': {'combo': 0}},"
            + "            {'then': 'idle'}],"
            + " 'nodes': {'idle': {'tags': ['idle']}},"
            + " 'machines': {'sword': {'defaultOnEntry': 'sword_idle',"
            + "   'conditions': {'attacked': {'type': 'core:decreased', 'value': 'sinceAttack'}},"
            + "   'select': [{'when': {'type': 'core:compare', 'left': 'sinceAttack', 'op': '>=', 'right': 10}, 'then': ["
            + "     {'when': {'type': 'core:state', 'state': 'STILL'}, 'then': 'stance', 'set': {'entered': 1}},"
            + "     {'then': 'sword_idle'}]}],"
            + "   'connections': ["
            + "     {'target': 'slash_a', 'triggerCondition': {'type': 'core:and', 'conditions': ['attacked', {'type': 'core:compare', 'left': 'combo', 'op': '==', 'right': 0}]}, 'set': {'combo': 1}},"
            + "     {'target': 'slash_b', 'triggerCondition': {'type': 'core:and', 'conditions': ['attacked', {'type': 'core:compare', 'left': 'combo', 'op': '==', 'right': 1}]}, 'set': {'combo': 0}}],"
            + "   'nodes': {'sword_idle': {'tags': ['sword_idle']}, 'stance': {'tags': ['stance']}, 'slash_a': {'tags': ['slash_a']}, 'slash_b': {'tags': ['slash_b']}}}}}]}";

    private static TestSubject swordSubject()
    {
        TestSubject subject = new TestSubject("arm");
        subject.states.put("SWORD", false);
        subject.states.put("STILL", false);
        subject.variables.put("sinceAttack", 100D);
        return subject;
    }

    @Test
    public void aMachineIsEnteredThroughItsSelectorOrItsDefaultOnEntry() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator(SWORD);
        TestSubject subject = swordSubject();
        assertEquals(Collections.singletonList("idle"), frame(animator, subject));

        subject.states.put("SWORD", true);
        subject.states.put("STILL", true);
        assertEquals(Collections.singletonList("stance"), frame(animator, subject));

        subject.states.put("SWORD", false);
        assertEquals(Collections.singletonList("idle"), frame(animator, subject));

        // The machine's selector chooses nothing so soon after an attack: its defaultOnEntry.
        subject.states.put("SWORD", true);
        subject.variables.put("sinceAttack", 3D);
        assertEquals(Collections.singletonList("sword_idle"), frame(animator, subject));
    }

    @Test
    public void aMachinesConnectionsLeadOutOfAnyOfItsNodes() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator(SWORD);
        TestSubject subject = swordSubject();
        subject.states.put("SWORD", true);
        assertEquals(Collections.singletonList("sword_idle"), frame(animator, subject));

        // Each attack: the next slash; the layer's selector keeps choosing the machine, which
        // chooses nothing while a slash plays, so the slash holds.
        subject.variables.put("sinceAttack", 0D);
        assertEquals(Collections.singletonList("slash_a"), frame(animator, subject));
        subject.variables.put("sinceAttack", 5D);
        assertEquals(Collections.singletonList("slash_a"), frame(animator, subject));
        subject.variables.put("sinceAttack", 0D);
        assertEquals(Collections.singletonList("slash_b"), frame(animator, subject));

        // The window closes: the machine's selector takes over again.
        subject.variables.put("sinceAttack", 12D);
        assertEquals(Collections.singletonList("sword_idle"), frame(animator, subject));
        subject.variables.put("sinceAttack", 0D);
        assertEquals(Collections.singletonList("slash_a"), frame(animator, subject));
    }

    @Test
    public void aLayerStartsWhereItsSelectorLeadsWithoutACrossfade() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', 'variables': {'v': 0},"
                + " 'select': [{'when': {'type': 'core:state', 'state': 'GO'}, 'then': 'b', 'transitionDuration': 10, 'set': {'v': 1}}],"
                + " 'nodes': {"
                + "  'a': {'tags': ['a'], 'pose': [{'driver': 'core:axis_rotate', 'bone': 'arm', 'axis': 'X', 'angle': 0, 'space': 'OVERRIDE'}]},"
                + "  'b': {'tags': ['b'], 'pose': [{'driver': 'core:axis_rotate', 'bone': 'arm', 'axis': 'X', 'angle': 90, 'space': 'OVERRIDE'}],"
                + "   'connections': [{'target': 'c', 'triggerCondition': {'type': 'core:compare', 'left': 'v', 'op': '==', 'right': 1}}]},"
                + "  'c': {'tags': ['c']}}}]}");
        TestSubject subject = new TestSubject("arm");
        subject.states.put("GO", true);

        assertEquals(Collections.singletonList("b"), frame(animator, subject));
        // Not faded in from the defaultOnEntry, which the layer was never in.
        assertRotation(TestSubject.axisAngle(1, 0, 0, 90), subject.target("arm"));
        // The branch's set applied on the way in.
        assertEquals(Collections.singletonList("c"), frame(animator, subject));
    }

    @Test
    public void aLayerStartsThroughTheSelectorsOfTheMachinesItEnters() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'm',"
                + " 'select': [{'when': {'type': 'core:state', 'state': 'OUT'}, 'then': 'out'}],"
                + " 'nodes': {'out': {'tags': ['out']}},"
                + " 'machines': {'m': {'select': [{'when': {'type': 'core:state', 'state': 'LATE'}, 'then': 'late'}],"
                + "   'nodes': {'early': {'tags': ['early']}, 'late': {'tags': ['late']}}}}}]}");
        TestSubject subject = new TestSubject("arm");
        subject.states.put("OUT", false);
        subject.states.put("LATE", true);
        // The layer's selector chooses nothing: its defaultOnEntry, the machine, whose selector decides.
        assertEquals(Collections.singletonList("late"), frame(animator, subject));

        animator = animator("{'formatVersion': 2, 'layers': [{'machines': {'m': {"
                + "   'select': [{'when': {'type': 'core:state', 'state': 'LATE'}, 'then': 'late'}],"
                + "   'nodes': {'early': {'tags': ['early']}, 'late': {'tags': ['late']}}}}}]}");
        subject.states.put("LATE", false);
        // No node of its own, no defaultOnEntry: the layer's first machine; nothing chosen there: its first node.
        assertEquals(Collections.singletonList("early"), frame(animator, subject));
    }

    private static final String ENTERED_CLOCK = "{'formatVersion': 2, 'layers': [{"
            + " 'select': [{'when': {'type': 'core:state', 'state': 'IN'}, 'then': 'm'}, {'then': 'out'}],"
            + " 'nodes': {'out': {'tags': ['out']}},"
            + " 'machines': {'m': {'defaultOnEntry': 'calm',"
            + "   'select': [{'when': {'type': 'core:ticks_passed', 'ticksToPass': 5}, 'then': 'late'},"
            + "              {'when': {'type': 'core:decreased', 'value': 'x'}, 'then': 'hit'}],"
            + "   'nodes': {'calm': {'tags': ['calm']}, 'late': {'tags': ['late']}, 'hit': {'tags': ['hit']}}}}}]}";

    @Test
    public void aMachinesSelectorClockStartsOverBeforeItChoosesOnEntry() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator(ENTERED_CLOCK);
        TestSubject subject = new TestSubject("arm");
        subject.states.put("IN", false);
        subject.variables.put("x", 0D);
        for (int frame = 0; frame < 10; frame++)
        {
            assertEquals(Collections.singletonList("out"), frame(animator, subject));
        }

        // Ten ticks into the layer, but none into the machine.
        subject.states.put("IN", true);
        assertEquals(Collections.singletonList("calm"), frame(animator, subject));
        for (int frame = 0; frame < 5; frame++)
        {
            assertEquals("frame " + frame, Collections.singletonList("calm"), frame(animator, subject));
        }
        assertEquals(Collections.singletonList("late"), frame(animator, subject));
    }

    @Test
    public void aMachinesSelectorEdgeTriggerStartsOverBeforeItChoosesOnEntry() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator(ENTERED_CLOCK);
        TestSubject subject = new TestSubject("arm");
        subject.states.put("IN", true);
        subject.variables.put("x", 5D);
        assertEquals(Collections.singletonList("calm"), frame(animator, subject));

        subject.states.put("IN", false);
        assertEquals(Collections.singletonList("out"), frame(animator, subject));
        // x drops while the machine is not entered: nothing it should react to on the way in.
        subject.variables.put("x", 1D);
        frame(animator, subject);
        subject.states.put("IN", true);
        assertEquals(Collections.singletonList("calm"), frame(animator, subject));

        subject.variables.put("x", 0D);
        assertEquals(Collections.singletonList("hit"), frame(animator, subject));
    }

    @Test
    public void aConnectionToTheCurrentNodeStartsItOver() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'formatVersion': 2, 'layers': [{'select': [{'then': 'jump'}],"
                + " 'nodes': {'jump': {'connections': [{'target': 'jump', 'triggerCondition': {'type': 'core:decreased', 'value': 'height'}}]}}}]}");
        TestSubject subject = new TestSubject("arm");
        subject.variables.put("height", 5D);
        frame(animator, subject);
        subject.variables.put("height", 1D);
        frame(animator, subject);
        // Started over this frame: one tick in, not two.
        assertEquals(1F, animator.getLayers().get(0).getCurrentNode().getElapsedTicks(), 0F);
    }

    @Test
    public void aNamedConditionIsACopyPerUse() throws MalformedKumoTemplateException
    {
        // The edge trigger is used twice; were it one instance, the second use would never see the drop.
        KumoAnimatorState animator = animator("{'formatVersion': 2, 'conditions': {'dropped': {'type': 'core:decreased', 'value': 'x'}},"
                + " 'layers': [{'defaultOnEntry': 'a', 'nodes': {"
                + "  'a': {'tags': ['a'], 'connections': [{'target': 'b', 'triggerCondition': {'type': 'core:and', 'conditions': ['dropped', 'dropped']}}]},"
                + "  'b': {'tags': ['b']}}}]}");
        TestSubject subject = new TestSubject("arm");
        subject.variables.put("x", 2D);
        assertEquals(Collections.singletonList("a"), frame(animator, subject));
        subject.variables.put("x", 1D);
        assertEquals(Collections.singletonList("b"), frame(animator, subject));
    }

    @Test
    public void anInnerNamedConditionShadowsAnOuterOne() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'formatVersion': 2, 'conditions': {'go': {'type': 'core:state', 'state': 'NEVER'}},"
                + " 'layers': [{'conditions': {'go': {'type': 'core:state', 'state': 'ALWAYS'}},"
                + "  'select': [{'when': 'go', 'then': 'b'}], 'nodes': {'a': {'tags': ['a']}, 'b': {'tags': ['b']}}}]}");
        TestSubject subject = new TestSubject("arm");
        subject.states.put("NEVER", false);
        subject.states.put("ALWAYS", true);
        assertEquals(Collections.singletonList("b"), frame(animator, subject));
    }

    @Test(expected = MalformedKumoTemplateException.class)
    public void aNamedConditionCantUseItself() throws MalformedKumoTemplateException
    {
        animator("{'formatVersion': 2, 'conditions': {'p': {'type': 'core:not', 'condition': 'q'}, 'q': 'p'},"
                + " 'layers': [{'nodes': {'a': {}}}]}");
    }

    @Test(expected = MalformedKumoTemplateException.class)
    public void anUnknownConditionNameIsRefused() throws MalformedKumoTemplateException
    {
        animator("{'formatVersion': 2, 'layers': [{'select': [{'when': 'nope', 'then': 'a'}], 'nodes': {'a': {}}}]}");
    }

    @Test(expected = MalformedKumoTemplateException.class)
    public void aSelectorOnlyNamesItsOwnMachinesMembers() throws MalformedKumoTemplateException
    {
        animator("{'formatVersion': 2, 'layers': [{'select': [{'then': 'inner'}], 'nodes': {'a': {}},"
                + " 'machines': {'m': {'nodes': {'inner': {}}}}}]}");
    }

    @Test(expected = MalformedKumoTemplateException.class)
    public void namesAreUniqueAcrossTheLayersMachines() throws MalformedKumoTemplateException
    {
        animator("{'formatVersion': 2, 'layers': [{'nodes': {'a': {}}, 'machines': {'m': {'nodes': {'a': {}}}}}]}");
    }

}
