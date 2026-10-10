package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.math.Quaternion;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Doubles: a third type beside numbers (floats) and booleans, for positions in the world. Numbers
 * and doubles never mix; toDouble and toFloat convert, and a constant takes the precision of what
 * it is used with.
 */
public class DoubleTest
{

    /** Ten million blocks out, where a float steps by a whole block. */
    private static final double FAR = 10_000_000;

    @BeforeClass
    public static void declareNames()
    {
        TestSubject.declareNames();
    }

    private static float angle(TestSubject subject)
    {
        Quaternion q = subject.target("arm");
        return (float) Math.toDegrees(2 * Math.atan2(q.x, q.w));
    }

    /** An animator with the layer's {@code define}, {@code functions} and {@code update} list, posing 'arm' about X by {@code angle}. */
    private static String animator(String define, String functions, String update, String angle)
    {
        return "{'formatVersion': 2, 'layers': [{'@define': " + define + ", '@functions': " + functions + ", '@on': {'update': " + update + "}, "
                + "'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {'pose': [{'core:axis_rotate': "
                + "{'bone': 'arm', 'axis': 'x', 'angle': " + angle + "}, '@space': 'override'}]}}}}]}";
    }

    private static float run(String angle, double... worldX) throws MalformedKumoTemplateException
    {
        return run("{}", "{}", "[]", angle, worldX);
    }

    /** The angle after a frame at each of {@code worldX}. */
    private static float run(String define, String functions, String update, String angle, double... worldX) throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = TestSubject.instance(animator(define, functions, update, angle));
        TestSubject subject = new TestSubject("arm");
        subject.variables.put("height", 2D);
        for (double x : worldX)
        {
            subject.variables.put("worldX", x);
            animator.update(subject, 1F);
        }
        return angle(subject);
    }

    private static void expectError(String angle, String message)
    {
        expectError("{}", "{}", angle, message);
    }

    private static void expectError(String define, String functions, String angle, String message)
    {
        try
        {
            run(define, functions, "[]", angle, 0);
            fail("Loads: " + angle);
        }
        catch (MalformedKumoTemplateException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains(message));
        }
    }

    @Test
    public void arithmeticOnDoublesKeepsAPositionFarFromTheOrigin() throws MalformedKumoTemplateException
    {
        // In floats, FAR + 0.25 is FAR: the part in the block would be 0.
        assertEquals(25, run("{'mul': [{'toFloat': [{'sub': ['worldX', {'floor': ['worldX']}]}]}, 100]}", FAR + 0.25), 1e-3);
        // height 2, a number, made a double to be added.
        assertEquals(30, run("{'toFloat': [{'mul': [{'sub': [{'add': ['worldX', {'toDouble': ['height']}]}, " + FAR + "]}, 10]}]}", FAR + 1), 1e-3);
    }

    @Test
    public void aConstantTakesThePrecisionItIsUsedWithAtTheValueItWasWritten() throws MalformedKumoTemplateException
    {
        // 0.1 among doubles is the double 0.1, not the float 0.1 widened: they differ by about 1.49e-9.
        assertEquals(-1.49, run("{'toFloat': [{'mul': [{'sub': [{'add': ['worldX', 0.1]}, {'toDouble': [0.1]}]}, 1e9]}]}", 0), 1e-2);
        // Among numbers, a constant stays a number.
        assertEquals(3, run("{'add': ['height', 1]}", 0), 1e-3);
    }

    @Test
    public void numbersAndDoublesNeverMix()
    {
        expectError("{'add': ['worldX', 'height']}", "'add' mixes a double (argument 1) and a number (argument 2): convert one with toDouble or toFloat");
        expectError("{'lt': ['height', 'worldX']}", "'lt' mixes a double (argument 2) and a number (argument 1)");
        expectError("{'if': [true, 'worldX', 'height']}", "'if' mixes a double (argument 2) and a number (argument 3)");
        expectError("{'sin': ['worldX']}", "must be a number, got a double (convert it with toFloat)");
        expectError("'worldX'", "Expected a number, got a double (convert it with toFloat)");
        expectError("{'toFloat': ['height']}", "must be a double, got a number (convert it with toDouble)");
    }

    @Test
    public void comparisonsConditionsAndEdgesTakeDoubles() throws MalformedKumoTemplateException
    {
        assertEquals(10, run("{'if': [{'gt': ['worldX', " + FAR + "]}, 10, 0]}", FAR + 0.25), 1e-3);
        assertEquals(0, run("{'if': [{'gt': ['worldX', " + FAR + "]}, 10, 0]}", FAR), 1e-3);
        assertEquals(7, run("{'toFloat': [{'if': [{'eq': ['worldX', " + (FAR + 0.25) + "]}, {'toDouble': [7]}, 'worldX']}]}", FAR + 0.25), 1e-3);
        // A step back of a quarter block, which a float wouldn't see.
        assertEquals(10, run("{'if': [{'decreased': ['worldX']}, 10, 0]}", FAR + 0.5, FAR + 0.25), 1e-3);
    }

    @Test
    public void definitionsAndStatesHoldDoubles() throws MalformedKumoTemplateException
    {
        String define = "{'start': {'constant': 'worldX'}, 'last': {'state': {'toDouble': [0]}}, 'moved': {'live': {'toFloat': [{'sub': ['worldX', 'layer.start']}]}}}";
        // The constant is taken on the first frame, the state set every frame: a quarter block moved, far out.
        assertEquals(25, run(define, "{}", "[{'set': ['layer.last', 'worldX']}]", "{'mul': ['layer.moved', 100]}", FAR, FAR + 0.25), 1e-3);
        assertEquals(50, run(define, "{}", "[{'set': ['layer.last', {'add': ['worldX', 0.25]}]}]",
                "{'mul': [{'toFloat': [{'sub': ['layer.last', 'layer.start']}]}, 100]}", FAR, FAR + 0.25), 1e-3);
        // A state is the type of its initial value: {'state': 0} is a number.
        try
        {
            run("{'last': {'state': 0}}", "{}", "[{'set': ['layer.last', 'worldX']}]", "0", 0);
            fail("A number state is set to a double.");
        }
        catch (MalformedKumoTemplateException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("Expected a number, got a double"));
        }
    }

    @Test
    public void functionsTakeDoubles() throws MalformedKumoTemplateException
    {
        String inBlock = "{'inBlock': {'params': {'p': 'double'}, 'body': {'toFloat': [{'sub': ['arg.p', {'floor': ['arg.p']}]}]}}}";
        assertEquals(25, run("{}", inBlock, "[]", "{'mul': [{'layer.inBlock': ['worldX']}, 100]}", FAR + 0.25), 1e-3);
        // A constant argument becomes a double too.
        assertEquals(75, run("{}", inBlock, "[]", "{'mul': [{'layer.inBlock': [2.75]}, 100]}", 0), 1e-3);
        expectError("{}", inBlock, "{'layer.inBlock': ['height']}", "'layer.inBlock' argument 1 (p) must be a double, got a number (convert it with toDouble)");
    }

}
