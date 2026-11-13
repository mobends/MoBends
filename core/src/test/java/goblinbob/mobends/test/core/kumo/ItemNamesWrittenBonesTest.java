package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/** An item's {@code @smoothing} and {@code @vectorModes} name only bones it writes: any other is a load error. */
public class ItemNamesWrittenBonesTest
{

    /** A clip of the head and the arm. */
    private static final String CLIP = "{'bones': {'head': {'keyframes': [{'position': [-10, 0, 0], 'rotation': [0, 0, 0, 1]}]}, "
            + "'arm': {'keyframes': [{'position': [0, 0, 0], 'rotation': [0, 0, 0, 1]}]}}}";

    private static void load(String item) throws MalformedKumoTemplateException
    {
        TestSubject.instance(("{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {'pose': [" + item + "]}}}}]}")
                .replace('\'', '"'), Collections.singletonMap("clip", CLIP.replace('\'', '"')));
    }

    private static void assertRefused(String item, String message)
    {
        try
        {
            load(item);
            fail("loaded " + item);
        }
        catch (MalformedKumoTemplateException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains(message));
        }
    }

    @Test
    public void aClipsSmoothingOfABoneItLeavesOutIsRefused()
    {
        assertRefused("{'core:clip': {'animationKey': 'clip', 'bones': ['arm']}, '@smoothing': {'arm': 1, 'head': 1}}",
                "The @smoothing of core:clip 'clip' names 'head', which the item doesn't write");
    }

    @Test
    public void aClipsSmoothingOfABoneNotInItIsRefused()
    {
        assertRefused("{'core:clip': {'animationKey': 'clip'}, '@smoothing': {'body': 2}}", "names 'body'");
    }

    @Test
    public void aClipsExpressionSmoothingOfABoneItLeavesOutIsRefused()
    {
        assertRefused("{'core:clip': {'animationKey': 'clip', 'bones': ['arm']}, '@smoothing': {'head': {'mul': [2, 1]}}}", "names 'head'");
    }

    @Test
    public void aSmoothingGivenApartOfABoneItLeavesOutIsRefused()
    {
        assertRefused("{'core:clip': {'animationKey': 'clip', 'bones': ['arm']}, '@smoothing': {'head': {'rotation': 1, 'position': 1}}}", "names 'head'");
    }

    @Test
    public void aClipsVectorModeOfABoneItLeavesOutIsRefused()
    {
        assertRefused("{'core:clip': {'animationKey': 'clip', 'bones': ['arm']}, '@vectorModes': {'head': 'snap'}}",
                "The @vectorModes of core:clip 'clip' names 'head', whose position the item doesn't write");
    }

    @Test
    public void aVectorModeOfABoneWhoseRotationAloneTheClipWritesIsRefused()
    {
        assertRefused("{'core:clip': {'animationKey': 'clip', 'bones': ['head.rotation']}, '@vectorModes': {'head': 'snap'}}", "whose position");
    }

    @Test
    public void aRotatesSmoothingOfAnotherBoneIsRefused()
    {
        assertRefused("{'core:rotate': {'bone': 'body', 'axis': 'x', 'angle': 10}, '@smoothing': {'body': 2, 'lowerBody': 2}}",
                "The @smoothing of core:rotate 'body' names 'lowerBody'");
    }

    @Test
    public void aRotatesVectorModeIsRefused()
    {
        assertRefused("{'core:rotate': {'bone': 'head', 'axis': 'x', 'angle': 10}, '@vectorModes': {'head': 'slide'}}", "whose position");
    }

    @Test
    public void aTranslatesSmoothingOfAnotherBoneIsRefused()
    {
        assertRefused("{'core:translate': {'bone': 'root', 'y': 1}, '@smoothing': {'center': 2}}",
                "The @smoothing of core:translate 'root' names 'center'");
    }

    @Test
    public void aMisspeltBoneIsRefused()
    {
        assertRefused("{'core:clip': {'animationKey': 'clip'}, '@smoothing': {'haed': 1}}", "names 'haed'");
    }

    @Test
    public void whatTheItemWritesIsTaken() throws MalformedKumoTemplateException
    {
        load("{'core:clip': {'animationKey': 'clip', 'bones': ['arm', 'head.rotation']}, '@smoothing': {'@default': 1, 'arm': 2, 'head': {'mul': [2, 1]}}, "
                + "'@vectorModes': {'arm': 'slide'}}");
        load("{'core:clip': {'animationKey': 'clip', 'bones': ['head.rotation']}, '@smoothing': {'head': {'rotation': 1, 'position': 1}}}");
        load("{'core:translate': {'bone': 'root', 'y': 1}, '@smoothing': {'root': [null, 1, null]}, '@vectorModes': {'root': 'slide'}}");
        load("{'core:rotate': {'bone': 'head', 'axis': 'x', 'angle': 10}, '@smoothing': {'head': 1}}");
    }

}
