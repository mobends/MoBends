package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.kumo.api.BindArgs;
import goblinbob.mobends.core.kumo.api.BooleanEvaluator;
import goblinbob.mobends.core.kumo.api.KumoOperation;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionOperations.Kind;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import javax.annotation.Nullable;

/**
 * The mods installed alongside Mo' Bends, which the host tells the engine about at start-up
 * ({@link #set}): core can't know them. Expressions ask about them through
 * {@code {"core:mod_loaded": ["bettercombat"]}} and
 * {@code {"core:mod_version": ["bettercombat", ">=", "3.0"]}}, computed once, when the expression
 * is compiled: one asset (a compat pack, say) can then turn itself on only where a mod is there.
 * <p>
 * Mod versions are compared as Minecraft versions are (see {@link MinecraftVersion}), by the
 * dot-separated numbers they start with: {@code 3.2.2+26.3-fabric} is {@code 3.2.2}, and
 * {@code 1.12.2-14.23.5.2860} is {@code 1.12.2}. A mod whose version doesn't start with a number
 * compares with no version.
 */
public final class InstalledMods
{

    /** What the host knows of the installed mods. */
    @FunctionalInterface
    public interface Lookup
    {
        /** The version of the mod with this id, as the loader has it, or null if no such mod is installed. */
        @Nullable
        String version(String modId);
    }

    @Nullable
    private static volatile Lookup lookup;

    /**
     * {@code {"core:mod_loaded": [modId, ...]}}: whether a mod with one of these ids is installed
     * (one mod can have different ids on different loaders). Pure: computed when the expression is
     * compiled.
     */
    public static final KumoOperation LOADED = KumoOperation.named("core:mod_loaded")
            .param("modId", Kind.STRING).repeatsLast()
            .returns(Expression.Type.BOOLEAN)
            .pure()
            .selectorSafe(true)
            .bind(args -> {
                Lookup mods = lookup("core:mod_loaded");
                boolean result = false;
                for (int i = 0; i < args.count(); i++)
                {
                    result |= mods.version(modId(args.string(i), args, i)) != null;
                }
                boolean loaded = result;
                return (BooleanEvaluator) (context, values) -> loaded;
            });

    /**
     * {@code {"core:mod_version": [modId, comparison, version]}}: whether the mod is installed and
     * its version compares so with {@code version}. Pure: computed when the expression is compiled.
     */
    public static final KumoOperation VERSION = KumoOperation.named("core:mod_version")
            .param("modId", Kind.STRING)
            .choice("comparison", MinecraftVersion.COMPARISONS)
            .param("version", Kind.STRING)
            .returns(Expression.Type.BOOLEAN)
            .pure()
            .selectorSafe(true)
            .bind(args -> {
                Lookup mods = lookup("core:mod_version");
                String modId = modId(args.string(0), args, 0);
                int[] version;
                try
                {
                    version = MinecraftVersion.parse(args.string(2));
                }
                catch (IllegalArgumentException e)
                {
                    throw args.error(2, e.getMessage());
                }
                int[] installed = leadingVersion(mods.version(modId));
                boolean result = installed != null
                        && MinecraftVersion.test(args.string(1), MinecraftVersion.compare(installed, version));
                return (BooleanEvaluator) (context, values) -> result;
            });

    private InstalledMods()
    {
    }

    /** Sets what the engine asks about the installed mods; the host calls it at start-up. */
    public static void set(Lookup lookup)
    {
        InstalledMods.lookup = lookup;
    }

    /** Forgets the installed mods (for tests). */
    public static void unset()
    {
        lookup = null;
    }

    private static Lookup lookup(String operation) throws MalformedKumoTemplateException
    {
        Lookup mods = lookup;
        if (mods == null)
        {
            throw new MalformedKumoTemplateException("'" + operation + "' needs the installed mods, which the host hasn't set (InstalledMods.set).");
        }
        return mods;
    }

    private static String modId(String modId, BindArgs args, int index) throws MalformedKumoTemplateException
    {
        if (modId.isEmpty())
        {
            throw args.error(index, "must be a mod id, got ''.");
        }
        return modId;
    }

    /**
     * The dot-separated numbers a mod's version starts with ({@code 3.2.2+26.3-fabric} is
     * {@code [3, 2, 2]}), or null if it isn't installed or its version doesn't start with a number.
     */
    @Nullable
    static int[] leadingVersion(@Nullable String version)
    {
        if (version == null)
        {
            return null;
        }
        int end = 0;
        while (end < version.length())
        {
            char c = version.charAt(end);
            // A dot continues the version only between digits.
            boolean dot = c == '.' && end > 0 && end + 1 < version.length()
                    && isDigit(version.charAt(end - 1)) && isDigit(version.charAt(end + 1));
            if (!isDigit(c) && !dot)
            {
                break;
            }
            end++;
        }
        if (end == 0)
        {
            return null;
        }
        try
        {
            return MinecraftVersion.parse(version.substring(0, end));
        }
        catch (IllegalArgumentException e)
        {
            // A segment too long to be a number.
            return null;
        }
    }

    private static boolean isDigit(char c)
    {
        return c >= '0' && c <= '9';
    }

}
