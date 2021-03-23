package goblinbob.mobends.core.error;

public interface IReportOutput extends IReportOutputStream
{
    /**
     * Prepares everything necessary to start calling 'print' methods.
     */
    void beginMessage();

    /**
     * Packs up whatever's been added by 'print' methods, and outputs it.
     */
    void finishMessage();
}
