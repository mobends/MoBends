package net.gobbob.mobends.core.client.gui.customize;

public interface IEditorAction<T>
{

    void perform(T editor);

    void undo(T editor);

}
