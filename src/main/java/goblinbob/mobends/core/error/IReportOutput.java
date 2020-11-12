package goblinbob.mobends.core.error;

public interface IReportOutput extends IReportOutputStream
{
    void beginMessage();

    void finishMessage();
}
