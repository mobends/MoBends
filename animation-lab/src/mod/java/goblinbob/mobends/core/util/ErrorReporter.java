package goblinbob.mobends.core.util;

/** LAB ONLY. There is no chat: errors meant for the player go to standard error. */
public class ErrorReporter
{
    public static boolean showErrorToPlayer(String error)
    {
        System.err.println("[Mo' Bends] " + error);
        return true;
    }
}
