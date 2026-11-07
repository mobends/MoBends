package goblinbob.mobends.standard.client;

import goblinbob.mobends.core.client.gui.GuiBendsMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

import java.util.Collections;
import java.util.Set;

/**
 * The Config button of Forge's Mods list opens the Mo' Bends menu; its More Settings button opens
 * Forge's screen for the mod's configuration, which the Config button opened before.
 */
public class MoBendsGuiFactory implements IModGuiFactory
{
    @Override
    public void initialize(Minecraft minecraft)
    {
    }

    @Override
    public boolean hasConfigGui()
    {
        return true;
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen parent)
    {
        return new GuiBendsMenu(parent);
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories()
    {
        return Collections.emptySet();
    }
}
