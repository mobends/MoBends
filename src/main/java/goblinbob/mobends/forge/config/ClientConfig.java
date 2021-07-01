package goblinbob.mobends.forge.config;

import net.minecraft.util.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public class ClientConfig implements IClientConfigProvider
{
    private final Set<ResourceLocation> disabledTargets = new HashSet<>();

    @Override
    public boolean isTargetDisabled(ResourceLocation resourceLocation)
    {
        return disabledTargets.contains(resourceLocation);
    }
}
