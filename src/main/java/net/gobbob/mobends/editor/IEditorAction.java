package net.gobbob.mobends.editor;

public interface IEditorAction<T>
{

    void perform(T editor);

    void undo(T editor);

}
