package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.MinecraftVersion;
import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.math.Quaternion;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/** Functions: a call is its body written out at the call site, with the arguments in place. */
public class FunctionTest
{

    @BeforeClass
    public static void declareNames()
    {
        TestSubject.declareNames();
        MinecraftVersion.set("1.21.1");
    }

    @AfterClass
    public static void forgetVersion()
    {
        MinecraftVersion.unset();
    }

    /** The arm's angle about X, in degrees. */
    private static float angle(TestSubject subject)
    {
        Quaternion q = subject.target("arm");
        return (float) Math.toDegrees(2 * Math.atan2(q.x, q.w));
    }

    /** An animator declaring {@code functions} and posing 'arm' about X by {@code angle}. */
    private static String animator(String functions, String layerFunctions, String angle)
    {
        return "{'formatVersion': 2, '@define': {'base': {'constant': 3}}, '@functions': " + functions + ", 'layers': [{'@functions': " + layerFunctions + ", "
                + "'@define': {'step': {'state': 2}}, 'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {'pose': [{'core:rotate': "
                + "{'bone': 'arm', 'axis': 'x', 'angle': " + angle + "}, '@space': 'override'}]}}}}]}";
    }

    private static float run(String functions, String layerFunctions, String angle, double height) throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = TestSubject.instance(animator(functions, layerFunctions, angle));
        TestSubject subject = new TestSubject("arm");
        subject.variables.put("height", height);
        animator.update(subject, 1F);
        return angle(subject);
    }

    @Test
    public void aCallIsItsBodyWithTheArgumentsInPlace() throws MalformedKumoTemplateException
    {
        String scale = "{'scale': {'params': {'x': 'number', 'by': 'constant'}, 'body': {'mul': ['arg.x', 'arg.by', 'animator.base']}}}";
        // height 2, times 4, times the animator's base 3.
        assertEquals(24, run(scale, "{}", "{'animator.scale': ['height', 4]}", 2), 1e-3);
        // A layer's function reads the layer's names and calls the animator's.
        String twice = "{'twice': {'params': {'x': 'number'}, 'body': {'animator.scale': [{'add': ['arg.x', 'layer.step']}, 2]}}}";
        assertEquals(30, run(scale, twice, "{'layer.twice': ['height']}", 3), 1e-3);
    }

    @Test
    public void choicesAndBooleansAreChecked() throws MalformedKumoTemplateException
    {
        String pick = "{'pick': {'params': {'which': {'choice': ['low', 'high']}, 'on': 'boolean'}, "
                + "'body': {'if': ['arg.on', {'if': [{'eq': [1, 1]}, 10, 0]}, 0]}}}";
        assertEquals(10, run(pick, "{}", "{'animator.pick': ['high', true]}", 0), 1e-3);
        expectError(pick, "{}", "{'animator.pick': ['middle', true]}", "'animator.pick' argument 1 (which) must be one of low, high, got 'middle'");
        expectError(pick, "{}", "{'animator.pick': ['low', 1]}", "'animator.pick' argument 2 (on) must be a boolean, got a number");
        expectError(pick, "{}", "{'animator.pick': ['low']}", "'animator.pick' takes 2 arguments, not 1");
    }

    @Test
    public void aBodyIsCheckedWhereItIsDeclared()
    {
        // The animator's function can't read a layer's names, whatever the call site.
        expectError("{'bad': {'params': {}, 'body': 'layer.step'}}", "{}", "0", "In the function 'animator.bad'");
        expectError("{'bad': {'params': {'x': 'number'}, 'body': {'add': ['arg.y', 1]}}}", "{}", "0", "'animator.bad' has no parameter 'y'");
    }

    @Test
    public void aFunctionCantCallItself()
    {
        expectError("{'loop': {'params': {'x': 'number'}, 'body': {'animator.loop': ['arg.x']}}}", "{}", "0", "'animator.loop' calls itself");
    }

    @Test
    public void argumentsAreOnlyReadInABody()
    {
        expectError("{}", "{}", "'arg.x'", "'arg.x' is read outside a function");
        expectError("{}", "{}", "{'animator.nothing': []}", "Unknown function 'animator.nothing'");
    }

    @Test
    public void everyPlaceAnArgumentIsReadKeepsItsOwnMemory() throws MalformedKumoTemplateException
    {
        // The body reads its argument twice: each read is the argument written out, an edge trigger with its own memory.
        String both = "{'both': {'params': {'c': 'boolean'}, 'body': {'add': [{'if': ['arg.c', 10, 0]}, {'if': ['arg.c', 20, 0]}]}}}";
        KumoAnimatorState animator = TestSubject.instance(animator(both, "{}", "{'animator.both': [{'decreased': ['height']}]}"));
        TestSubject subject = new TestSubject("arm");
        subject.variables.put("height", 5D);
        animator.update(subject, 1F);
        subject.variables.put("height", 1D);
        animator.update(subject, 1F);
        assertEquals(30, angle(subject), 1e-3);
    }

    /** 10 where {@code condition} holds, 0 where it doesn't. */
    private static String ifThen(String condition)
    {
        return "{'if': [" + condition + ", 10, 0]}";
    }

    @Test
    public void aStringArgumentIsCheckedWhereTheFunctionIsCalled() throws MalformedKumoTemplateException
    {
        // The body loads though it writes no version: what the calls pass is checked, at each call.
        String since = "{'since': {'params': {'v': 'string'}, 'body': {'core:minecraft_version': ['>=', 'arg.v']}}}";
        assertEquals(10, run(since, "{}", ifThen("{'animator.since': ['1.20.1']}"), 0), 1e-3);
        assertEquals(0, run(since, "{}", ifThen("{'animator.since': ['1.21.2']}"), 0), 1e-3);
        expectError(since, "{}", ifThen("{'animator.since': ['nope']}"), "got 'nope'");
    }

    @Test
    public void aChoiceArgumentIsCheckedWhereTheFunctionIsCalled() throws MalformedKumoTemplateException
    {
        // Its first choice isn't a version: the body is checked with the call's value only.
        String pick = "{'pick': {'params': {'v': {'choice': ['latest', '1.20.1']}}, 'body': {'core:minecraft_version': ['>=', 'arg.v']}}}";
        assertEquals(10, run(pick, "{}", ifThen("{'animator.pick': ['1.20.1']}"), 0), 1e-3);
        expectError(pick, "{}", ifThen("{'animator.pick': ['latest']}"), "got 'latest'");
        expectError(pick, "{}", ifThen("{'animator.pick': ['1.19']}"), "'animator.pick' argument 1 (v) must be one of latest, 1.20.1, got '1.19'");
    }

    @Test
    public void aStringArgumentPassedOnIsCheckedAtTheOuterCall() throws MalformedKumoTemplateException
    {
        // f's string is g's choice: checked against g's choices where f is called.
        String functions = "{'g': {'params': {'c': {'choice': ['>=', '<']}}, 'body': {'core:minecraft_version': ['arg.c', '1.21']}}, "
                + "'f': {'params': {'s': 'string'}, 'body': {'animator.g': ['arg.s']}}, "
                + "'outer': {'params': {'v': 'string'}, 'body': {'animator.since': ['arg.v']}}, "
                + "'since': {'params': {'v': 'string'}, 'body': {'core:minecraft_version': ['>=', 'arg.v']}}}";
        assertEquals(10, run(functions, "{}", ifThen("{'animator.f': ['>=']}"), 0), 1e-3);
        expectError(functions, "{}", ifThen("{'animator.f': ['==']}"), "'animator.g' argument 1 (c) must be one of >=, <, got '=='");
        assertEquals(10, run(functions, "{}", ifThen("{'animator.outer': ['1.21']}"), 0), 1e-3);
        expectError(functions, "{}", ifThen("{'animator.outer': ['nope']}"), "got 'nope'");
    }

    @Test
    public void aStringParameterCantNameAFieldOrStandAsAnExpression()
    {
        expectError("{'read': {'params': {'name': 'string'}, 'body': {'field': ['arg.name']}}}", "{}", "0",
                "can't be given to 'field', whose type depends on the string");
        expectError("{'bad': {'params': {'s': 'string'}, 'body': {'add': ['arg.s', 1]}}}", "{}", "0",
                "A function's string parameter ('s') stands only where an operation takes a string");
        // What a file writes is checked as any string, whatever it says.
        expectError("{'odd': {'params': {'v': 'string'}, 'body': {'core:minecraft_version': ['>=', 'arg.w']}}}", "{}", "0", "got 'arg.w'");
    }

    private static void expectError(String functions, String layerFunctions, String angle, String message)
    {
        try
        {
            run(functions, layerFunctions, angle, 0);
            fail("Loads: " + angle);
        }
        catch (MalformedKumoTemplateException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains(message));
        }
    }

}
