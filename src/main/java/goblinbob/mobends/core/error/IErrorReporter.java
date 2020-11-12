package goblinbob.mobends.core.error;

public interface IErrorReporter
{
    <T extends Exception> void report(T exception);
}
