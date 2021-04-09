package goblinbob.mobends.forge.addon;

import goblinbob.mobends.core.client.gui.IAnimationEditor;

/**
 * Used as a proxy between the bender provider and the addon that want's to add content to the base mod.
 */
public class AddonContentRegistry implements IAddonContentRegistry
{
    private final String addonKey;

    public AddonContentRegistry(String addonKey)
    {
        this.addonKey = addonKey;
    }

    @Override
    public void registerAnimationEditor(IAnimationEditor editor)
    {
        // TODO Implement this
    }
}
