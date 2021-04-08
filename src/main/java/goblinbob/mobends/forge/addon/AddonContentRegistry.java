package goblinbob.mobends.forge.addon;

import goblinbob.mobends.core.client.gui.IAnimationEditor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Used as a proxy between the bender provider and the addon that want's to add content to the base mod.
 */
public class AddonContentRegistry implements IAddonContentRegistry
{
    private final String addonKey;

    private final List<BenderEntry<?>> registeredBenders = new ArrayList<>();

    public AddonContentRegistry(String addonKey)
    {
        this.addonKey = addonKey;
    }

    public Iterable<BenderEntry<?>> getRegisteredBenders()
    {
        return registeredBenders;
    }

    @Override
    public <T extends LivingEntity> void registerEntity(EntityType<T> entityType, ResourceLocation instructionsPath, ResourceLocation animatorPath)
    {
        ResourceLocation resourceLocation = EntityType.getKey(entityType);
        if (resourceLocation == null)
            throw new RuntimeException("Unable to find a key for " + entityType.getDescriptionId());

        String entityKey = resourceLocation.toString();
        String key = String.format("%s-%s", this.addonKey, entityKey);
        String unlocalizedName = String.format("entity.%s.name", entityKey);

        this.registeredBenders.add(new BenderEntry<>(entityType, key, unlocalizedName, instructionsPath, animatorPath));
    }

    @Override
    public void registerAnimationEditor(IAnimationEditor editor)
    {
        // TODO Implement this
    }

    public static class BenderEntry<T extends LivingEntity>
    {
        public final EntityType<T> entityType;
        public final String key;
        public final String unlocalizedName;
        public final ResourceLocation mutatorPath;
        public final ResourceLocation animatorPath;

        public BenderEntry(EntityType<T> entityType, String key, String unlocalizedName, ResourceLocation mutatorPath, ResourceLocation animatorPath)
        {
            this.entityType = entityType;
            this.key = key;
            this.unlocalizedName = unlocalizedName;
            this.mutatorPath = mutatorPath;
            this.animatorPath = animatorPath;
        }
    }
}
