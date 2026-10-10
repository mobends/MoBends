package goblinbob.mobends.lab;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.state.DefinitionScope;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.EntityState;
import goblinbob.mobends.core.kumo.state.KumoContext;
import goblinbob.mobends.core.kumo.state.StateLayout;
import goblinbob.mobends.core.kumo.state.VariableTable;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.DefinitionTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.util.Tween;
import goblinbob.mobends.lab.scenarios.Scenarios;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.LabBootstrap;
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

/** Expressions (misc/kumo-format.md, "Expressions"): operations, errors, and definitions read by scoped name. */
public class ExpressionTest
{

    /** A context whose entity state grows as the test compiles expressions, which take their slots. */
    private final KumoContext context = new KumoContext()
    {
        private final EntityState state = new StateLayout().newState(new Skeleton());

        @Override
        public EntityState getState()
        {
            root.getLayout().grow(state, new Skeleton());
            return state;
        }
    };
    private final VariableTable variables = new VariableTable();
    /** A layer declaring the states x = 3 and ticks = 42, which the expressions read. */
    private final DefinitionScope layer = new DefinitionScope(DefinitionScope.Kind.LAYER, "the layer");
    private final ExpressionScope root;

    public ExpressionTest() throws MalformedKumoTemplateException
    {
        // The core: operations (core:holds_item) are the mod's: registered here, not by whichever test ran first.
        LabBootstrap.ensure();
        layer.declare(define("x", "{\"state\": 3}", "ticks", "{\"state\": 42}"), true);
        root = ExpressionScope.root(variables).inside(layer);
        layer.compileIn(root);
        context.beginFrame(null, 1F);
        layer.start(context);
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
        return eval(text, root);
    }

    /** Definitions as a file declares them: name, then the definition's JSON. */
    private static Map<String, DefinitionTemplate> define(String... nameAndJson)
    {
        Map<String, DefinitionTemplate> definitions = new LinkedHashMap<>();
        for (int i = 0; i < nameAndJson.length; i += 2)
        {
            definitions.put(nameAndJson[i], KumoSerializer.INSTANCE.gson.fromJson(nameAndJson[i + 1], DefinitionTemplate.class));
        }
        return definitions;
    }

    @Test
    void numbersNamesAndOperations() throws Exception
    {
        assertEquals(2.5F, eval("2.5"));
        assertEquals(3F, eval("\"layer.x\""), "a scoped name reads the definition");
        assertEquals(6F, eval("{\"add\": [1, 2, 3]}"));
        assertEquals(5F, eval("{\"sub\": [10, 3, 2]}"), "folded left to right");
        assertEquals(2F, eval("{\"div\": [12, 2, 3]}"));
        assertEquals(12F, eval("{\"mul\": [\"layer.x\", 2, 2]}"));
        assertEquals(-1F, eval("{\"min\": [4, -1, 2]}"));
        assertEquals(4F, eval("{\"max\": [4, -1, 2]}"));
        assertEquals(1F, eval("{\"mod\": [7, 3]}"));
        assertEquals(19F, eval("{\"mod\": [-1, 20]}"), "floored: the result takes the divisor's sign");
        assertEquals(8F, eval("{\"pow\": [2, 3]}"));
        assertEquals((float) Math.atan2(1, 2), eval("{\"atan2\": [1, 2]}"));
        assertEquals(-3F, eval("{\"neg\": [\"layer.x\"]}"));
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
                + "{\"mul\": [{\"sin\": [{\"mul\": [\"layer.ticks\", 0.1]}]}, 6]},"
                + "{\"mul\": [{\"sin\": [{\"mul\": [\"layer.ticks\", 0.37]}]}, 2]},"
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
        return Expression.compile(json(text), root, Expression.Type.BOOLEAN).test(context);
    }

    private Expression condition(String text, ExpressionScope scope) throws MalformedKumoTemplateException
    {
        return Expression.compile(json(text), scope, Expression.Type.BOOLEAN);
    }

    private void setX(float value) throws MalformedKumoTemplateException
    {
        root.resolveState("layer.x", "the test").set(value, context);
    }

    @Test
    void booleansComparisonsAndLogic() throws Exception
    {
        assertTrue(test("true"));
        assertTrue(test("{\"lt\": [\"layer.x\", 4]}"));
        assertTrue(test("{\"le\": [3, \"layer.x\"]}"));
        assertFalse(test("{\"gt\": [\"layer.x\", 3]}"));
        assertTrue(test("{\"ge\": [\"layer.x\", 3]}"));
        assertTrue(test("{\"eq\": [\"layer.x\", 3]}"));
        assertTrue(test("{\"ne\": [true, false]}"), "eq and ne compare two booleans too");
        assertTrue(test("{\"and\": [true, {\"not\": [false]}, {\"lt\": [1, 2]}]}"));
        assertFalse(test("{\"and\": [true, false]}"));
        assertTrue(test("{\"or\": [false, true]}"));
        assertTrue(test("{\"and\": [true]}"), "and / or take one condition or more");
        assertEquals(7F, eval("{\"if\": [{\"gt\": [\"layer.x\", 2]}, 7, 9]}"));
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
        assertMalformed("'add' argument 2 (b) must be a number or a double, got a boolean", "{\"add\": [1, true]}");
        assertMalformed("'not' argument 1 (condition) must be a boolean, got a number", "{\"if\": [{\"not\": [1]}, 1, 2]}");
        assertMalformed("'if' needs two branches of the same type", "{\"if\": [true, 1, false]}");
        assertMalformed("'eq' compares two numbers, two doubles or two booleans", "{\"if\": [{\"eq\": [1, true]}, 1, 2]}");
        assertMalformed("'core:holds_item' argument 1 (hand) must be one of main_hand, off_hand, got 'left_hand'",
                "{\"if\": [{\"core:holds_item\": [\"left_hand\", \"minecraft:torch\"]}, 1, 2]}");
        assertMalformed("'core:holds_item' argument 2 (item) must be a string", "{\"if\": [{\"core:holds_item\": [\"main_hand\", 3]}, 1, 2]}");
        assertMalformed("Expected a number, got a boolean", "{\"lt\": [1, 2]}");
    }

    @Test
    void edgesTriggerOnceAndStartOverWithTheirScope() throws Exception
    {
        setX(10);
        Expression decreased = condition("{\"decreased\": [\"layer.x\"]}", root);
        Expression rose = condition("{\"rose\": [{\"gt\": [\"layer.x\", 5]}]}", root);
        Expression fell = condition("{\"fell\": [{\"gt\": [\"layer.x\", 5]}]}", root);
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
        Expression or = condition("{\"or\": [true, {\"decreased\": [\"layer.x\"]}]}", root);
        Expression decreasedAfter = condition("{\"decreased\": [\"layer.x\"]}", root);
        or.restart(context);
        decreasedAfter.restart(context);
        setX(3);
        assertTrue(or.test(context));
        assertTrue(decreasedAfter.test(context));
        // Had "or" skipped its edge, the edge would now see 10 -> 3 and fire.
        Expression edgeOfOr = condition("{\"and\": [true, {\"decreased\": [\"layer.x\"]}]}", root);
        edgeOfOr.restart(context);
        assertFalse(edgeOfOr.test(context));
    }

    @Test
    void aLiveDefinitionIsComputedOnceAFrame() throws Exception
    {
        // One memory, read twice in the frame: both reads see the edge, which a second
        // evaluation would have missed.
        DefinitionScope node = new DefinitionScope(DefinitionScope.Kind.NODE, "the node 'n'");
        node.declare(define("dropped", "{\"live\": {\"decreased\": [\"layer.x\"]}}"), true);
        ExpressionScope place = root.inside(node);
        node.compileIn(place);
        Expression first = condition("\"node.dropped\"", place);
        Expression second = condition("\"node.dropped\"", place);
        setX(10);
        node.start(context);
        context.beginFrame(null, 1F);
        setX(3);
        assertTrue(first.test(context));
        assertTrue(second.test(context), "the second read is the same frame's value");
        context.beginFrame(null, 1F);
        assertFalse(first.test(context), "a new frame computes it again");
    }

    private void assertMalformed(String message, String text)
    {
        MalformedKumoTemplateException e = assertThrows(MalformedKumoTemplateException.class, () -> eval(text));
        assertTrue(e.getMessage().contains(message), "expected \"" + message + "\" in: " + e.getMessage());
    }

    @Test
    void definitionsAreReadByTheirScopedName() throws Exception
    {
        DefinitionScope node = new DefinitionScope(DefinitionScope.Kind.NODE, "the node 'n'");
        node.declare(define("x", "{\"constant\": 10}", "sum", "{\"live\": {\"add\": [\"node.x\", \"layer.x\"]}}"), true);
        ExpressionScope place = root.inside(node);
        node.compileIn(place);
        node.start(context);

        assertEquals(13F, eval("\"node.sum\"", place), "node.x and layer.x are two names: nothing shadows");
        assertEquals(3F, eval("\"layer.x\"", place));
        assertMalformed("Unknown name 'layer.y': the layer declares no 'y'", "\"layer.y\"");
        assertMalformed("'node.x' is read outside any node", "\"node.x\"");
        assertMalformed("Unknown scope 'nodes'", "\"nodes.x\"");
        assertMalformed("A name is one scope and one name", "\"layer.x.y\"");
    }

    @Test
    void definitionsAreCheckedWhenDeclared()
    {
        DefinitionScope cycle = new DefinitionScope(DefinitionScope.Kind.NODE, "the node 'n'");
        MalformedKumoTemplateException loop = assertThrows(MalformedKumoTemplateException.class, () -> {
            cycle.declare(define("p", "{\"live\": {\"add\": [\"node.q\", 1]}}", "q", "{\"constant\": \"node.p\"}"), true);
            cycle.compileIn(root.inside(cycle));
        });
        assertTrue(loop.getMessage().contains("depends on itself"), loop.getMessage());

        DefinitionScope unused = new DefinitionScope(DefinitionScope.Kind.NODE, "the node 'n'");
        MalformedKumoTemplateException broken = assertThrows(MalformedKumoTemplateException.class, () -> {
            unused.declare(define("unused", "{\"live\": {\"nope\": [1]}}"), true);
            unused.compileIn(root.inside(unused));
        });
        assertTrue(broken.getMessage().contains("'node.unused'") && broken.getMessage().contains("Unknown operation 'nope'"), broken.getMessage());

        MalformedKumoTemplateException twice = assertThrows(MalformedKumoTemplateException.class,
                () -> layer.declare(define("x", "{\"state\": 1}"), true));
        assertTrue(twice.getMessage().contains("declared twice"), twice.getMessage());
    }

    @Test
    void statesChangeOnlyThroughStatements() throws Exception
    {
        assertMalformed("can't change 'layer.c': it is a constant", () -> {
            layer.declare(define("c", "{\"constant\": 1}"), true);
            layer.compileIn(root);
            root.resolveState("layer.c", "A set statement");
        });
        DefinitionScope trusted = new DefinitionScope(DefinitionScope.Kind.ANIMATOR, "the animator");
        trusted.declare(define("t", "{\"state\": 0}"), true);
        ExpressionScope fromAPack = root.inside(trusted).trusted(false);
        trusted.compileIn(fromAPack);
        assertMalformed("from a resource pack, can't change 'animator.t'", () -> fromAPack.resolveState("animator.t", "A set statement"));
    }

    @Test
    void anExtendingAnimatorSharesTheScopeOfTheOneItExtends() throws Exception
    {
        // The parent animator (a lab resource) declares animator.base = -85 and rotates the left
        // arm by it. The child reads it too, through a layer and a node definition.
        AnimatorTemplate child = KumoSerializer.INSTANCE.gson.fromJson("{\"formatVersion\": 2, \"extends\": \"mobends_test:bends/animators/expressions_parent.json\", "
                + "\"layers\": [{\"defaultOnEntry\": \"child\", \"@define\": {\"lift\": {\"live\": {\"mul\": [\"animator.base\", "
                + "0.5]}}}, \"nodes\": {\"child\": {\"core:pose\": {\"pose\": [{\"core:axis_rotate\": {\"bone\": \"rightArm\", "
                + "\"axis\": \"x\", \"angle\": \"animator.base\"}, \"@space\": \"override\"}, "
                + "{\"core:axis_rotate\": {\"bone\": \"rightLeg\", \"axis\": \"x\", \"angle\": \"node.legs\"}, "
                + "\"@space\": \"override\"}]}, \"@define\": {\"legs\": {\"live\": {\"add\": [\"layer.lift\", 1]}}}}}}]}", AnimatorTemplate.class);

        Scenario scenario = new Scenario(EntityKind.PLAYER, "expression_scopes", Scenarios.FPS, 10, (tick, in) -> {});
        List<FramePose> frames = new KumoSession(scenario, child).run().frames;
        FramePose last = frames.get(frames.size() - 1);

        assertEquals(-85, xAngle(last, "leftArm"), 1e-3, "the parent's layer");
        assertEquals(-85, xAngle(last, "rightArm"), 1e-3, "the child reads the parent's animator.base");
        assertEquals(-41.5, xAngle(last, "rightLeg"), 1e-3, "node, layer and animator definitions read each other");

        AnimatorTemplate redeclares = KumoSerializer.INSTANCE.gson.fromJson("{\"formatVersion\": 2, \"extends\": \"mobends_test:bends/animators/expressions_parent.json\", "
                + "\"@define\": {\"base\": {\"live\": -45}}, \"layers\": []}", AnimatorTemplate.class);
        Exception e = assertThrows(Exception.class, () -> new KumoSession(scenario, redeclares).run());
        assertTrue(String.valueOf(e.getMessage()).contains("declared twice") || String.valueOf(e.getCause()).contains("declared twice"), String.valueOf(e));
    }

    private interface Compiling
    {
        void run() throws Exception;
    }

    private void assertMalformed(String message, Compiling compiling)
    {
        MalformedKumoTemplateException e = assertThrows(MalformedKumoTemplateException.class, compiling::run);
        assertTrue(e.getMessage().contains(message), "expected \"" + message + "\" in: " + e.getMessage());
    }

    private static double xAngle(FramePose frame, String bone)
    {
        float[] q = frame.bones.get(bone).rt;
        return Math.toDegrees(2 * Math.atan2(q[0], q[3]));
    }

}
