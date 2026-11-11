package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** A clip's bone with no keyframes writes nothing, so the clip item's effects leave it alone. */
public class EmptyClipTrackTest
{

    private static final String EMPTY_HEAD_CLIP = "{'formatVersion': 2, 'bones': {'head': {'keyframes': []}}}";

    private static KumoAnimatorState animator(String clipItem) throws MalformedKumoTemplateException
    {
        return TestSubject.instance(("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {'pose': ["
                + "{'core:rotate': {'bone': 'head', 'axis': 'x', 'angle': 90}, '@space': 'override', '@smoothing': {'head': 10}}, "
                + clipItem + "]}}}}]}").replace('\'', '"'),
                Collections.singletonMap("clip", EMPTY_HEAD_CLIP.replace('\'', '"')));
    }

    @Test
    public void aSnappingClipLeavesABoneItHasNoKeyframesForEasingIn() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:clip': {'animationKey': 'clip'}, '@snap': true}");
        TestSubject subject = new TestSubject("head");

        animator.update(subject, 1F);
        subject.bones.get("head").update(1F);

        float w = subject.bones.get("head").getSmooth().w;
        assertTrue("on its way to 90 degrees: w = " + w, w < 1F - 1e-4F && w > 0.70710677F + 1e-4F);
    }

    @Test
    public void aSmoothingClipLeavesABoneItHasNoKeyframesAtItsOwnSmoothing() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator("{'core:clip': {'animationKey': 'clip'}, '@smoothing': {'@default': 0}}");
        TestSubject subject = new TestSubject("head");

        animator.update(subject, 1F);

        assertEquals(10F, subject.bones.get("head").getSmoothing(), 0F);
    }

}
