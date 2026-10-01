package goblinbob.mobends.test.core.kumo;

import com.google.gson.JsonParseException;
import goblinbob.mobends.core.kumo.state.condition.NamedCondition;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;
import goblinbob.mobends.core.kumo.state.template.MachineTemplate;
import org.junit.Test;

import static org.junit.Assert.*;

public class KumoSerializerTest
{

    private static final String LAYERS = "\"layers\": [{\"defaultOnEntry\": \"a\", \"nodes\": {\"a\": {}}}]";

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
        assertEquals("a", template.layers.get(0).defaultOnEntry);
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
        messageOf(prefix + "{\"defaultOnEntry\": 1, \"nodes\": {\"a\": {}}}]}");                    // defaultOnEntry not a name
        messageOf(prefix + "{\"defaultOnEntry\": \"a\", \"nodes\": [{}]}]}");                        // nodes not an object
        messageOf(prefix + "{\"defaultOnEntry\": \"a\", \"nodes\": {\"a\": {\"type\": \"x:y\"}}}]}"); // unknown node type
        messageOf(prefix + "{\"defaultOnEntry\": \"a\", \"mode\": \"ADDITIVE\", \"additiveSpace\": \"SIDEWAYS\", \"nodes\": {\"a\": {}}}]}");
        messageOf(prefix + "{\"defaultOnEntry\": \"a\", \"nodes\": {\"a\": {\"pose\": [{\"driver\": \"x:y\"}]}}}]}");
        messageOf(prefix + "{\"defaultOnEntry\": \"a\", \"nodes\": {\"a\": {\"pose\": [{\"weight\": 1}]}}}]}");
        messageOf(prefix + "{\"defaultOnEntry\": \"a\", \"nodes\": {\"a\": {\"damping\": {\"arm\": true}}}}]}");
        messageOf(prefix + "{\"defaultOnEntry\": \"a\", \"nodes\": {\"a\": {\"connections\": [{\"target\": \"a\", \"triggerCondition\": {}}]}}}]}");
        messageOf(prefix + "{\"select\": [{\"when\": \"x\"}], \"nodes\": {\"a\": {}}}]}");        // a branch without "then"
        messageOf(prefix + "{\"select\": [{\"then\": 1}], \"nodes\": {\"a\": {}}}]}");          // "then" neither a name nor a list
        messageOf(prefix + "{\"select\": [{\"then\": \"a\", \"transitionEasing\": \"x\"}], \"nodes\": {\"a\": {}}}]}");
        messageOf(prefix + "{\"machines\": [], \"nodes\": {\"a\": {}}}]}");                      // machines not an object
        messageOf(prefix + "{\"machines\": {\"m\": {\"nodes\": {\"b\": {\"type\": \"x:y\"}}}}}]}"); // unknown node type in a machine
    }

    @Test
    public void readsMachinesSelectorsAndNamedConditions()
    {
        AnimatorTemplate template = TestSubject.animator("{\"formatVersion\": 2, \"conditions\": {\"still\": {\"type\": \"core:state\", \"state\": \"STANDING_STILL\"}},"
                + " \"layers\": [{\"select\": [{\"when\": \"still\", \"then\": \"a\", \"transitionDuration\": 2},"
                + " {\"then\": [{\"then\": \"m\", \"set\": {\"v\": 1}}]}],"
                + " \"nodes\": {\"a\": {}}, \"machines\": {\"m\": {\"defaultOnEntry\": \"c\", \"nodes\": {\"b\": {}, \"c\": {}}}}}]}");
        LayerTemplate layer = template.layers.get(0);
        assertNull(layer.defaultOnEntry);
        assertEquals("still", ((NamedCondition.Template) layer.select.get(0).when).name);
        assertEquals("a", layer.select.get(0).target);
        assertEquals(2F, layer.select.get(0).transitionDuration, 0F);
        assertEquals("m", layer.select.get(1).branches.get(0).target);
        assertEquals(1F, layer.select.get(1).branches.get(0).set.get("v"), 0F);
        MachineTemplate machine = layer.machines.get(0);
        assertEquals("m", machine.name);
        assertEquals("c", machine.defaultOnEntry);
        assertEquals("b", machine.nodes.get(0).name);
        assertEquals(3, layer.allNodes().size());
    }

}
