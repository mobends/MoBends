package goblinbob.mobends.core.error;

import java.util.HashMap;
import java.util.Map;

public class ErrorReportRegistry implements IErrorReportRegistry, IErrorReporter
{
    private IReportOutput reportOutput;

    private Map<Class<? extends Exception>, IErrorHandler<?>> handlers = new HashMap<>();

    public ErrorReportRegistry(IReportOutput reportOutput)
    {
        this.reportOutput = reportOutput;
    }

    @Override
    public <T extends Exception> void register(Class<T> exceptionClass, IErrorHandler<T> errorHandler)
    {
        handlers.put(exceptionClass, errorHandler);
    }

    @Override
    public <T extends Exception> void report(T exception)
    {
        @SuppressWarnings("unchecked")
        IErrorHandler<T> handler = (IErrorHandler<T>) handlers.get(exception.getClass());

        if (handler != null)
        {
            this.reportOutput.beginMessage();
            handler.handle(this.reportOutput, exception);
            this.reportOutput.finishMessage();
        }
    }
}
