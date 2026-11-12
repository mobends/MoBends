package goblinbob.mobends.test.core.kumo;

import com.google.gson.JsonParser;
import goblinbob.mobends.core.kumo.InstalledMods;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.state.VariableTable;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/** {@code core:mod_loaded} and {@code core:mod_version}: the installed mods, asked about when the expression is compiled. */
public class InstalledModsTest
{

    @BeforeClass
    public static void declareNames()
    {
        TestSubject.declareNames();
    }

    @After
    public void unset()
    {
        InstalledMods.unset();
    }

    /** Installs these mods: id, version, id, version, ... */
    private static void install(String... idsAndVersions)
    {
        Map<String, String> mods = new HashMap<>();
        for (int i = 0; i < idsAndVersions.length; i += 2)
        {
            mods.put(idsAndVersions[i], idsAndVersions[i + 1]);
        }
        InstalledMods.set(mods::get);
    }

    private static Expression condition(String json) throws MalformedKumoTemplateException
    {
        return Expression.compile(new JsonParser().parse(json.replace('\'', '"')), ExpressionScope.root(new VariableTable()), Expression.Type.BOOLEAN);
    }

    private static boolean holds(String json) throws MalformedKumoTemplateException
    {
        return condition(json).test(null);
    }

    private static boolean versionHolds(String modId, String comparison, String version) throws MalformedKumoTemplateException
    {
        return holds("{'core:mod_version': ['" + modId + "', '" + comparison + "', '" + version + "']}");
    }

    @Test
    public void loadedHoldsForAnInstalledMod() throws MalformedKumoTemplateException
    {
        install("bettercombat", "3.2.2+26.3-fabric");
        assertTrue(holds("{'core:mod_loaded': ['bettercombat']}"));
        assertFalse(holds("{'core:mod_loaded': ['geckolib']}"));
    }

    @Test
    public void loadedHoldsForAnyOfItsIds() throws MalformedKumoTemplateException
    {
        install("better_combat", "1.0");
        assertTrue(holds("{'core:mod_loaded': ['bettercombat', 'better_combat']}"));
        assertFalse(holds("{'core:mod_loaded': ['bettercombat', 'betterercombat']}"));
    }

    @Test
    public void versionComparesWhatTheModsVersionStartsWith() throws MalformedKumoTemplateException
    {
        install("bettercombat", "3.2.2+26.3-fabric", "forge", "1.12.2-14.23.5.2860", "pal", "1.2.9+mc.26.3");
        assertTrue(versionHolds("bettercombat", ">=", "3.2"));
        assertTrue(versionHolds("bettercombat", "==", "3.2.2"));
        assertFalse(versionHolds("bettercombat", ">", "3.2.2"));
        assertTrue(versionHolds("bettercombat", "<", "3.10"));
        assertTrue(versionHolds("bettercombat", "!=", "26.3"));
        assertTrue(versionHolds("forge", "==", "1.12.2"));
        assertTrue(versionHolds("pal", "<=", "1.2.9"));
        assertFalse(versionHolds("pal", "<", "1.2.9"));
    }

    @Test
    public void versionDoesntHoldForAModThatIsntInstalled() throws MalformedKumoTemplateException
    {
        install();
        for (String comparison : new String[] {">", ">=", "<", "<=", "==", "!="})
        {
            assertFalse(comparison, versionHolds("bettercombat", comparison, "1.0"));
        }
    }

    @Test
    public void versionDoesntHoldForAVersionWithoutANumber() throws MalformedKumoTemplateException
    {
        install("dev", "${version}", "dotted", ".1.2", "nightly", "nightly-1.2");
        for (String modId : new String[] {"dev", "dotted", "nightly"})
        {
            assertFalse(modId, versionHolds(modId, "!=", "1.0"));
            assertFalse(modId, versionHolds(modId, "<", "1.0"));
        }
        // But it is installed.
        assertTrue(holds("{'core:mod_loaded': ['dev']}"));
    }

    @Test
    public void aTrailingDotIsntPartOfTheVersion() throws MalformedKumoTemplateException
    {
        install("mod", "2.1.-beta", "other", "4..2");
        assertTrue(versionHolds("mod", "==", "2.1"));
        assertTrue(versionHolds("other", "==", "4"));
    }

    @Test
    public void itIsComputedWhenCompiled() throws MalformedKumoTemplateException
    {
        install("bettercombat", "3.2.2");
        Expression loaded = condition("{'core:mod_loaded': ['bettercombat']}");
        assertTrue(loaded.isConstant());
        assertSame(Expression.TRUE, loaded);
        assertSame(Expression.FALSE, condition("{'core:mod_version': ['bettercombat', '<', '3']}"));
        // And folds with the language's operations around it.
        assertTrue(condition("{'and': [{'core:mod_loaded': ['bettercombat']}, {'not': [false]}]}").isConstant());
        assertFalse(condition("{'and': [{'core:mod_loaded': ['bettercombat']}, {'gt': ['nodeTicksElapsed', 1]}]}").isConstant());
    }

    @Test
    public void unsetIsAnErrorWhenCompiled()
    {
        for (String json : new String[] {
                "{'core:mod_loaded': ['bettercombat']}",
                "{'core:mod_version': ['bettercombat', '>', '1']}"})
        {
            try
            {
                condition(json);
                fail("compiled without the installed mods: " + json);
            }
            catch (MalformedKumoTemplateException e)
            {
                assertTrue(e.getMessage(), e.getMessage().contains("hasn't set"));
            }
        }
    }

    @Test
    public void malformedArgumentsAreErrors()
    {
        install("bettercombat", "3.2.2");
        for (String json : new String[] {
                "{'core:mod_loaded': []}",
                "{'core:mod_loaded': ['']}",
                "{'core:mod_loaded': [1]}",
                "{'core:mod_version': ['bettercombat', '>', '3.x']}",
                "{'core:mod_version': ['bettercombat', '=>', '3']}",
                "{'core:mod_version': ['bettercombat', '>']}",
                "{'core:mod_version': ['', '>', '3']}",
                "{'core:mod_version': ['bettercombat', '>', 3]}"})
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

}
