package goblinbob.mobends.test.core.kumo;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import goblinbob.mobends.core.kumo.option.KumoOption;
import goblinbob.mobends.core.kumo.option.KumoOptions;
import goblinbob.mobends.core.kumo.option.OptionSet;
import goblinbob.mobends.core.kumo.option.OptionTemplate;
import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.KumoProgram;
import goblinbob.mobends.core.kumo.state.template.EntityTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.math.Quaternion;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/** Options: values the user sets, declared by model definitions, type files and extension files, read as {@code option.x}. */
public class OptionTest
{

    private final KumoOptions registry = new KumoOptions();

    private static Map<String, OptionTemplate> templates(String json)
    {
        return new Gson().fromJson(json.replace('\'', '"'), new TypeToken<Map<String, OptionTemplate>>() {}.getType());
    }

    private OptionSet declare(String owner, String json)
    {
        return registry.declare(owner, templates(json));
    }

    /** Poses the bone 'arm' about X by {@code angle} degrees. */
    private static String posing(String angle)
    {
        return "{'formatVersion': 2, 'layers': [{'defaultOnEntry': 'a', 'nodes': {'a': {'core:pose': {'pose': [{'core:rotate': "
                + "{'bone': 'arm', 'axis': 'x', 'angle': " + angle + "}, '@space': 'override'}]}}}}]}";
    }

    private static EntityTemplate entity(OptionSet options)
    {
        return new EntityTemplate(Object.class, null, null, true, options);
    }

    private static KumoAnimatorState animator(EntityTemplate entity, OptionSet typeOptions, String json) throws MalformedKumoTemplateException
    {
        return new KumoAnimatorState(new KumoProgram(entity, TestSubject.animator(json), true, typeOptions,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), key -> null));
    }

    private static float angleOf(KumoAnimatorState animator) throws MalformedKumoTemplateException
    {
        TestSubject subject = new TestSubject("arm");
        subject.entity = new Object();
        animator.update(subject, 1F);
        Quaternion q = subject.target("arm");
        return (float) Math.toDegrees(2 * Math.atan2(q.x, q.w));
    }

    @Test
    public void anAnimatorReadsItsModelsOptionsEveryFrame() throws MalformedKumoTemplateException
    {
        OptionSet options = declare("x:model", "{'spin': {'default': true}, 'amount': {'default': 30, 'min': 0, 'max': 90, 'step': 5}}");
        KumoAnimatorState animator = animator(entity(options), OptionSet.EMPTY, posing("{'if': ['option.spin', 'option.amount', 0]}"));

        assertEquals(30, angleOf(animator), 1e-3);
        options.get("amount").set(47);
        assertEquals("a number moves by its step", 45, angleOf(animator), 1e-3);
        options.get("amount").set(1000);
        assertEquals("and stays in its range", 90, angleOf(animator), 1e-3);
        options.get("spin").set(0);
        assertEquals(0, angleOf(animator), 1e-3);
    }

    @Test
    public void anAnimatorReadsItsTypeFilesOptionsButNotOneDeclaredTwice() throws MalformedKumoTemplateException
    {
        OptionSet model = declare("x:model", "{'spin': {'default': true}}");
        OptionSet type = declare("x:type", "{'lean': {'default': 12, 'min': 0, 'max': 20}}");
        assertEquals(12, angleOf(animator(entity(model), type, posing("{'if': ['option.spin', 'option.lean', 0]}"))), 1e-3);

        OptionSet clash = declare("x:other_type", "{'spin': {'default': false}}");
        try
        {
            animator(entity(model), clash, posing("0"));
            fail("A type file declared an option its model definition declares.");
        }
        catch (MalformedKumoTemplateException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("both the model definition and the type file"));
        }
    }

    @Test
    public void anExtensionReadsOnlyItsOwnOptions() throws MalformedKumoTemplateException
    {
        OptionSet model = declare("x:model", "{'spin': {'default': true}}");
        OptionSet wave = declare("x:wave", "{'wave': {'default': 20, 'min': 0, 'max': 40}}");
        String reads = posing("'option.wave'");
        String readsBase = posing("{'if': ['option.spin', 10, 0]}");

        KumoProgram program = new KumoProgram(entity(model), TestSubject.animator(posing("5")), true, OptionSet.EMPTY,
                Arrays.asList(TestSubject.animator(reads), TestSubject.animator(readsBase)), Arrays.asList(true, true), Arrays.asList(wave, wave), key -> null);
        assertEquals(Collections.singleton(1), program.getSkippedExtensions().keySet());
        assertTrue(program.getSkippedExtensions().get(1).getMessage().contains("Unknown option 'option.spin'"));
        assertEquals("the extension's layer is on top", 20, angleOf(new KumoAnimatorState(program)), 1e-3);
    }

    @Test
    public void anOptionCantBeWrittenNorAnUndeclaredOneRead()
    {
        OptionSet model = declare("x:model", "{'spin': {'default': true}}");
        assertRefused(model, posing("'option.missing'"), "Unknown option 'option.missing'");
        assertRefused(model, "{'formatVersion': 2, '@on': {'update': [{'set': ['option.spin', false]}]}, 'layers': [{'defaultOnEntry': 'a', "
                + "'nodes': {'a': {'core:pose': {'pose': []}}}}]}", "can only be read");
    }

    private static void assertRefused(OptionSet model, String json, String message)
    {
        try
        {
            animator(entity(model), OptionSet.EMPTY, json);
            fail("Loaded: " + json);
        }
        catch (MalformedKumoTemplateException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains(message));
        }
    }

    @Test
    public void aValueOutlivesItsDeclarationsAndOnlyTheUsersIsSaved()
    {
        List<String> changes = new ArrayList<>();
        registry.setListener((key, value) -> changes.add(key + "=" + value));

        KumoOption first = declare("x:model", "{'amount': {'default': 10, 'min': 0, 'max': 20}}").get("amount");
        first.set(15);
        KumoOption reloaded = declare("x:model", "{'amount': {'default': 12, 'min': 0, 'max': 20}}").get("amount");
        assertEquals("a declaration after a reload has the user's value", 15, reloaded.get(), 0);
        reloaded.set(12);
        assertTrue("setting the default clears the user's value, for every declaration", first.isDefault());
        assertEquals("each of which has its own file's default", 10, first.get(), 0);
        assertNull(registry.getSaved("x:model#amount"));
        assertEquals(Arrays.asList("x:model#amount=15.0", "x:model#amount=null"), changes);

        Map<String, Double> saved = new HashMap<>();
        saved.put("x:model#amount", 3.0);
        registry.load(saved);
        assertEquals(3, reloaded.get(), 0);
        reloaded.reset();
        assertEquals(12, reloaded.get(), 0);
    }

    @Test
    public void aFileSaysWhatAnOptionIs()
    {
        assertMalformed("{'a': {'default': 'yes'}}", "a boolean or a number");
        assertMalformed("{'a': {'default': 1}}", "needs a \"min\" and a \"max\"");
        assertMalformed("{'a': {'default': 5, 'min': 0, 'max': 4}}", "outside its range");
        assertMalformed("{'a': {'default': true, 'max': 4}}", "no \"min\", \"max\" or \"step\"");
        assertMalformed("{'a': {'default': true, 'lable': 'x'}}", "takes no 'lable'");
        try
        {
            OptionTemplate.validate(templates("{'a.b': {'default': true}}"), "the test");
            fail("An option's name had a dot.");
        }
        catch (MalformedKumoTemplateException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("can't be empty or contain a dot"));
        }
    }

    private static void assertMalformed(String json, String message)
    {
        try
        {
            templates(json);
            fail("Read: " + json);
        }
        catch (JsonParseException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains(message));
        }
    }

}
