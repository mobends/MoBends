package goblinbob.mobends.test.core.kumo;

import com.google.gson.JsonParseException;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import org.junit.Test;

import static org.junit.Assert.*;

public class KumoSerializerTest
{

    private static final String LAYERS = "\"layers\": [{\"entryNode\": \"a\", \"nodes\": {\"a\": {}}}]";

    private static String messageOf(String json)
    {
        try
        {
            TestSubject.animator(json);
        }
        catch (JsonParseException e)
        {
            return e.getMessage();
        }
        fail("Reading should have failed: " + json);
        return null;
    }

    @Test
    public void readsTheCurrentFormat()
    {
        AnimatorTemplate template = TestSubject.animator("{\"formatVersion\": " + AnimatorTemplate.FORMAT_VERSION + ", " + LAYERS + "}");
        assertEquals("a", template.layers.get(0).entryNodeName);
        assertEquals("a", template.layers.get(0).nodes.get(0).name);
    }

    @Test
    public void refusesAnAnimatorWithoutAFormatVersion()
    {
        assertTrue(messageOf("{" + LAYERS + "}").contains("formatVersion"));
    }

    @Test
    public void refusesANewerFormat()
    {
        assertTrue(messageOf("{\"formatVersion\": " + (AnimatorTemplate.FORMAT_VERSION + 1) + ", " + LAYERS + "}").contains("update Mo' Bends"));
    }

    @Test
    public void refusesAnOlderFormat()
    {
        assertTrue(messageOf("{\"formatVersion\": 1, " + LAYERS + "}").contains("no longer reads"));
    }

    @Test
    public void malformedPiecesEndInAParseException()
    {
        String prefix = "{\"formatVersion\": 2, \"layers\": [";
        messageOf(prefix + "{\"nodes\": {\"a\": {}}}]}");                                       // no entryNode
        messageOf(prefix + "{\"entryNode\": \"a\", \"nodes\": [{}]}]}");                        // nodes not an object
        messageOf(prefix + "{\"entryNode\": \"a\", \"nodes\": {\"a\": {\"type\": \"x:y\"}}}]}"); // unknown node type
        messageOf(prefix + "{\"entryNode\": \"a\", \"mode\": \"ADDITIVE\", \"additiveSpace\": \"SIDEWAYS\", \"nodes\": {\"a\": {}}}]}");
        messageOf(prefix + "{\"entryNode\": \"a\", \"nodes\": {\"a\": {\"pose\": [{\"driver\": \"x:y\"}]}}}]}");
        messageOf(prefix + "{\"entryNode\": \"a\", \"nodes\": {\"a\": {\"pose\": [{\"weight\": 1}]}}}]}");
        messageOf(prefix + "{\"entryNode\": \"a\", \"nodes\": {\"a\": {\"damping\": {\"arm\": true}}}}]}");
        messageOf(prefix + "{\"entryNode\": \"a\", \"nodes\": {\"a\": {\"connections\": [{\"target\": \"a\", \"triggerCondition\": {}}]}}}]}");
    }

}
