package goblinbob.mobends.test.core.kumo;

import com.google.gson.JsonParseException;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.DefinitionTemplate;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;
import goblinbob.mobends.core.kumo.state.template.MachineTemplate;
import org.junit.Test;

import static org.junit.Assert.*;

public class KumoSerializerTest
{

    private static final String LAYERS = "\"layers\": [{\"defaultOnEntry\": \"a\", \"nodes\": {\"a\": {\"core:pose\": {}}}}]";

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

    private static void assertRefused(String fragment, String layer)
    {
        String message = messageOf("{\"formatVersion\": 2, \"layers\": [" + layer + "]}");
        assertTrue("expected \"" + fragment + "\" in: " + message, message.contains(fragment));
    }

    @Test
    public void malformedPiecesEndInAParseException()
    {
        String a = "\"nodes\": {\"a\": {\"core:pose\": {}}}";
        assertRefused("has to be a string", "{\"defaultOnEntry\": 1, " + a + "}");
        assertRefused("\"nodes\" has to be an object", "{\"defaultOnEntry\": \"a\", \"nodes\": [{}]}");
        assertRefused("Unknown node type: \"x:y\"", "{\"nodes\": {\"a\": {\"x:y\": {}}}}");
        assertRefused("unknown value 'SIDEWAYS'", "{\"mode\": \"ADDITIVE\", \"additiveSpace\": \"SIDEWAYS\", " + a + "}");
        assertRefused("Unknown pose item: \"x:y\"", "{\"nodes\": {\"a\": {\"core:pose\": {\"pose\": [{\"x:y\": {}}]}}}}");
        assertRefused("needs a key naming it", "{\"nodes\": {\"a\": {\"core:pose\": {\"pose\": [{\"@space\": \"PRE\"}]}}}}");
        assertRefused("damping", "{\"nodes\": {\"a\": {\"core:pose\": {\"damping\": {\"arm\": true}}}}}");
        assertRefused("has no \"then\"", "{\"select\": [{\"when\": \"x\"}], " + a + "}");
        assertRefused("a name or a list of branches", "{\"select\": [{\"then\": 1}], " + a + "}");
        assertRefused("unknown value 'x'", "{\"select\": [{\"then\": \"a\", \"transitionEasing\": \"x\"}], " + a + "}");
        assertRefused("\"machines\" has to be an object", "{\"machines\": [], " + a + "}");
        assertRefused("Unknown node type: \"x:y\"", "{\"machines\": {\"m\": {\"nodes\": {\"b\": {\"x:y\": {}}}}}, " + a + "}");
    }

    @Test
    public void everyObjectRefusesKeysItDoesntTake()
    {
        String a = "\"nodes\": {\"a\": {\"core:pose\": {}}}";
        assertRefused("A layer has an unknown key \"type\"", "{\"type\": \"KEYFRAME\", " + a + "}");
        assertRefused("A node has exactly one key that doesn't start with @", "{\"nodes\": {\"a\": {\"core:pose\": {}, \"tags\": []}}}");
        assertRefused("A node has an unknown key \"@nope\"", "{\"nodes\": {\"a\": {\"core:pose\": {}, \"@nope\": 1}}}");
        assertRefused("The node type \"core:pose\" has an unknown key \"connections\"", "{\"nodes\": {\"a\": {\"core:pose\": {\"connections\": []}}}}");
        assertRefused("A pose item has exactly one key that doesn't start with @",
                "{\"nodes\": {\"a\": {\"core:pose\": {\"pose\": [{\"core:clip\": {\"animationKey\": \"x\"}, \"space\": \"PRE\"}]}}}}");
        assertRefused("A pose item has an unknown modifier \"@weight\"",
                "{\"nodes\": {\"a\": {\"core:pose\": {\"pose\": [{\"core:clip\": {\"animationKey\": \"x\"}, \"@weight\": 1}]}}}}");
        assertRefused("The pose item \"core:clip\" has an unknown key \"space\"",
                "{\"nodes\": {\"a\": {\"core:pose\": {\"pose\": [{\"core:clip\": {\"animationKey\": \"x\", \"space\": \"PRE\"}}]}}}}");
        assertRefused("The pose item \"core:axis_rotate\" has an unknown key \"angel\"",
                "{\"nodes\": {\"a\": {\"core:pose\": {\"pose\": [{\"core:axis_rotate\": {\"bone\": \"arm\", \"axis\": \"X\", \"angel\": 1}}]}}}}");
        assertRefused("A connection has an unknown key \"target\"",
                "{\"nodes\": {\"a\": {\"core:pose\": {}, \"@connections\": [{\"target\": \"a\", \"when\": true}]}}}");
        assertRefused("A mirror rule has an unknown key \"when\"", "{\"mirror\": {\"when\": true, \"pairs\": []}, " + a + "}");
        assertRefused("A mirror rule no longer negates inputs", "{\"mirror\": {\"negate\": [\"headYaw\"]}, " + a + "}");
        assertRefused("A selector branch has an unknown key \"target\"", "{\"select\": [{\"target\": \"a\"}], " + a + "}");
        assertTrue(messageOf("{\"formatVersion\": 2, \"comment\": \"x\", " + LAYERS + "}").contains("An animator has an unknown key \"comment\""));
    }

    @Test
    public void aCommentGoesAnywhere()
    {
        AnimatorTemplate template = TestSubject.animator("{\"formatVersion\": 2, \"@comment\": \"root\", \"layers\": [{\"@comment\": \"layer\", "
                + "\"mirror\": {\"@comment\": \"rule\", \"pairs\": []}, \"select\": [{\"then\": \"a\", \"@comment\": \"branch\"}], "
                + "\"nodes\": {\"a\": {\"@comment\": \"node\", \"core:pose\": {\"@comment\": \"type\", \"pose\": [{\"@comment\": \"item\", "
                + "\"core:clip\": {\"@comment\": \"clip\", \"animationKey\": \"x\"}}]}, \"@connections\": [{\"when\": true, "
                + "\"then\": \"a\", \"@comment\": \"connection\"}]}}}]}");
        assertEquals(1, template.layers.get(0).nodes.size());
    }

    @Test
    public void readsMachinesSelectorsAndDefinitions()
    {
        AnimatorTemplate template = TestSubject.animator("{\"formatVersion\": 2, \"@define\": {\"still\": {\"live\": \"STANDING_STILL\"}}, "
                + "\"layers\": [{\"@define\": {\"v\": {\"state\": 0}}, \"select\": [{\"when\": \"animator.still\", \"then\": \"a\", "
                + "\"transitionDuration\": 2}, {\"then\": [{\"then\": \"m\", \"do\": [{\"set\": [\"layer.v\", 1]}]}]}], "
                + "\"nodes\": {\"a\": {\"core:pose\": {}}}, \"machines\": {\"m\": {\"defaultOnEntry\": \"c\", "
                + "\"nodes\": {\"b\": {\"core:pose\": {}}, \"c\": {\"core:pose\": {}}}}}}]}");
        LayerTemplate layer = template.layers.get(0);
        assertNull(layer.defaultOnEntry);
        assertEquals("animator.still", layer.select.get(0).when.json.getAsString());
        assertEquals(DefinitionTemplate.Kind.LIVE, template.define.get("still").kind);
        assertEquals(DefinitionTemplate.Kind.STATE, layer.define.get("v").kind);
        assertEquals("a", layer.select.get(0).target);
        assertEquals(2F, layer.select.get(0).transitionDuration, 0F);
        assertEquals("m", layer.select.get(1).branches.get(0).target);
        assertEquals("layer.v", layer.select.get(1).branches.get(0).run.get(0).target);
        MachineTemplate machine = layer.machines.get(0);
        assertEquals("m", machine.name);
        assertEquals("c", machine.defaultOnEntry);
        assertEquals("b", machine.nodes.get(0).name);
        assertEquals(3, layer.allNodes().size());
    }

}
