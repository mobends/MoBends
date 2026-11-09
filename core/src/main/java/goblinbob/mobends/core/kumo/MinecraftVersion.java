package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.kumo.api.BooleanEvaluator;
import goblinbob.mobends.core.kumo.api.KumoOperation;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionOperations.Kind;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import javax.annotation.Nullable;

/**
 * The Minecraft version the engine runs in, which the host sets at start-up ({@link #set}), before
 * any animator or definition loads: core can't know it. Expressions compare with it through
 * {@code {"core:minecraft_version": [">", "1.12.2"]}}, computed once, when the expression is
 * compiled: one asset can then say how it differs between versions.
 * <p>
 * Versions are dot-separated numbers ({@code 1.21.1}, {@code 26.1}), compared segment by segment as
 * numbers, a missing segment counting as 0: {@code 1.21} is {@code 1.21.0}, and
 * {@code 1.21.1 < 1.21.10 < 26.1}.
 */
public final class MinecraftVersion
{

    /** The comparisons {@code core:minecraft_version} takes, as written. */
    private static final String[] COMPARISONS = {">", ">=", "<", "<=", "==", "!="};

    @Nullable
    private static volatile String current;
    @Nullable
    private static volatile int[] currentSegments;

    /**
     * {@code {"core:minecraft_version": [comparison, version]}}: whether the running version compares
     * so with {@code version}. Pure: computed when the expression is compiled.
     */
    public static final KumoOperation OPERATION = KumoOperation.named("core:minecraft_version")
            .choice("comparison", COMPARISONS)
            .param("version", Kind.STRING)
            .returns(Expression.Type.BOOLEAN)
            .pure()
            .selectorSafe(true)
            .bind(args -> {
                int[] running = currentSegments;
                if (running == null)
                {
                    throw new MalformedKumoTemplateException("'core:minecraft_version' needs the running Minecraft version, which the host hasn't set (MinecraftVersion.set).");
                }
                int[] version;
                try
                {
                    version = parse(args.string(1));
                }
                catch (IllegalArgumentException e)
                {
                    throw args.error(1, e.getMessage());
                }
                boolean result = test(args.string(0), compare(running, version));
                return (BooleanEvaluator) (context, values) -> result;
            });

    private MinecraftVersion()
    {
    }

    /** Sets the running Minecraft version ({@code 1.21.1}); the host calls it at start-up. */
    public static void set(String version)
    {
        int[] segments;
        try
        {
            segments = parse(version);
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("The Minecraft version " + e.getMessage(), e);
        }
        current = version;
        currentSegments = segments;
    }

    /** The running Minecraft version, or null if the host hasn't set it. */
    @Nullable
    public static String get()
    {
        return current;
    }

    /** Forgets the running version (for tests). */
    public static void unset()
    {
        current = null;
        currentSegments = null;
    }

    /** Negative, zero or positive as version {@code a} is lower than, the same as or higher than {@code b}. */
    public static int compare(String a, String b)
    {
        return compare(parse(a), parse(b));
    }

    private static int compare(int[] a, int[] b)
    {
        for (int i = 0; i < Math.max(a.length, b.length); i++)
        {
            int x = i < a.length ? a[i] : 0;
            int y = i < b.length ? b[i] : 0;
            if (x != y)
            {
                return x < y ? -1 : 1;
            }
        }
        return 0;
    }

    private static boolean test(String comparison, int order)
    {
        switch (comparison)
        {
            case ">": return order > 0;
            case ">=": return order >= 0;
            case "<": return order < 0;
            case "<=": return order <= 0;
            case "==": return order == 0;
            default: return order != 0;
        }
    }

    /** The segments of a version, or an {@link IllegalArgumentException} saying what is wrong with it. */
    private static int[] parse(String version)
    {
        String[] parts = version.split("\\.", -1);
        int[] segments = new int[parts.length];
        for (int i = 0; i < parts.length; i++)
        {
            String part = parts[i];
            if (part.isEmpty() || part.length() > 9 || !part.chars().allMatch(c -> c >= '0' && c <= '9'))
            {
                throw new IllegalArgumentException("must be a version of dot-separated numbers, such as 1.21.1, got '" + version + "'.");
            }
            segments[i] = Integer.parseInt(part);
        }
        return segments;
    }

}
