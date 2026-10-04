package goblinbob.mobends.lab;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import goblinbob.mobends.core.kumo.state.KumoContext;
import goblinbob.mobends.core.kumo.state.VariableTable;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Expressions (misc/kumo-format.md, "Expressions"): operations, errors, and named expressions in scopes. */
public class ExpressionTest
{

    private final KumoContext context = new KumoContext();
    private final VariableTable variables = new VariableTable();
    private final ExpressionScope root = ExpressionScope.root(variables);

    public ExpressionTest()
    {
        context.getLayerScope().set(variables.layerVariable("x"), 3);
        context.getLayerScope().set(variables.layerVariable("ticks"), 42);
    }

    private static JsonElement json(String text)
    {
        return JsonParser.parseString(text);
    }

    private float eval(String text, ExpressionScope scope) throws MalformedKumoTemplateException
    {
        Expression expression = Expression.compile(json(text), scope);
        variables.link();
        return expression.get(context);
    }

    private float eval(String text) throws MalformedKumoTemplateException
    {
        return eval(text, root);
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
        assertMalformed("Expected a number, got a boolean", "true");
        assertMalformed("Not an expression", "[1, 2]");
        assertMalformed("Unknown operation 'nope'", "{\"add\": [1, {\"nope\": []}]}");
    }

    private boolean test(String text) throws MalformedKumoTemplateException
    {
        Expression expression = Expression.compile(json(text), root, Expression.Type.BOOLEAN);
        variables.link();
        return expression.test(context);
    }

    private Expression condition(String text, ExpressionScope scope) throws MalformedKumoTemplateException
    {
        Expression expression = Expression.compile(json(text), scope, Expression.Type.BOOLEAN);
        variables.link();
        return expression;
    }

    private void setX(float value)
    {
        context.getLayerScope().set(variables.layerVariable("x"), value);
    }

    @Test
    void booleansComparisonsAndLogic() throws Exception
    {
        assertTrue(test("true"));
        assertTrue(test("{\"lt\": [\"x\", 4]}"));
        assertTrue(test("{\"le\": [3, \"x\"]}"));
        assertFalse(test("{\"gt\": [\"x\", 3]}"));
        assertTrue(test("{\"ge\": [\"x\", 3]}"));
        assertTrue(test("{\"eq\": [\"x\", 3]}"));
        assertTrue(test("{\"ne\": [true, false]}"), "eq and ne compare two booleans too");
        assertTrue(test("{\"and\": [true, {\"not\": [false]}, {\"lt\": [1, 2]}]}"));
        assertFalse(test("{\"and\": [true, false]}"));
        assertTrue(test("{\"or\": [false, true]}"));
        assertTrue(test("{\"and\": [true]}"), "and / or take one condition or more");
        assertEquals(7F, eval("{\"if\": [{\"gt\": [\"x\", 2]}, 7, 9]}"));
        assertFalse(test("{\"if\": [false, true, false]}"));
        assertEquals(1F, eval("{\"if\": [true, {\"if\": [false, 0, 1]}, 0]}"));
    }

    @Test
    void stepsAndAngles() throws Exception
    {
        assertEquals(0F, eval("{\"linstep\": [-1, 0, 10]}"));
        assertEquals(0.3F, eval("{\"linstep\": [3, 0, 10]}"), 1e-6F);
        assertEquals(1F, eval("{\"linstep\": [12, 0, 10]}"));
        assertEquals(1F, eval("{\"linstep\": [5, 5, 5]}"), "edges together: a step");
        assertEquals(0.5F, eval("{\"smoothstep\": [5, 0, 10]}"), 1e-6F);
        assertEquals(0.104F, eval("{\"smoothstep\": [2, 0, 10]}"), 1e-6F);
        assertEquals(-170F, eval("{\"wrapDegrees\": [190]}"));
        assertEquals(170F, eval("{\"wrapDegrees\": [-190]}"));
        assertEquals(360F, eval("{\"lerpAngle\": [350, 10, 0.5]}"), "the short way round, not wrapped: 350 to 370 is continuous");
    }

    @Test
    void typesAreCheckedWhenCompiling()
    {
        assertMalformed("'add' argument 2 (b) must be a number, got a boolean", "{\"add\": [1, true]}");
        assertMalformed("'not' argument 1 (condition) must be a boolean, got a number", "{\"if\": [{\"not\": [1]}, 1, 2]}");
        assertMalformed("'if' needs two branches of the same type", "{\"if\": [true, 1, false]}");
        assertMalformed("'eq' compares two numbers or two booleans", "{\"if\": [{\"eq\": [1, true]}, 1, 2]}");
        assertMalformed("'core:holds_item' argument 1 (hand) must be one of main_hand, off_hand, got 'left_hand'",
                "{\"if\": [{\"core:holds_item\": [\"left_hand\", \"minecraft:torch\"]}, 1, 2]}");
        assertMalformed("'core:holds_item' argument 2 (item) must be a string", "{\"if\": [{\"core:holds_item\": [\"main_hand\", 3]}, 1, 2]}");
        assertMalformed("Expected a number, got a boolean", "{\"lt\": [1, 2]}");
    }

    @Test
    void edgesTriggerOnceAndStartOverWithTheirScope() throws Exception
    {
        setX(10);
        Expression decreased = condition("{\"decreased\": [\"x\"]}", root);
        Expression rose = condition("{\"rose\": [{\"gt\": [\"x\", 5]}]}", root);
        Expression fell = condition("{\"fell\": [{\"gt\": [\"x\", 5]}]}", root);
        decreased.restart(context);
        rose.restart(context);
        fell.restart(context);

        assertFalse(decreased.test(context));
        setX(3);
        assertTrue(decreased.test(context), "lower than on the previous evaluation");
        assertFalse(decreased.test(context), "only on that frame");
        assertTrue(fell.test(context));
        assertFalse(fell.test(context));
        assertFalse(rose.test(context), "an edge is measured against the last evaluation");
        setX(8);
        assertTrue(rose.test(context));
        assertFalse(rose.test(context));

        // A restart notes the value as it is: going lower than that is a new edge, not the old one.
        setX(1);
        decreased.restart(context);
        assertFalse(decreased.test(context));
    }

    @Test
    void edgesSeeEveryFrameInsideAndOrAndIf() throws Exception
    {
        setX(10);
        // The edge is evaluated even when the first argument already decides the result.
        Expression or = condition("{\"or\": [true, {\"decreased\": [\"x\"]}]}", root);
        Expression decreasedAfter = condition("{\"decreased\": [\"x\"]}", root);
        or.restart(context);
        decreasedAfter.restart(context);
        setX(3);
        assertTrue(or.test(context));
        assertTrue(decreasedAfter.test(context));
        // Had "or" skipped its edge, the edge would now see 10 -> 3 and fire.
        Expression edgeOfOr = condition("{\"and\": [true, {\"decreased\": [\"x\"]}]}", root);
        edgeOfOr.restart(context);
        assertFalse(edgeOfOr.test(context));
    }

    @Test
    void aNamedExpressionThatRemembersIsOneMemoryPerUse() throws Exception
    {
        setX(10);
        ExpressionScope scope = root.child(declare("dropped", "{\"decreased\": [\"x\"]}"));
        Expression first = condition("\"dropped\"", scope);
        Expression second = condition("\"dropped\"", scope);
        first.restart(context);
        second.restart(context);
        setX(3);
        assertTrue(first.test(context));
        assertTrue(second.test(context), "the first use didn't take the edge from the second");
    }

    private void assertMalformed(String message, String text)
    {
        MalformedKumoTemplateException e = assertThrows(MalformedKumoTemplateException.class, () -> eval(text));
        assertTrue(e.getMessage().contains(message), "expected \"" + message + "\" in: " + e.getMessage());
    }

    @Test
    void namedExpressionsAreLexicallyScoped() throws Exception
    {
        ExpressionScope outer = root.child(declare("a", "2", "b", "{\"mul\": [\"a\", 3]}"));
        ExpressionScope inner = outer.child(declare("a", "10"));

        assertEquals(6F, eval("\"b\"", outer));
        assertEquals(10F, eval("\"a\"", inner), "an inner declaration shadows an outer one");
        assertEquals(6F, eval("\"b\"", inner), "a named expression sees the names of the scope declaring it");
        assertEquals(13F, eval("{\"add\": [\"a\", \"x\"]}", inner), "undeclared names stay variables");

        ExpressionScope shadowsVariable = root.child(declare("x", "100"));
        assertEquals(100F, eval("\"x\"", shadowsVariable), "a named expression shadows a variable of the same name");
    }

    @Test
    void namedExpressionsAreCheckedWhenDeclared()
    {
        MalformedKumoTemplateException cycle = assertThrows(MalformedKumoTemplateException.class,
                () -> root.child(declare("p", "{\"add\": [\"q\", 1]}", "q", "\"p\"")));
        assertTrue(cycle.getMessage().contains("depends on itself"), cycle.getMessage());

        MalformedKumoTemplateException unused = assertThrows(MalformedKumoTemplateException.class,
                () -> root.child(declare("unused", "{\"nope\": [1]}")));
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
                + "\"layers\": [{\"defaultOnEntry\": \"child\","
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
