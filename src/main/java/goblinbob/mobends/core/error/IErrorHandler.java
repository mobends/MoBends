package goblinbob.mobends.core.error;

@FunctionalInterface
public interface IErrorHandler<T extends Exception>
{
    void handle(IReportOutputStream stream, T exception);
}
