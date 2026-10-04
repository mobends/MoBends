package goblinbob.mobends.test.core.kumo;

import com.google.gson.reflect.TypeToken;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.DefinitionTemplate;
import goblinbob.mobends.core.kumo.state.template.EntityTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.OnTemplate;
import goblinbob.mobends.core.math.Quaternion;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

/** The entity scope a model definition declares, and {@code field}, which only it may use. */
public class EntityScopeTest
{

    /** An entity, as reflection sees it. */
    @SuppressWarnings("unused")
    static class Mob
    {
        float wingRotation = 30;
        int attackTimer;
        boolean angry;
        Mob rider;
    }

    static class BigMob extends Mob
    {
    }

    /** Poses the bone 'arm' about X by {@code angle} degrees. */
    private static String posing(String angle)
    {
        return "{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {'pose': [{'core:axis_rotate': "
                + "{'bone': 'arm', 'axis': 'x', 'angle': " + angle + "}, '@space': 'override'}]}}}}]}";
    }

    private static EntityTemplate entity(Class<?> type, String define, String on, boolean trusted)
    {
        Map<String, DefinitionTemplate> definitions = KumoSerializer.INSTANCE.gson.fromJson(define, new TypeToken<Map<String, DefinitionTemplate>>() {}.getType());
        OnTemplate lists = on == null ? null : KumoSerializer.INSTANCE.gson.fromJson(on, OnTemplate.class);
        return new EntityTemplate(type, definitions, lists, trusted);
    }

    private static KumoAnimatorState animator(EntityTemplate entity, String json) throws MalformedKumoTemplateException
    {
        return new KumoAnimatorState(entity, TestSubject.animator(json), true, Collections.emptyList(), Collections.emptyList(), key -> null);
    }

    private static void assertAngle(float degrees, TestSubject subject)
    {
        Quaternion expected = TestSubject.axisAngle(1, 0, 0, degrees);
        Quaternion actual = subject.target("arm");
        float dot = Math.abs(expected.x * actual.x + expected.y * actual.y + expected.z * actual.z + expected.w * actual.w);
        assertEquals("rotation " + actual.x + ", " + actual.y + ", " + actual.z + ", " + actual.w, 1F, dot, 1e-4F);
    }

    private static TestSubject subjectOf(Object entity)
    {
        TestSubject subject = new TestSubject("arm");
        subject.entity = entity;
        return subject;
    }

    @Test
    public void anAnimatorReadsTheEntitysLiveDefinitionsEveryFrame() throws MalformedKumoTemplateException
    {
        Mob mob = new Mob();
        KumoAnimatorState animator = animator(entity(Mob.class, "{'wing': {'live': {'mul': [{'field': ['wingRotation']}, 2]}}}", null, true),
                posing("'entity.wing'"));
        TestSubject subject = subjectOf(mob);

        animator.update(subject, 1F);
        assertAngle(60, subject);
        mob.wingRotation = 10;
        animator.update(subject, 1F);
        assertAngle(20, subject);
    }

    @Test
    public void aFieldIsFoundOnASuperclassAndAlongAPath() throws MalformedKumoTemplateException
    {
        BigMob mob = new BigMob();
        mob.rider = new Mob();
        mob.rider.attackTimer = 7;
        KumoAnimatorState animator = animator(entity(BigMob.class, "{'riderTimer': {'live': {'field': ['rider', 'attackTimer'], '@fallback': -1}}}", null, true),
                posing("'entity.riderTimer'"));
        TestSubject subject = subjectOf(mob);

        animator.update(subject, 1F);
        assertAngle(7, subject);
        // A null step: the fallback.
        mob.rider = null;
        animator.update(subject, 1F);
        assertAngle(-1, subject);
    }

    @Test
    public void aBooleanFieldIsABooleanAndExistsTellsWhetherAPathIsThere() throws MalformedKumoTemplateException
    {
        Mob mob = new Mob();
        KumoAnimatorState animator = animator(entity(Mob.class, "{'angry': {'live': {'field': ['angry']}}, "
                + "'ridden': {'live': {'exists': ['rider', 'angry']}}, 'flies': {'constant': {'exists': ['wingSpan']}}}", null, true),
                posing("{'add': [{'if': ['entity.angry', 10, 0]}, {'if': ['entity.ridden', 20, 0]}, {'if': ['entity.flies', 40, 0]}]}"));
        TestSubject subject = subjectOf(mob);

        animator.update(subject, 1F);
        assertAngle(0, subject);
        mob.angry = true;
        mob.rider = new Mob();
        animator.update(subject, 1F);
        assertAngle(30, subject);
    }

    @Test
    public void aMissingFieldIsItsFallbackOrAnError() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator(entity(Mob.class, "{'span': {'constant': {'field': ['wingSpan'], '@fallback': {'field': ['attackTimer']}}}}", null, true),
                posing("'entity.span'"));
        Mob mob = new Mob();
        mob.attackTimer = 12;
        TestSubject subject = subjectOf(mob);
        animator.update(subject, 1F);
        assertAngle(12, subject);

        try
        {
            animator(entity(Mob.class, "{'span': {'constant': {'field': ['wingSpan']}}}", null, true), posing("'entity.span'"));
            fail("A missing field without a fallback loads.");
        }
        catch (MalformedKumoTemplateException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("no field 'wingSpan'"));
        }
    }

    @Test
    public void aFallbackHasTheFieldsType() throws MalformedKumoTemplateException
    {
        try
        {
            animator(entity(Mob.class, "{'angry': {'live': {'field': ['angry'], '@fallback': 0}}}", null, true), posing("0"));
            fail("A number fallback of a boolean field loads.");
        }
        catch (MalformedKumoTemplateException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("its \"@fallback\" is a number"));
        }
    }

    @Test
    public void onlyAModelDefinitionReadsFields()
    {
        try
        {
            animator(null, posing("{'field': ['wingRotation']}"));
            fail("An animator reads a field.");
        }
        catch (MalformedKumoTemplateException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("only in a model definition"));
        }
    }

    @Test
    public void onlyAnOperationThatTakesAFallbackHasOne()
    {
        try
        {
            animator(null, posing("{'abs': [-3], '@fallback': 1}"));
            fail("'abs' takes a fallback.");
        }
        catch (MalformedKumoTemplateException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("takes no \"@fallback\""));
        }
    }

    @Test
    public void anOperationMayCarryAComment() throws MalformedKumoTemplateException
    {
        KumoAnimatorState animator = animator(null, posing("{'abs': [-30], '@comment': 'why not'}"));
        TestSubject subject = subjectOf(null);
        animator.update(subject, 1F);
        assertAngle(30, subject);
    }

    @Test
    public void theEntitysStateIsKeptAndItsListsRunFirst() throws MalformedKumoTemplateException
    {
        // The entity counts frames; the animator's update list reads it after the entity's ran.
        KumoAnimatorState animator = animator(entity(Mob.class, "{'frames': {'state': 0}}",
                "{'update': [{'set': ['entity.frames', {'add': ['entity.frames', 1]}]}]}", true),
                "{'formatVersion': 2, '@define': {'seen': {'state': -1}}, '@on': {'update': [{'set': ['animator.seen', 'entity.frames']}]}, "
                        + "'layers': [{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {'pose': [{'core:axis_rotate': "
                        + "{'bone': 'arm', 'axis': 'x', 'angle': {'mul': ['animator.seen', 10]}}, '@space': 'override'}]}}}}]}");
        TestSubject subject = subjectOf(new Mob());

        animator.update(subject, 1F);
        assertAngle(-10, subject);
        animator.update(subject, 1F);
        assertAngle(10, subject);
        animator.update(subject, 1F);
        assertAngle(20, subject);
    }

    @Test
    public void anUntrustedAnimatorCantSetATrustedEntitysState()
    {
        try
        {
            new KumoAnimatorState(entity(Mob.class, "{'mood': {'state': 0}}", null, true),
                    TestSubject.animator("{'formatVersion': 2, '@on': {'update': [{'set': ['entity.mood', 1]}]}, "
                            + "'layers': [{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {}}}}]}"),
                    false, Collections.emptyList(), Collections.emptyList(), key -> null);
            fail("An untrusted animator sets a trusted entity state.");
        }
        catch (MalformedKumoTemplateException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("can't change 'entity.mood'"));
        }
    }

    @Test
    public void entityNamesNeedAModelDefinition()
    {
        try
        {
            animator(null, posing("'entity.wing'"));
            fail("entity.wing is read without an entity scope.");
        }
        catch (MalformedKumoTemplateException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("outside any entity"));
        }
    }

}
