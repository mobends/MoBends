package goblinbob.mobends.core.client.gui;

import java.util.ArrayList;
import java.util.List;

public class AnimationEditorRegistry
{
    public static AnimationEditorRegistry INSTANCE = new AnimationEditorRegistry();

    private List<IAnimationEditor> registeredEditors = new ArrayList<>();

    public AnimationEditorRegistry()
    {
    }

    public void registerEditor(IAnimationEditor editor)
    {
        registeredEditors.add(editor);
    }

    /**
     * Right now, this returns the first registered editor.
     * Consider a more sophisticated method if more people create editors.
     * @return
     */
    public IAnimationEditor getPrimaryEditor()
    {
        return registeredEditors.size() > 0 ? registeredEditors.get(0) : null;
    }

}
