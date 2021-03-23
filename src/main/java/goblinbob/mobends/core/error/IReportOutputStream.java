package goblinbob.mobends.core.error;

public interface IReportOutputStream
{
    /**
     * Prints the specified text in a default style.
     * Same as calling `print(text, 0)`
     * @param text The text to be printed.
     */
    void print(String text);

    /**
     * Prints the specified text, applying optional style according to the flags.
     * @param text The text to be printed.
     * @param flags A binary OR combination of TextStyle flags.
     */
    void print(String text, int flags);
}
