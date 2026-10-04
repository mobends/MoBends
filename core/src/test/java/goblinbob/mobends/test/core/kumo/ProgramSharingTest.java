package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.KumoProgram;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.math.Quaternion;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/** One compiled program animates many entities, each with a state of its own. */
public class ProgramSharingTest
{

    @BeforeClass
    public static void declareNames()
    {
        TestSubject.declareNames();
    }

    /**
     * Something of every kind of state: a layer moving between nodes on a definition, crossfades,
     * node clocks, an accumulator stepping a node state, an edge trigger, a layer state.
     */
    private static final String ANIMATOR = "{'formatVersion': 2, '@define': {'raised': {'live': {'gt': ['height', 2]}}}, 'layers': [{"
            + "'@define': {'drops': {'state': 0}}, "
            + "'@on': {'update': [{'@when': {'decreased': ['height']}, 'set': ['layer.drops', {'add': ['layer.drops', 1]}]}]}, "
            + "'defaultOnEntry': 'low', 'select': [{'when': 'animator.raised', 'then': 'high', 'transitionDuration': 3}, {'then': 'low', 'transitionDuration': 2}], "
            + "'nodes': {"
            + "'low': {'core:pose': {'pose': [{'core:axis_rotate': {'bone': 'arm', 'axis': 'x', 'angle': {'mul': ['nodeTicksElapsed', 3]}}, '@space': 'override'}]}}, "
            + "'high': {'core:pose': {'pose': ["
            + "{'core:accumulate': {'inout': 'node.lift', 'rate': 4, 'max': 60}}, "
            + "{'core:axis_rotate': {'bone': 'arm', 'axis': 'z', 'angle': {'add': ['node.lift', {'mul': ['layer.drops', 10]}]}}, '@space': 'override'}]}, "
            + "'@define': {'lift': {'state': 0}}}}}]}";

    private static float[] pose(TestSubject subject)
    {
        Quaternion q = subject.target("arm");
        return new float[] { q.x, q.y, q.z, q.w };
    }

    private static double height(int subject, int frame)
    {
        // Two different histories: one rises early and drops, the other later.
        return subject == 0 ? (frame < 6 ? 5 : frame < 9 ? 1 : 4) : (frame < 4 ? 0 : frame < 12 ? 6 : 1);
    }

    @Test
    public void entitiesSharingAProgramAnimateAsIfEachHadItsOwn() throws MalformedKumoTemplateException
    {
        KumoProgram shared = new KumoProgram(null, TestSubject.animator(ANIMATOR), true, Collections.emptyList(), Collections.emptyList(), key -> null);
        KumoAnimatorState[] sharing = { new KumoAnimatorState(shared), new KumoAnimatorState(shared) };
        KumoAnimatorState[] alone = { TestSubject.instance(ANIMATOR), TestSubject.instance(ANIMATOR) };
        TestSubject[] sharingSubjects = { new TestSubject("arm"), new TestSubject("arm") };
        TestSubject[] aloneSubjects = { new TestSubject("arm"), new TestSubject("arm") };

        for (int frame = 0; frame < 16; frame++)
        {
            // Interleaved: each entity's frame runs between the other's.
            for (int i = 0; i < 2; i++)
            {
                sharingSubjects[i].variables.put("height", height(i, frame));
                aloneSubjects[i].variables.put("height", height(i, frame));
                sharing[i].update(sharingSubjects[i], 1F);
                alone[i].update(aloneSubjects[i], 1F);
                assertArrayEquals("entity " + i + ", frame " + frame, pose(aloneSubjects[i]), pose(sharingSubjects[i]), 0F);
                assertEquals("entity " + i + ", frame " + frame, alone[i].getCurrentNodes(), sharing[i].getCurrentNodes());
            }
        }
        // The two histories did differ.
        assertNotEquals(sharing[0].getCurrentNodes(), sharing[1].getCurrentNodes());
    }

}
