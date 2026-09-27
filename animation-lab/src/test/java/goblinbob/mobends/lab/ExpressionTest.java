package goblinbob.mobends.lab;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import goblinbob.mobends.core.kumo.state.KumoContext;
import goblinbob.mobends.core.kumo.state.VariableScope;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.util.Tween;
import goblinbob.mobends.lab.scenarios.Scenarios;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.Scenario;
import goblinbob.mobends.lab.trace.FramePose;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Expressions (misc/kumo-format.md, "Expressions"): operations, errors, and named expressions in scopes. */
public class ExpressionTest
{

    private final KumoContext context = new KumoContext();

    public ExpressionTest()
    {
        context.getLayerScope().set("x", 3);
        context.getLayerScope().set("ticks", 42);
    }

    private static JsonElement json(String text)
    {
        return JsonParser.parseString(text);
    }

    private float eval(String text, ExpressionScope scope) throws MalformedKumoTemplateException
    {
        return Expression.compile(json(text), scope).get(context);
    }

    private float eval(String text) throws MalformedKumoTemplateException
    {
        return eval(text, ExpressionScope.ROOT);
    }

    private static Map<String, ExpressionTemplate> declare(String... nameAndJson)
    {
        Map<String, ExpressionTemplate> declarations = new LinkedHashMap<>();
        for (int i = 0; i < nameAndJson.length; i += 2)
        {
            declarations.put(nameAndJson[i], new ExpressionTemplate(json(nameAndJson[i + 1])));
        }
        return declarations;
    }

    @Test
    void numbersNamesAndOperations() throws Exception
    {
        assertEquals(2.5F, eval("2.5"));
        assertEquals(3F, eval("\"x\""), "a name nothing declares is a variable");
        assertEquals(6F, eval("{\"add\": [1, 2, 3]}"));
        assertEquals(5F, eval("{\"sub\": [10, 3, 2]}"), "folded left to right");
        assertEquals(2F, eval("{\"div\": [12, 2, 3]}"));
        assertEquals(12F, eval("{\"mul\": [\"x\", 2, 2]}"));
        assertEquals(-1F, eval("{\"min\": [4, -1, 2]}"));
        assertEquals(4F, eval("{\"max\": [4, -1, 2]}"));
        assertEquals(1F, eval("{\"mod\": [7, 3]}"));
        assertEquals(19F, eval("{\"mod\": [-1, 20]}"), "floored: the result takes the divisor's sign");
        assertEquals(8F, eval("{\"pow\": [2, 3]}"));
        assertEquals((float) Math.atan2(1, 2), eval("{\"atan2\": [1, 2]}"));
        assertEquals(-3F, eval("{\"neg\": [\"x\"]}"));
        assertEquals(3F, eval("{\"abs\": [-3]}"));
        assertEquals(3F, eval("{\"sqrt\": [9]}"));
        assertEquals(1F, eval("{\"floor\": [1.7]}"));
        assertEquals(2F, eval("{\"ceil\": [1.2]}"));
        assertEquals(0F, eval("{\"clamp\": [-5, 0, 1]}"));
        assertEquals(1F, eval("{\"clamp\": [5, 0, 1]}"));
        assertEquals(15F, eval("{\"lerp\": [10, 20, 0.5]}"));
        assertEquals((float) Tween.easeInOut(0.25, 3), eval("{\"easeInOut\": [0.25, 3]}"));
    }

    @Test
    void operationsCompose() throws Exception
    {
        // Two sine waves added: the case a fixed pipeline couldn't express.
        float ticks = 42;
        float expected = (float) Math.sin(ticks * 0.1F) * 6 + (float) Math.sin(ticks * 0.37F) * 2 - 85;
        assertEquals(expected, eval("{\"add\": ["
                + "{\"mul\": [{\"sin\": [{\"mul\": [\"ticks\", 0.1]}]}, 6]},"
                + "{\"mul\": [{\"sin\": [{\"mul\": [\"ticks\", 0.37]}]}, 2]},"
                + "-85]}"));
    }

    @Test
    void mistakesAreReportedWhenCompiling()
    {
        assertMalformed("Unknown operation 'ad'", "{\"ad\": [1, 2]}");
        assertMalformed("exactly one key", "{\"add\": [1, 2], \"mul\": [3, 4]}");
        assertMalformed("have to be a list", "{\"sin\": 3}");
        assertMalformed("'sin' takes 1 argument, not 2", "{\"sin\": [1, 2]}");
        assertMalformed("'add' takes 2 or more arguments, not 1", "{\"add\": [1]}");
        assertMalformed("Not an expression", "true");
        assertMalformed("Not an expression", "[1, 2]");
        assertMalformed("Unknown operation 'nope'", "{\"add\": [1, {\"nope\": []}]}");
    }

    private void assertMalformed(String message, String text)
    {
        MalformedKumoTemplateException e = assertThrows(MalformedKumoTemplateException.class, () -> eval(text));
        assertTrue(e.getMessage().contains(message), "expected \"" + message + "\" in: " + e.getMessage());
    }

    @Test
    void namedExpressionsAreLexicallyScoped() throws Exception
    {
        ExpressionScope outer = ExpressionScope.ROOT.child(declare("a", "2", "b", "{\"mul\": [\"a\", 3]}"));
        ExpressionScope inner = outer.child(declare("a", "10"));

        assertEquals(6F, eval("\"b\"", outer));
        assertEquals(10F, eval("\"a\"", inner), "an inner declaration shadows an outer one");
        assertEquals(6F, eval("\"b\"", inner), "a named expression sees the names of the scope declaring it");
        assertEquals(13F, eval("{\"add\": [\"a\", \"x\"]}", inner), "undeclared names stay variables");

        ExpressionScope shadowsVariable = ExpressionScope.ROOT.child(declare("x", "100"));
        assertEquals(100F, eval("\"x\"", shadowsVariable), "a named expression shadows a variable of the same name");
    }

    @Test
    void namedExpressionsAreCheckedWhenDeclared()
    {
        MalformedKumoTemplateException cycle = assertThrows(MalformedKumoTemplateException.class,
                () -> ExpressionScope.ROOT.child(declare("p", "{\"add\": [\"q\", 1]}", "q", "\"p\"")));
        assertTrue(cycle.getMessage().contains("depends on itself"), cycle.getMessage());

        MalformedKumoTemplateException unused = assertThrows(MalformedKumoTemplateException.class,
                () -> ExpressionScope.ROOT.child(declare("unused", "{\"nope\": [1]}")));
        assertTrue(unused.getMessage().contains("'unused'") && unused.getMessage().contains("Unknown operation 'nope'"), unused.getMessage());
    }

    @Test
    void animatorLayerAndNodeScopes() throws Exception
    {
        // The parent animator (a lab resource) declares base = -85 and rotates the left arm by it.
        // This child shadows base with -45 for its own layers; the parent's layer keeps its own.
        AnimatorTemplate child = KumoSerializer.INSTANCE.gson.fromJson("{"
                + "\"formatVersion\": 2,"
                + "\"extends\": \"mobends_test:bends/animators/expressions_parent.json\","
                + "\"expressions\": {\"base\": -45},"
                + "\"layers\": [{\"entryNode\": \"child\","
                + "  \"expressions\": {\"lift\": {\"mul\": [\"base\", 0.5]}},"
                + "  \"nodes\": {\"child\": {\"type\": \"core:pose\","
                + "    \"expressions\": {\"legs\": {\"add\": [\"lift\", 1]}},"
                + "    \"pose\": ["
                + "      {\"driver\": \"core:axis_rotate\", \"bone\": \"rightArm\", \"axis\": \"X\", \"angle\": \"base\", \"space\": \"OVERRIDE\"},"
                + "      {\"driver\": \"core:axis_rotate\", \"bone\": \"rightLeg\", \"axis\": \"X\", \"angle\": \"legs\", \"space\": \"OVERRIDE\"}"
                + "    ]}}}]}", AnimatorTemplate.class);

        Scenario scenario = new Scenario(EntityKind.PLAYER, "expression_scopes", Scenarios.FPS, 10, (tick, in) -> {});
        List<FramePose> frames = new KumoSession(scenario, child).run().frames;
        FramePose last = frames.get(frames.size() - 1);

        assertEquals(-85, xAngle(last, "leftArm"), 1e-3, "the parent's layer uses the parent's base");
        assertEquals(-45, xAngle(last, "rightArm"), 1e-3, "the child's layers use the child's base");
        assertEquals(-21.5, xAngle(last, "rightLeg"), 1e-3, "node scope, layer scope and animator scope nest");
    }

    private static double xAngle(FramePose frame, String bone)
    {
        float[] q = frame.bones.get(bone).rt;
        return Math.toDegrees(2 * Math.atan2(q[0], q[3]));
    }

}
