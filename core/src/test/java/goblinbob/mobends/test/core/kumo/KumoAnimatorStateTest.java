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
        KumoAnimatorState animator = TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"defaultOnEntry\": \"play\", "
                + "\"nodes\": {\"play\": {\"core:pose\": {\"pose\": [{\"core:axis_rotate\": {\"bone\": \"arm\", \"axis\": \"X\", "
                + "\"angle\": 10}}, {\"core:clip\": {\"animationKey\": \"clip\", \"duration\": 5}}]}, "
                + "\"@connections\": [{\"when\": \"nodeIsFinished\", \"then\": \"done\"}]}, \"done\": {\"core:pose\": {}}}}]}", Collections.singletonMap("clip", STILL_CLIP));
        TestSubject subject = new TestSubject("arm");

        for (int frame = 0; frame < 5; frame++)
        {
            animator.update(subject, 1F);
            assertEquals("frame " + frame, Collections.singletonList("play"), animator.getCurrentNodes());
        }
        animator.update(subject, 1F);
        assertEquals(Collections.singletonList("done"), animator.getCurrentNodes());
    }

    @Test
    public void aNodeOfUntimedItemsNeverFinishes() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"defaultOnEntry\": \"loop\", "
                + "\"nodes\": {\"loop\": {\"core:pose\": {\"pose\": [{\"core:clip\": {\"animationKey\": \"clip\"}}]}, "
                + "\"@connections\": [{\"when\": \"nodeIsFinished\", \"then\": \"done\"}]}, \"done\": {\"core:pose\": {}}}}]}", Collections.singletonMap("clip", STILL_CLIP));
        TestSubject subject = new TestSubject("arm");

        for (int frame = 0; frame < 50; frame++)
        {
            animator.update(subject, 1F);
        }
        assertEquals(Collections.singletonList("loop"), animator.getCurrentNodes());
    }

    @Test
    public void anItemsTicksPassedCountsFromItsNode() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"defaultOnEntry\": \"wait\", \"nodes\": {\"wait\": {\"core:pose\": {}, "
                + "\"@connections\": [{\"when\": {\"gt\": [\"nodeTicksElapsed\", 10]}, \"then\": \"raise\"}]}, "
                + "\"raise\": {\"core:pose\": {\"pose\": [{\"@when\": {\"gt\": [\"nodeTicksElapsed\", 3]}, "
                + "\"core:axis_rotate\": {\"bone\": \"arm\", \"axis\": \"X\", \"angle\": 45}, \"@space\": \"OVERRIDE\"}]}}}}]}");
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
        KumoAnimatorState animator = TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"defaultOnEntry\": \"base\", "
                + "\"nodes\": {\"base\": {\"core:pose\": {\"pose\": [{\"core:axis_rotate\": {\"bone\": \"arm\", \"axis\": \"X\", "
                + "\"angle\": 90}, \"@space\": \"OVERRIDE\"}]}}}}, {\"defaultOnEntry\": \"relative\", "
                + "\"nodes\": {\"relative\": {\"core:pose\": {\"pose\": [{\"core:axis_rotate\": {\"bone\": \"arm\", \"axis\": \"Y\", "
                + "\"angle\": 90}, \"@space\": \"PRE\"}]}, \"@connections\": [{\"when\": {\"gt\": [\"nodeTicksElapsed\", 2]}, "
                + "\"then\": \"absolute\", \"transitionDuration\": 10, \"transitionEasing\": \"LINEAR\"}]}, "
                + "\"absolute\": {\"core:pose\": {\"pose\": [{\"core:axis_rotate\": {\"bone\": \"arm\", \"axis\": \"Z\", \"angle\": 0}, "
                + "\"@space\": \"OVERRIDE\"}]}}}}]}");
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
    public void anUnknownVariableFailsTheAnimatorBeforeItAnimates() throws MalformedKumoTemplateException
    {
        // Read only by a node the layer never reaches: still found on the first frame.
        KumoAnimatorState animator = TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"defaultOnEntry\": \"a\", \"nodes\": {\"a\": {\"core:pose\": {}}, "
                + "\"b\": {\"core:pose\": {\"pose\": [{\"core:axis_rotate\": {\"bone\": \"arm\", \"axis\": \"X\", "
                + "\"angle\": \"noSuchVariable\"}}]}}}}]}");
        try
        {
            animator.update(new TestSubject("arm"), 1F);
            fail("The animator should have failed.");
        }
        catch (MalformedKumoTemplateException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("'noSuchVariable'"));
        }
    }

    @Test
    public void anUnknownStateFailsTheAnimatorBeforeItAnimates() throws MalformedKumoTemplateException
    {
        // Entering the layer is the first frame's decision: its connections aren't checked then.
        KumoAnimatorState animator = TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"defaultOnEntry\": \"a\", \"nodes\": {\"a\": {\"core:pose\": {}, "
                + "\"@connections\": [{\"when\": \"FLYING\", \"then\": \"a\"}]}}}]}");
        try
        {
            animator.update(new TestSubject("arm"), 1F);
            fail("The animator should have failed.");
        }
        catch (MalformedKumoTemplateException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("'FLYING'"));
        }
    }

    @Test
    public void anUpdateStatementRunsBeforeTheNodePoses() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', "
                + "'nodes': {'a': {'core:pose': {'pose': [{'core:axis_rotate': {'bone': 'arm', 'axis': 'X', "
                + "'angle': {'mul': ['node.raise', 90]}}, '@space': 'OVERRIDE'}]}, '@define': {'raise': {'state': 0}}, "
                + "'@on': {'update': [{'set': ['node.raise', 0.5]}]}}}}]}");
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);
        assertRotation(TestSubject.axisAngle(1, 0, 0, 45), subject.target("arm"));
    }

    /**
     * An animator whose first layer runs the lists of nodes a and b, counting into animator
     * states, and whose second layer poses one bone per count (in degrees about X), so they can
     * be read whatever the first layer's crossfades do.
     */
    private static KumoAnimatorState counting(String nodes, String connection) throws MalformedKumoTemplateException
    {
        String count = "{'set': ['animator.%1$s', {'add': ['animator.%1$s', 1]}]}";
        String lists = "'@on': {'enter': [" + String.format(count, "enters") + "], 'update': [" + String.format(count, "frames")
                + "], 'exit': [" + String.format(count, "exits") + "]}";
        return animator("{'formatVersion': 2, '@define': {'enters': {'state': 0}, 'frames': {'state': 0}, 'exits': {'state': 0}, 'taken': {'state': 0}},"
                + " 'layers': [{'defaultOnEntry': 'a', 'nodes': {"
                + "  'a': {'core:pose': {}, " + lists + ", '@connections': [{'when': {'gt': ['nodeTicksElapsed', 2]}, 'then': 'b', " + connection
                + "     'do': [" + String.format(count, "taken") + "]}]},"
                + "  'b': {'core:pose': {}, " + nodes + "}}},"
                + " {'defaultOnEntry': 'probe', 'nodes': {'probe': {'core:pose': {'pose': ["
                + "  {'core:axis_rotate': {'bone': 'enters', 'axis': 'X', 'angle': 'animator.enters'}, '@space': 'OVERRIDE'},"
                + "  {'core:axis_rotate': {'bone': 'frames', 'axis': 'X', 'angle': 'animator.frames'}, '@space': 'OVERRIDE'},"
                + "  {'core:axis_rotate': {'bone': 'exits', 'axis': 'X', 'angle': 'animator.exits'}, '@space': 'OVERRIDE'},"
                + "  {'core:axis_rotate': {'bone': 'taken', 'axis': 'X', 'angle': 'animator.taken'}, '@space': 'OVERRIDE'}]}}}}]}");
    }

    private static float counted(TestSubject subject, String bone)
    {
        Quaternion q = subject.target(bone);
        return Math.round(Math.toDegrees(2 * Math.atan2(q.x, q.w)));
    }

    @Test
    public void aNodesListsRunWhenItIsEnteredEveryFrameAndWhenItIsLeft() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = counting("'@define': {}", "");
        TestSubject subject = new TestSubject("enters", "frames", "exits", "taken");
        for (int frame = 0; frame < 3; frame++)
        {
            animator.update(subject, 1F);
        }
        assertEquals(1, counted(subject, "enters"), 0);
        assertEquals(3, counted(subject, "frames"), 0);
        assertEquals(0, counted(subject, "exits"), 0);

        // The fourth frame takes the connection, without a crossfade: a is left at once.
        animator.update(subject, 1F);
        assertEquals(1, counted(subject, "exits"), 0);
        assertEquals(1, counted(subject, "taken"), 0);
        assertEquals(3, counted(subject, "frames"), 0);
    }

    @Test
    public void aNodeFadingOutKeepsItsUpdateListAndExitsWhenItsCrossfadeEnds() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = counting("'@define': {}", "'transitionDuration': 3,");
        TestSubject subject = new TestSubject("enters", "frames", "exits", "taken");
        for (int frame = 0; frame < 4; frame++)
        {
            animator.update(subject, 1F);
        }
        // Frame 4 took the connection: a fades out, still updated, not exited yet.
        assertEquals(4, counted(subject, "frames"), 0);
        assertEquals(0, counted(subject, "exits"), 0);
        animator.update(subject, 1F);
        animator.update(subject, 1F);
        assertEquals(6, counted(subject, "frames"), 0);
        // The crossfade ended after frame 6 was posed: a exited then.
        animator.update(subject, 1F);
        assertEquals(1, counted(subject, "exits"), 0);
        assertEquals(6, counted(subject, "frames"), 0);
    }

    @Test
    public void aTransitionsListRunsBeforeTheScopesItEnters() throws MalformedKumoTemplateException
    {
        // b notes animator.taken when it is entered: the connection's do list has run by then.
        KumoAnimatorState animator = counting("'@define': {'seen': {'state': 0}}, '@on': {'enter': [{'set': ['node.seen', 'animator.taken']}]},"
                + " 'core:pose': {'pose': [{'core:axis_rotate': {'bone': 'seen', 'axis': 'X', 'angle': 'node.seen'}, '@space': 'OVERRIDE'}]}", "");
        TestSubject subject = new TestSubject("enters", "frames", "exits", "taken", "seen");
        for (int frame = 0; frame < 4; frame++)
        {
            animator.update(subject, 1F);
        }
        assertEquals(1, counted(subject, "seen"), 0);
    }

    @Test
    public void aNodesDefinitionsStartOverOnEveryEntryAndALayersKeep() throws MalformedKumoTemplateException
    {
        // a counts its frames in node.n and the layer's in layer.n, and leaves for b and back.
        KumoAnimatorState animator = animator("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', '@define': {'n': {'state': 0}}, "
                + "'nodes': {'a': {'core:pose': {'pose': [{'core:axis_rotate': {'bone': 'node', 'axis': 'X', "
                + "'angle': 'node.n'}, '@space': 'OVERRIDE'}, {'core:axis_rotate': {'bone': 'layer', 'axis': 'X', "
                + "'angle': 'layer.n'}, '@space': 'OVERRIDE'}]}, '@define': {'n': {'state': 0}}, "
                + "'@on': {'update': [{'set': ['node.n', {'add': ['node.n', 1]}]}, {'set': ['layer.n', "
                + "{'add': ['layer.n', 1]}]}]}, '@connections': [{'when': {'ge': ['nodeTicksElapsed', 2]}, "
                + "'then': 'b'}]}, 'b': {'core:pose': {}, '@connections': [{'when': true, 'then': 'a'}]}}}]}");
        TestSubject subject = new TestSubject("node", "layer");
        animator.update(subject, 1F);
        animator.update(subject, 1F);
        animator.update(subject, 1F); // to b: nothing writes the bones, they keep their targets
        assertEquals(2, counted(subject, "node"), 0);
        animator.update(subject, 1F); // back to a, whose node.n starts over
        assertEquals(1, counted(subject, "node"), 0);
        assertEquals(3, counted(subject, "layer"), 0);
    }

    @Test
    public void theNodePhasesAndTheClocks() throws MalformedKumoTemplateException
    {
        // a crossfades into b over 3 ticks on frame 4; their update lists note what they see.
        String count = "{'@when': '%1$s', 'set': ['animator.%2$s', {'add': ['animator.%2$s', 1]}]}";
        KumoAnimatorState animator = animator("{'formatVersion': 2, '@define': {'out': {'state': 0}, 'in': {'state': 0}, 'active': {'state': 0},"
                + "   'progress': {'state': 0}, 'layer': {'state': 0}},"
                + " 'layers': [{'defaultOnEntry': 'a', 'nodes': {"
                + "  'a': {'core:pose': {}, '@on': {'update': [" + String.format(count, "nodeIsFadingOut", "out") + "]},"
                + "   '@connections': [{'when': {'gt': ['nodeTicksElapsed', 2]}, 'then': 'b', 'transitionDuration': 3}]},"
                + "  'b': {'core:pose': {}, '@on': {'update': [" + String.format(count, "nodeIsFadingIn", "in") + ", "
                + String.format(count, "nodeIsActive", "active") + ", {'set': ['animator.progress', {'mul': ['nodeFadeProgress', 30]}]},"
                + "   {'set': ['animator.layer', 'layerTicksElapsed']}]}}}},"
                + " {'defaultOnEntry': 'probe', 'nodes': {'probe': {'core:pose': {'pose': ["
                + "  {'core:axis_rotate': {'bone': 'out', 'axis': 'X', 'angle': 'animator.out'}, '@space': 'OVERRIDE'},"
                + "  {'core:axis_rotate': {'bone': 'in', 'axis': 'X', 'angle': 'animator.in'}, '@space': 'OVERRIDE'},"
                + "  {'core:axis_rotate': {'bone': 'active', 'axis': 'X', 'angle': 'animator.active'}, '@space': 'OVERRIDE'},"
                + "  {'core:axis_rotate': {'bone': 'progress', 'axis': 'X', 'angle': 'animator.progress'}, '@space': 'OVERRIDE'},"
                + "  {'core:axis_rotate': {'bone': 'layer', 'axis': 'X', 'angle': 'animator.layer'}, '@space': 'OVERRIDE'}]}}}}]}");
        TestSubject subject = new TestSubject("out", "in", "active", "progress", "layer");
        for (int frame = 0; frame < 5; frame++)
        {
            animator.update(subject, 1F);
        }
        // Frame 5, the crossfade's second: a third of the way.
        assertEquals(2, counted(subject, "out"), 0);
        assertEquals(2, counted(subject, "in"), 0);
        assertEquals(10, counted(subject, "progress"), 0);
        animator.update(subject, 1F);
        animator.update(subject, 1F);
        // Frame 7: the crossfade ended after frame 6; b is fully in.
        assertEquals(3, counted(subject, "out"), 0);
        assertEquals(3, counted(subject, "in"), 0);
        assertEquals(1, counted(subject, "active"), 0);
        assertEquals(30, counted(subject, "progress"), 0);
        assertEquals(6, counted(subject, "layer"), 0);
    }

    @Test
    public void aMachinesDefinitionsStartOverOnEveryEntry() throws MalformedKumoTemplateException
    {
        // Inside machine m, its two nodes count into machine.n; m is left for out and entered again.
        KumoAnimatorState animator = animator("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'm', 'select': [{'when': 'IN', 'then': 'm'}, "
                + "{'then': 'out'}], 'nodes': {'out': {'core:pose': {}}}, 'machines': {'m': {'defaultOnEntry': 'a', "
                + "'@define': {'n': {'state': 0}}, '@on': {'update': [{'set': ['machine.n', {'add': ['machine.n', "
                + "1]}]}]}, 'nodes': {'a': {'core:pose': {'pose': [{'core:axis_rotate': {'bone': 'n', 'axis': 'X', "
                + "'angle': 'machine.n'}, '@space': 'OVERRIDE'}]}}}}}}]}");
        TestSubject subject = new TestSubject("n");
        subject.states.put("IN", true);
        animator.update(subject, 1F);
        animator.update(subject, 1F);
        assertEquals(2, counted(subject, "n"), 0);
        subject.states.put("IN", false);
        animator.update(subject, 1F);
        subject.states.put("IN", true);
        animator.update(subject, 1F);
        assertEquals(1, counted(subject, "n"), 0);
    }

    @Test
    public void aConstantIsTakenWhenItsScopeIsCreated() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', "
                + "'nodes': {'a': {'core:pose': {'pose': [{'core:axis_rotate': {'bone': 'arm', 'axis': 'X', "
                + "'angle': 'node.start'}, '@space': 'OVERRIDE'}]}, '@define': {'start': {'constant': 'yaw'}}}}}]}");
        TestSubject subject = new TestSubject("arm");
        subject.variables.put("yaw", 20.0);
        animator.update(subject, 1F);
        subject.variables.put("yaw", 50.0);
        animator.update(subject, 1F);
        assertEquals(20, counted(subject, "arm"), 0);
    }

    @Test(expected = MalformedKumoTemplateException.class)
    public void aSetStatementCantSetALiveDefinition() throws MalformedKumoTemplateException
    {
        animator("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', '@define': {'v': {'live': 1}}, "
                + "'nodes': {'a': {'core:pose': {}, '@on': {'enter': [{'set': ['layer.v', 2]}]}}}}]}");
    }

    @Test(expected = MalformedKumoTemplateException.class)
    public void aSetStatementsValueHasTheStatesType() throws MalformedKumoTemplateException
    {
        animator("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', '@define': {'v': {'state': 0}}, "
                + "'nodes': {'a': {'core:pose': {}, '@on': {'enter': [{'set': ['layer.v', true]}]}}}}]}");
    }

    @Test
    public void aComparisonTakesExpressionsAndSeesTheNodesNamedExpressions() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"defaultOnEntry\": \"walk\", \"nodes\": {\"walk\": {\"core:pose\": {}, "
                + "\"@define\": {\"doubled\": {\"live\": {\"mul\": [\"speed\", 2]}}}, "
                + "\"@connections\": [{\"when\": {\"gt\": [\"node.doubled\", {\"add\": [\"limit\", 1]}]}, \"then\": \"run\"}]}, "
                + "\"run\": {\"core:pose\": {}}}}]}");
        TestSubject subject = new TestSubject("arm");
        subject.variables.put("limit", 2D);

        subject.variables.put("speed", 1.5D);
        animator.update(subject, 1F);
        assertEquals(Collections.singletonList("walk"), animator.getCurrentNodes());

        subject.variables.put("speed", 2D);
        animator.update(subject, 1F);
        assertEquals(Collections.singletonList("run"), animator.getCurrentNodes());
    }

    @Test
    public void decreasedWatchesAnExpression() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"defaultOnEntry\": \"a\", \"nodes\": {\"a\": {\"core:pose\": {}, "
                + "\"@connections\": [{\"when\": {\"decreased\": [{\"abs\": [\"x\"]}]}, \"then\": \"b\"}]}, "
                + "\"b\": {\"core:pose\": {}}}}]}");
        TestSubject subject = new TestSubject("arm");

        subject.variables.put("x", 1D);
        animator.update(subject, 1F);
        // x falls, but its absolute value rises.
        subject.variables.put("x", -3D);
        animator.update(subject, 1F);
        assertEquals(Collections.singletonList("a"), animator.getCurrentNodes());

        subject.variables.put("x", 2D);
        animator.update(subject, 1F);
        assertEquals(Collections.singletonList("b"), animator.getCurrentNodes());
    }

    private static final String QUARTER_TURN_CLIP = "{\"bones\": {\"arm\": {\"keyframes\": [{\"rotation\": [0.70710677, 0, 0, 0.70710677]}, {\"rotation\": [0.70710677, 0, 0, 0.70710677]}]}}}";

    @Test
    public void aWeightedClipThatReplacesBlendsFromTheLayersBelow() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"defaultOnEntry\": \"a\", "
                + "\"nodes\": {\"a\": {\"core:pose\": {\"pose\": [{\"core:axis_rotate\": {\"bone\": \"arm\", \"axis\": \"X\", "
                + "\"angle\": 90}, \"@space\": \"OVERRIDE\"}]}}}}, {\"defaultOnEntry\": \"b\", "
                + "\"nodes\": {\"b\": {\"core:pose\": {\"pose\": [{\"core:clip\": {\"animationKey\": \"clip\", \"weight\": 0.5}, "
                + "\"@space\": \"OVERRIDE\"}]}}}}]}",
                Collections.singletonMap("clip", STILL_CLIP));
        TestSubject subject = new TestSubject("arm");

        animator.update(subject, 1F);
        // Halfway between the 90 degrees below and the clip's rest rotation.
        assertRotation(TestSubject.axisAngle(1, 0, 0, 45), subject.target("arm"));
    }

    @Test
    public void aWeightedClipWithNothingBelowBlendsFromTheRestPose() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"defaultOnEntry\": \"b\", "
                + "\"nodes\": {\"b\": {\"core:pose\": {\"pose\": [{\"core:clip\": {\"animationKey\": \"clip\", \"weight\": 0.5}}]}}}}]}",
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
        TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"defaultOnEntry\": \"a\", \"mask\": {\"includedParts\": [\"arm\"]}, "
                + "\"nodes\": {\"a\": {\"core:pose\": {}}}}]}");
    }

    @Test(expected = MalformedKumoTemplateException.class)
    public void aVanillaNodeCantPose() throws MalformedKumoTemplateException
    {
        TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"defaultOnEntry\": \"a\", "
                + "\"nodes\": {\"a\": {\"core:vanilla\": {\"snapOnEnter\": [\"arm\"]}}}}]}");
    }

    @Test(expected = MalformedKumoTemplateException.class)
    public void aConditionThatIsNoExpressionIsRefused() throws MalformedKumoTemplateException
    {
        TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"defaultOnEntry\": \"a\", \"nodes\": {\"a\": {\"core:pose\": {}, "
                + "\"@connections\": [{\"when\": {}, \"then\": \"a\"}]}}}]}");
    }

    @Test(expected = MalformedKumoTemplateException.class)
    public void anAndWithoutConditionsIsRefused() throws MalformedKumoTemplateException
    {
        TestSubject.instance("{\"formatVersion\": 2, \"layers\": [{\"defaultOnEntry\": \"a\", \"nodes\": {\"a\": {\"core:pose\": {}, "
                + "\"@connections\": [{\"when\": {\"and\": []}, \"then\": \"a\"}]}}}]}");
    }

    @Test
    public void aMirroredItemReadsItsInputsAsTheyAre() throws MalformedKumoTemplateException
    {
        // The pose is reflected around the item, not its inputs: an unpaired bone turned about Y
        // turns the other way, and an unmarked item follows the input as it is.
        KumoAnimatorState animator = animator("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', 'mirror': {'@when': 'LEFT_HANDED', "
                + "'pairs': []}, 'nodes': {'a': {'core:pose': {'pose': [{'core:axis_rotate': {'bone': 'head', "
                + "'axis': 'Y', 'angle': 'yaw'}, '@mirror': true}, {'core:axis_rotate': {'bone': 'neck', 'axis': 'Y', "
                + "'angle': 'yaw'}}]}}}}]}");
        TestSubject subject = new TestSubject("head", "neck");
        subject.variables.put("yaw", 30.0);
        subject.states.put("LEFT_HANDED", true);

        animator.update(subject, 1F);

        assertRotation(TestSubject.axisAngle(0, 1, 0, -30), subject.target("head"));
        assertRotation(TestSubject.axisAngle(0, 1, 0, 30), subject.target("neck"));
    }

    /** An animator from JSON written with single quotes. */
    private static KumoAnimatorState animator(String json) throws MalformedKumoTemplateException
    {
        return TestSubject.instance(json.replace('\'', '"'));
    }

    private static List<String> frame(KumoAnimatorState animator, TestSubject subject) throws MalformedKumoTemplateException
    {
        animator.update(subject, 1F);
        return animator.getCurrentNodes();
    }

    private static final String LOCOMOTION = "{'formatVersion': 2, '@define': {'jumping': {'live': 'AIRBORNE'}}, "
            + "'layers': [{'defaultOnEntry': 'stand', 'select': [{'when': 'animator.jumping', "
            + "'then': [{'when': 'FLYING', 'then': 'fly'}, {'when': {'gt': ['fall', 10]}, 'then': 'fall'}]}, "
            + "{'when': 'STANDING_STILL', 'then': 'stand'}, {'when': {'ge': ['speed', 2]}, 'then': 'walk'}], "
            + "'nodes': {'stand': {'core:pose': {}}, 'walk': {'core:pose': {}}, 'fly': {'core:pose': {}}, "
            + "'fall': {'core:pose': {}}}}]}";

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

    private static final String SWORD = "{'formatVersion': 2, 'layers': [{'@define': {'combo': {'state': 0}, 'entered': {'state': 0}}, "
            + "'defaultOnEntry': 'idle', 'select': [{'when': 'SWORD', 'then': 'sword', "
            + "'do': [{'set': ['layer.combo', 0]}]}, {'then': 'idle'}], 'nodes': {'idle': {'core:pose': {}}}, "
            + "'machines': {'sword': {'defaultOnEntry': 'sword_idle', "
            + "'@define': {'attacked': {'live': {'decreased': ['sinceAttack']}}}, "
            + "'select': [{'when': {'ge': ['sinceAttack', 10]}, 'then': [{'when': 'STILL', 'then': 'stance', "
            + "'do': [{'set': ['layer.entered', 1]}]}, {'then': 'sword_idle'}]}], "
            + "'@connections': [{'when': {'and': ['machine.attacked', {'eq': ['layer.combo', 0]}]}, "
            + "'then': 'slash_a', 'do': [{'set': ['layer.combo', 1]}]}, {'when': {'and': ['machine.attacked', "
            + "{'eq': ['layer.combo', 1]}]}, 'then': 'slash_b', 'do': [{'set': ['layer.combo', 0]}]}], "
            + "'nodes': {'sword_idle': {'core:pose': {}}, 'stance': {'core:pose': {}}, "
            + "'slash_a': {'core:pose': {}}, 'slash_b': {'core:pose': {}}}}}}]}";

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
        KumoAnimatorState animator = animator("{'formatVersion': 2, 'layers': [{'@define': {'v': {'state': 0}}, 'defaultOnEntry': 'a', "
                + "'select': [{'when': 'GO', 'then': 'b', 'transitionDuration': 10, 'do': [{'set': ['layer.v', 1]}]}], "
                + "'nodes': {'a': {'core:pose': {'pose': [{'core:axis_rotate': {'bone': 'arm', 'axis': 'X', "
                + "'angle': 0}, '@space': 'OVERRIDE'}]}}, "
                + "'b': {'core:pose': {'pose': [{'core:axis_rotate': {'bone': 'arm', 'axis': 'X', 'angle': 90}, "
                + "'@space': 'OVERRIDE'}]}, '@connections': [{'when': {'eq': ['layer.v', 1]}, 'then': 'c'}]}, "
                + "'c': {'core:pose': {}}}}]}");
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
        KumoAnimatorState animator = animator("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'm', 'select': [{'when': 'OUT', 'then': 'out'}], "
                + "'nodes': {'out': {'core:pose': {}}}, 'machines': {'m': {'select': [{'when': 'LATE', "
                + "'then': 'late'}], 'nodes': {'early': {'core:pose': {}}, 'late': {'core:pose': {}}}}}}]}");
        TestSubject subject = new TestSubject("arm");
        subject.states.put("OUT", false);
        subject.states.put("LATE", true);
        // The layer's selector chooses nothing: its defaultOnEntry, the machine, whose selector decides.
        assertEquals(Collections.singletonList("late"), frame(animator, subject));

        animator = animator("{'formatVersion': 2, 'layers': [{'machines': {'m': {'select': [{'when': 'LATE', 'then': 'late'}], "
                + "'nodes': {'early': {'core:pose': {}}, 'late': {'core:pose': {}}}}}}]}");
        subject.states.put("LATE", false);
        // No node of its own, no defaultOnEntry: the layer's first machine; nothing chosen there: its first node.
        assertEquals(Collections.singletonList("early"), frame(animator, subject));
    }

    private static final String ENTERED_EDGE = "{'formatVersion': 2, 'layers': [{'select': [{'when': 'IN', 'then': 'm'}, {'then': 'out'}], "
            + "'nodes': {'out': {'core:pose': {}}}, 'machines': {'m': {'defaultOnEntry': 'calm', "
            + "'select': [{'when': false, 'then': 'late'}, {'when': {'decreased': ['x']}, 'then': 'hit'}], "
            + "'nodes': {'calm': {'core:pose': {}}, 'late': {'core:pose': {}}, 'hit': {'core:pose': {}}}}}}]}";

    @Test
    public void aMachinesSelectorEdgeTriggerStartsOverBeforeItChoosesOnEntry() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator(ENTERED_EDGE);
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
        KumoAnimatorState animator = animator("{'formatVersion': 2, 'layers': [{'select': [{'then': 'jump'}], 'nodes': {'jump': {'core:pose': {}, "
                + "'@connections': [{'when': {'decreased': ['height']}, 'then': 'jump'}]}}}]}");
        TestSubject subject = new TestSubject("arm");
        subject.variables.put("height", 5D);
        frame(animator, subject);
        subject.variables.put("height", 1D);
        frame(animator, subject);
        // Started over this frame: one tick in, not two.
        assertEquals(1F, animator.getLayers().get(0).getCurrentNode().getElapsedTicks(), 0F);
    }

    @Test
    public void aNamedExpressionThatRemembersIsACopyPerUse() throws MalformedKumoTemplateException
    {
        // The edge trigger is used twice; were it one instance, the second use would never see the drop.
        KumoAnimatorState animator = animator("{'formatVersion': 2, '@define': {'dropped': {'live': {'decreased': ['x']}}}, "
                + "'layers': [{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {}, "
                + "'@connections': [{'when': {'and': ['animator.dropped', 'animator.dropped']}, 'then': 'b'}]}, "
                + "'b': {'core:pose': {}}}}]}");
        TestSubject subject = new TestSubject("arm");
        subject.variables.put("x", 2D);
        assertEquals(Collections.singletonList("a"), frame(animator, subject));
        subject.variables.put("x", 1D);
        assertEquals(Collections.singletonList("b"), frame(animator, subject));
    }

    @Test
    public void anInnerNamedExpressionShadowsAnOuterOne() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'formatVersion': 2, '@define': {'go': {'live': 'NEVER'}}, "
                + "'layers': [{'@define': {'go': {'live': 'ALWAYS'}}, 'select': [{'when': 'layer.go', 'then': 'b'}], "
                + "'nodes': {'a': {'core:pose': {}}, 'b': {'core:pose': {}}}}]}");
        TestSubject subject = new TestSubject("arm");
        subject.states.put("NEVER", false);
        subject.states.put("ALWAYS", true);
        assertEquals(Collections.singletonList("b"), frame(animator, subject));
    }

    @Test(expected = MalformedKumoTemplateException.class)
    public void aNamedExpressionCantUseItself() throws MalformedKumoTemplateException
    {
        animator("{'formatVersion': 2, '@define': {'p': {'live': {'not': ['animator.q']}}, "
                + "'q': {'live': 'animator.p'}}, 'layers': [{'nodes': {'a': {'core:pose': {}}}}]}");
    }

    @Test(expected = MalformedKumoTemplateException.class)
    public void aNumberWhereAConditionGoesIsRefused() throws MalformedKumoTemplateException
    {
        animator("{'formatVersion': 2, 'layers': [{'select': [{'when': 'nope', 'then': 'a'}], "
                + "'nodes': {'a': {'core:pose': {}}}}]}");
    }

    @Test(expected = MalformedKumoTemplateException.class)
    public void aSelectorOnlyNamesItsOwnMachinesMembers() throws MalformedKumoTemplateException
    {
        animator("{'formatVersion': 2, 'layers': [{'select': [{'then': 'inner'}], 'nodes': {'a': {'core:pose': {}}}, "
                + "'machines': {'m': {'nodes': {'inner': {'core:pose': {}}}}}}]}");
    }

    @Test(expected = MalformedKumoTemplateException.class)
    public void namesAreUniqueAcrossTheLayersMachines() throws MalformedKumoTemplateException
    {
        animator("{'formatVersion': 2, 'layers': [{'nodes': {'a': {'core:pose': {}}}, "
                + "'machines': {'m': {'nodes': {'a': {'core:pose': {}}}}}}]}");
    }

}
