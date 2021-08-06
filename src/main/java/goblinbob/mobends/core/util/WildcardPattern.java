package goblinbob.mobends.core.util;

public class WildcardPattern
{
    private String pattern;

    public WildcardPattern(String pattern)
    {
        this.pattern = pattern;
    }

    /**
     * Returns true if the check matches the pattern. The pattern can either be:
     * - text - The check has to match this pattern exactly.
     * - *text - The check has to end with the pattern.
     * - text* - The check has to start with the pattern.
     * - *text* - The check has to contain the pattern.
     * @param check
     * @param pattern
     * @return
     */
    public boolean matches(String check)
    {
        final boolean startsWithWildcard =  pattern.startsWith("*");
        final boolean endsWithWildcard =  pattern.endsWith("*");

        return pattern.equals("*") ||
                startsWithWildcard && endsWithWildcard && check.contains(pattern.substring(1, pattern.length() - 1)) ||
                startsWithWildcard && check.endsWith(pattern.substring(1)) ||
                endsWithWildcard && check.startsWith(pattern.substring(0, pattern.length() - 1)) ||
                check.equals(pattern);
    }
}
