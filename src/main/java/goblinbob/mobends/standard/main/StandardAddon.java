package goblinbob.mobends.standard.main;

import goblinbob.mobends.forge.addon.IAddon;
import goblinbob.mobends.forge.addon.IAddonContentRegistry;

public class StandardAddon implements IAddon
{
    @Override
    public void registerContent(IAddonContentRegistry registry)
    {
//        registry.registerEntity(EntityType.WOLF, new ResourceLocation(ModStatics.MODID, "mutators/wolf.json"), new ResourceLocation(ModStatics.MODID, "animators/wolf.bender"));
    }

    @Override
    public String getDisplayName()
    {
        return "Mo' Bends";
    }
}
