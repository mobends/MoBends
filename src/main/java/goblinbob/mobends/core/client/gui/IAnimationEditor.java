package goblinbob.mobends.core.client.gui;

import net.minecraft.client.gui.GuiScreen;

public interface IAnimationEditor
{
    IAnimationEditorGui createGui(GuiBendsMenu menu);

    class IAnimationEditorGui extends GuiScreen
    {

    }
}
