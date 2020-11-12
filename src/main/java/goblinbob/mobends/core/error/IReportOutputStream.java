package goblinbob.mobends.core.error;

public interface IReportOutputStream
{
    void print(String text);

    void print(String text, int flags);
}
