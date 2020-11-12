package goblinbob.mobends.core.error;

public interface IErrorReportRegistry
{
    <T extends Exception> void register(Class<T> exceptionClass, IErrorHandler<T> errorHandler);
}
