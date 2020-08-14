package goblinbob.mobends.core.client.gui;

import net.minecraft.client.gui.GuiScreen;

public interface IAnimationEditor
{
    EditorGuiBase createGui(GuiBendsMenu menu);

    class EditorGuiBase extends GuiScreen
    {

    }
}
