package goblinbob.mobends.test.core.kumo;

import com.google.gson.JsonParser;
import goblinbob.mobends.core.kumo.MinecraftVersion;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.state.VariableTable;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/** {@code core:minecraft_version}: the running Minecraft version, compared when the expression is compiled. */
public class MinecraftVersionTest
{

    @BeforeClass
    public static void declareNames()
    {
        TestSubject.declareNames();
    }

    @After
    public void unset()
    {
        MinecraftVersion.unset();
    }

    private static Expression condition(String json) throws MalformedKumoTemplateException
    {
        return Expression.compile(new JsonParser().parse(json.replace('\'', '"')), ExpressionScope.root(new VariableTable()), Expression.Type.BOOLEAN);
    }

    private static boolean holds(String comparison, String version) throws MalformedKumoTemplateException
    {
        return condition("{'core:minecraft_version': ['" + comparison + "', '" + version + "']}").test(null);
    }

    @Test
    public void versionsCompareSegmentBySegmentAsNumbers()
    {
        assertEquals(0, MinecraftVersion.compare("1.21", "1.21.0"));
        assertTrue(MinecraftVersion.compare("1.21.1", "1.21.10") < 0);
        assertTrue(MinecraftVersion.compare("1.21.10", "26.1") < 0);
        assertTrue(MinecraftVersion.compare("1.12.2", "1.9") > 0);
        assertTrue(MinecraftVersion.compare("1.21.0.1", "1.21") > 0);
    }

    @Test
    public void everyComparison() throws MalformedKumoTemplateException
    {
        MinecraftVersion.set("1.21.1");
        assertTrue(holds(">", "1.12.2"));
        assertFalse(holds(">", "1.21.1"));
        assertTrue(holds(">=", "1.21.1"));
        assertFalse(holds(">=", "1.21.10"));
        assertTrue(holds("<", "1.21.10"));
        assertFalse(holds("<", "1.21.1"));
        assertTrue(holds("<=", "1.21.1"));
        assertFalse(holds("<=", "1.21"));
        assertTrue(holds("==", "1.21.1.0"));
        assertFalse(holds("==", "1.21"));
        assertTrue(holds("!=", "26.1"));
        assertFalse(holds("!=", "1.21.1"));

        MinecraftVersion.set("1.12.2");
        assertFalse(holds(">", "1.12.2"));
        assertTrue(holds("==", "1.12.2"));
    }

    @Test
    public void itIsComputedWhenCompiled() throws MalformedKumoTemplateException
    {
        MinecraftVersion.set("1.21.1");
        Expression version = condition("{'core:minecraft_version': ['>', '1.12.2']}");
        assertTrue(version.isConstant());
        assertSame(Expression.TRUE, version);
        // And folds with the language's operations around it.
        assertTrue(condition("{'and': [{'core:minecraft_version': ['>=', '1.21']}, {'not': [false]}]}").isConstant());
        assertFalse(condition("{'and': [{'core:minecraft_version': ['>=', '1.21']}, {'gt': ['nodeTicksElapsed', 1]}]}").isConstant());
        assertEquals(2F, Expression.compile(new JsonParser().parse("{\"if\": [{\"core:minecraft_version\": [\"<\", \"1.13\"]}, 1, 2]}"),
                ExpressionScope.root(new VariableTable()), Expression.Type.NUMBER).get(null), 0F);
    }

    @Test
    public void unsetIsAnErrorWhenCompiled()
    {
        try
        {
            condition("{'core:minecraft_version': ['>', '1.12.2']}");
            fail("compiled without a version");
        }
        catch (MalformedKumoTemplateException e)
        {
            assertTrue(e.getMessage(), e.getMessage().contains("hasn't set"));
        }
    }

    @Test
    public void malformedArgumentsAreErrors()
    {
        MinecraftVersion.set("1.21.1");
        for (String json : new String[] {
                "{'core:minecraft_version': ['>', '1.12.x']}",
                "{'core:minecraft_version': ['>', '']}",
                "{'core:minecraft_version': ['=>', '1.12']}",
                "{'core:minecraft_version': ['>']}",
                "{'core:minecraft_version': ['>', 1.12]}"})
        {
            try
            {
                condition(json);
                fail("compiled " + json);
            }
            catch (MalformedKumoTemplateException e)
            {
                // expected
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void settingAMalformedVersionIsAnError()
    {
        MinecraftVersion.set("1.21-pre1");
    }

}
