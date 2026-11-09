package goblinbob.mobends.core.util;

/**
 * A pattern with a {@code *} at either end, or both: {@code text} matches exactly, {@code *text}
 * what ends with it, {@code text*} what starts with it, {@code *text*} what contains it, {@code *}
 * anything.
 */
public final class WildcardPattern {
    private final String pattern;

    public WildcardPattern(String pattern) {
        this.pattern = pattern;
    }

    public boolean matches(String check) {
        boolean startsWithWildcard = pattern.startsWith("*");
        boolean endsWithWildcard = pattern.endsWith("*");
        return pattern.equals("*")
                || startsWithWildcard && endsWithWildcard && pattern.length() >= 2 && check.contains(pattern.substring(1, pattern.length() - 1))
                || startsWithWildcard && check.endsWith(pattern.substring(1))
                || endsWithWildcard && check.startsWith(pattern.substring(0, pattern.length() - 1))
                || check.equals(pattern);
    }

    /**
     * Whether {@code location} ({@code namespace:path}) matches {@code pattern}, a location whose
     * namespace and path are each a wildcard pattern ({@code minecraft:*_sword}, {@code *:*}); a
     * pattern without a namespace is in {@code minecraft}'s.
     */
    public static boolean matchesLocation(String location, String pattern) {
        int at = location.indexOf(':');
        String namespace = at < 0 ? "minecraft" : location.substring(0, at);
        String path = at < 0 ? location : location.substring(at + 1);
        int patternAt = pattern.indexOf(':');
        String patternNamespace = patternAt < 0 ? "minecraft" : pattern.substring(0, patternAt);
        String patternPath = patternAt < 0 ? pattern : pattern.substring(patternAt + 1);
        return new WildcardPattern(patternNamespace).matches(namespace) && new WildcardPattern(patternPath).matches(path);
    }
}
