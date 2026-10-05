package goblinbob.mobends.test.core.kumo;

import goblinbob.mobends.core.kumo.api.FloatSlot;
import goblinbob.mobends.core.kumo.api.KumoOperation;
import goblinbob.mobends.core.kumo.api.KumoRegistry;
import goblinbob.mobends.core.kumo.api.NumberEvaluator;
import goblinbob.mobends.core.kumo.driver.DriverRegistry;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionOperations.Kind;
import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.EntityTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.DriverItemTemplate;
import goblinbob.mobends.core.math.Quaternion;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/** Operations registered through the API: functions, entity readers and full operations with state. */
public class OperationApiTest
{

    static class Mob
    {
        float wetness = 4;
    }

    static class BigMob extends Mob
    {
    }

    static class Bird
    {
    }

    private static int doubled;

    static
    {
        KumoRegistry.registerFunction("test:double", a -> {
            doubled++;
            return a * 2;
        });
        KumoRegistry.registerEntityFloatReader("test:wetness", Mob.class, mob -> mob.wetness);
        KumoRegistry.registerEntityBooleanReader("test:is_soaked", Mob.class, mob -> mob.wetness > 3);
        // Counts the frames since the scope holding it started, by a constant step.
        KumoRegistry.registerOperation(KumoOperation.named("test:count")
                .param("step", Kind.CONSTANT)
                .returns(Expression.Type.NUMBER)
                .bind(args -> {
                    float step = args.constant(0);
                    if (step <= 0) throw args.error(0, "must be positive");
                    FloatSlot count = args.slot("count", 0);
                    return (NumberEvaluator) (context, values) -> {
                        count.set(context, count.get(context) + step);
                        return count.get(context);
                    };
                }));
    }

    /** Poses 'arm' about X by {@code angle} degrees, in the node 'a', which restarts after {@code restartAfter} ticks. */
    private static String posing(String angle, int restartAfter)
    {
        return "{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {'pose': [{'core:axis_rotate': "
                + "{'bone': 'arm', 'axis': 'x', 'angle': " + angle + "}, '@space': 'override'}]}, "
                + "'@connections': [{'when': {'ge': ['nodeTicksElapsed', " + restartAfter + "]}, 'then': 'a'}]}}}]}";
    }

    private static KumoAnimatorState animator(Class<?> entityClass, String json) throws MalformedKumoTemplateException
    {
        return new KumoAnimatorState(entityClass == null ? null : new EntityTemplate(entityClass), TestSubject.animator(json), true,
                Collections.emptyList(), Collections.emptyList(), key -> null);
    }

    /** The arm's angle about X, in degrees. */
    private static float angle(TestSubject subject)
    {
        Quaternion q = subject.target("arm");
        return (float) Math.toDegrees(2 * Math.atan2(q.x, q.w));
    }

    private static TestSubject subjectOf(Object entity)
    {
        TestSubject subject = new TestSubject("arm");
        subject.entity = entity;
        return subject;
    }

    @Test
    public void aFunctionOfConstantsIsComputedOnceWhenTheAnimatorLoads() throws MalformedKumoTemplateException
    {
        int before = doubled;
        KumoAnimatorState animator = animator(null, posing("{'test:double': [15]}", 100));
        assertEquals(1, doubled - before);
        TestSubject subject = subjectOf(null);
        for (int i = 0; i < 3; i++)
        {
            animator.update(subject, 1F);
            assertEquals(30, angle(subject), 1e-3);
        }
        assertEquals(1, doubled - before);
    }

    @Test
    public void aFunctionOfAValueIsComputedEveryFrame() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator(null, posing("{'test:double': ['nodeTicksElapsed']}", 100));
        TestSubject subject = subjectOf(null);
        animator.update(subject, 1F);
        assertEquals(0, angle(subject), 1e-3);
        animator.update(subject, 1F);
        assertEquals(2, angle(subject), 1e-3);
    }

    @Test
    public void anEntityReaderReadsTheEntityOfItsClassOrASubclass() throws MalformedKumoTemplateException
    {
        BigMob mob = new BigMob();
        KumoAnimatorState animator = animator(BigMob.class, posing("{'add': [{'test:wetness': []}, {'if': [{'test:is_soaked': []}, 10, 0]}]}", 100));
        TestSubject subject = subjectOf(mob);
        animator.update(subject, 1F);
        assertEquals(14, angle(subject), 1e-3);
        mob.wetness = 2;
        animator.update(subject, 1F);
        assertEquals(2, angle(subject), 1e-3);
    }

    @Test
    public void anEntityReaderOfAnotherClassIsItsFallbackOrAnError() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator(Bird.class, posing("{'test:wetness': [], '@fallback': 7}", 100));
        TestSubject subject = subjectOf(new Bird());
        animator.update(subject, 1F);
        assertEquals(7, angle(subject), 1e-3);

        try
        {
            animator(Bird.class, posing("{'test:wetness': []}", 100));
            fail("A reader of another class loads without a fallback.");
        }
        catch (MalformedKumoTemplateException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("'test:wetness' doesn't apply to " + Bird.class.getName()));
        }
    }

    @Test
    public void aFallbackHasTheOperationsType()
    {
        try
        {
            animator(Bird.class, posing("{'test:wetness': [], '@fallback': true}", 100));
            fail("A boolean fallback of a number operation loads.");
        }
        catch (MalformedKumoTemplateException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("its \"@fallback\" is a boolean"));
        }
    }

    @Test
    public void anOperationsStateStartsOverWithTheScopeHoldingIt() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator(null, posing("{'test:count': [10]}", 3));
        TestSubject subject = subjectOf(null);
        List<Float> angles = new ArrayList<>();
        for (int i = 0; i < 8; i++)
        {
            animator.update(subject, 1F);
            angles.add(Math.round(angle(subject) * 100) / 100F);
        }
        // Counts 10, 20, 30, ..., and from 10 again when the node restarts.
        assertEquals(10F, angles.get(0), 0);
        assertEquals(20F, angles.get(1), 0);
        int restart = angles.subList(1, angles.size()).indexOf(10F);
        assertTrue(angles.toString(), restart > 0);
        assertEquals(angles.toString(), 20F, angles.get(restart + 2), 0);
    }

    @Test
    public void aConstantArgumentIsANumberWrittenOut()
    {
        try
        {
            animator(null, posing("{'test:count': ['nodeTicksElapsed']}", 100));
            fail("An expression for a constant loads.");
        }
        catch (MalformedKumoTemplateException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("argument 1 (step) must be a number written out"));
        }
        try
        {
            animator(null, posing("{'test:count': [-1]}", 100));
            fail("A step the binder refuses loads.");
        }
        catch (MalformedKumoTemplateException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("'test:count' argument 1 (step) must be positive"));
        }
    }

    @Test
    public void aRegisteredNameIsNamespacedAndTakenOnce()
    {
        try
        {
            KumoRegistry.registerFunction("double", a -> a * 2);
            fail("A bare name registers.");
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("namespaced"));
        }
        try
        {
            KumoRegistry.registerFunction("test:double", a -> a * 3);
            fail("A name registers twice.");
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("already registered"));
        }
    }

    @Test
    public void operationsAndDriversShareOneNamespace()
    {
        try
        {
            KumoRegistry.registerFunction("core:spring", a -> a);
            fail("An operation registers under a driver's name.");
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("already registered as a driver"));
        }
        try
        {
            DriverRegistry.INSTANCE.register("test:double", (context, skeleton, template) -> null, DriverItemTemplate.class);
            fail("A driver registers under an operation's name.");
        }
        catch (IllegalArgumentException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("already registered as an operation"));
        }
    }

}
