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

    /** Forge makes it with the Mods list as its only argument. */
    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass()
    {
        return GuiBendsMenu.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories()
    {
        return Collections.emptySet();
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element)
    {
        return null;
    }
}
