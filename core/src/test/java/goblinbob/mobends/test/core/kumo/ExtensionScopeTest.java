package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.EntityTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.math.Quaternion;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/** Extensions are scopes of their own, and one that fails to load is left out. */
public class ExtensionScopeTest
{

    /** Turns {@code bone} about X by {@code angle} degrees. */
    private static AnimatorTemplate posing(String define, String bone, String angle)
    {
        return TestSubject.animator("{'formatVersion': 2, " + define + "'layers': [{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {'pose': "
                + "[{'core:axis_rotate': {'bone': '" + bone + "', 'axis': 'X', 'angle': " + angle + "}, '@space': 'OVERRIDE'}]}}}}]}");
    }

    private static float angle(TestSubject subject, String bone)
    {
        Quaternion q = subject.target(bone);
        return (float) Math.toDegrees(2 * Math.atan2(q.x, q.w));
    }

    @Test
    public void anExtensionThatFailsIsLeftOutAndTheOthersStillAnimate() throws MalformedKumoTemplateException
    {
        List<AnimatorTemplate> extensions = Arrays.asList(
                // Reads the base animator's name: an extension never sees it.
                posing("", "leg", "'animator.raise'"),
                // Reads an entity name the model doesn't declare.
                posing("", "leg", "'entity.wagging'"),
                posing("'@define': {'raise': {'constant': 20}}, ", "tail", "'animator.raise'"));
        KumoAnimatorState animator = new KumoAnimatorState(new EntityTemplate(Object.class), posing("'@define': {'raise': {'constant': 10}}, ", "arm", "'animator.raise'"),
                true, extensions, Collections.nCopies(3, true), key -> null);

        Map<Integer, MalformedKumoTemplateException> skipped = animator.getSkippedExtensions();
        assertEquals(Arrays.asList(0, 1), Arrays.asList(skipped.keySet().toArray()));
        assertTrue(skipped.get(0).getMessage(), skipped.get(0).getMessage().contains("'animator.raise'"));
        assertTrue(skipped.get(1).getMessage(), skipped.get(1).getMessage().contains("'entity.wagging'"));

        TestSubject subject = new TestSubject("arm", "leg", "tail");
        animator.update(subject, 1F);
        assertEquals(10, angle(subject, "arm"), 1e-3);
        // Each extension reads its own animator. scope.
        assertEquals(20, angle(subject, "tail"), 1e-3);
        assertEquals(0, angle(subject, "leg"), 1e-3);
    }

    @Test
    public void theAnimatorItselfFailingFailsTheWhole()
    {
        try
        {
            new KumoAnimatorState(null, posing("", "arm", "'animator.missing'"), true, Collections.singletonList(posing("", "leg", "5")),
                    Collections.singletonList(true), key -> null);
            fail("An animator reading an undeclared name loads.");
        }
        catch (MalformedKumoTemplateException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("'animator.missing'"));
        }
    }

}
